/**
 *  Copyright 2012-2019 Solace Corporation. All rights reserved.
 *
 *  http://www.solace.com
 *
 *  This source is distributed under the terms and conditions
 *  of any contract or contracts between Solace and you or
 *  your company. If there are no contracts in place use of
 *  this source is not authorized. No support is provided and
 *  no distribution, sharing with others or re-use of this
 *  source is authorized unless specifically stated in the
 *  contracts referred to above.
 *
 *  HelloWorldQueueSub
 *
 *  This sample shows the basics of creating session, connecting a session,
 *  and subscribing to a queue and provisioning it if it does not exist. This 
 *  is meant to be a very basic example for demonstration purposes.
 */

package com.solacesystems.jcsmp.samples.introsamples.intro;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;

public class HelloWorldQueueSub {

    public static void main(String... args) throws JCSMPException, InterruptedException {
        // Check command line arguments
        if (args.length < 4) {
            System.out.println("Usage: HelloWorldQueueSub <msg_backbone_ip:port> <vpn> <client-username> <queue-name>");
            System.out.println();
            System.out.println(" Note: the client-username provided must have adequate permissions in its client");
            System.out.println("       profile to send and receive guaranteed messages, and to create endpoints.");
            System.out.println("       Also, the message-spool for the VPN must be configured with >0 capacity.");
            System.exit(-1);
        }
        System.out.println("HelloWorldQueueSub initializing...");
        // Create a JCSMP Session
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, args[0]);  // msg-backbone ip:port
        properties.setProperty(JCSMPProperties.VPN_NAME, args[1]);  // message-vpn
        // client-username (assumes no password)
        properties.setProperty(JCSMPProperties.USERNAME, args[2]);
        final String queueName = args[3];
        final JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);

        System.out.printf("Attempting to provision the queue '%s' on the appliance.%n",queueName);
        final EndpointProperties endpointProps = new EndpointProperties();
        // set queue permissions to "consume" and access-type to "exclusive"
        endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        // create the queue object locally
        final Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
        // Actually provision it, and do not fail if it already exists
        session.provision(queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

        System.out.printf("Attempting to bind to the queue '%s' on the appliance.%n",queueName);
        // Create a Flow be able to bind to and consume messages from the Queue.
        final ConsumerFlowProperties flow_prop = new ConsumerFlowProperties();
        flow_prop.setEndpoint(queue);
        // set to "auto acknowledge" where the API will ack back to Solace at the
        // end of the message received callback
        flow_prop.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
        EndpointProperties endpoint_props = new EndpointProperties();
        endpoint_props.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        // bind to the queue, passing null as message listener for no async callback
        final FlowReceiver cons = session.createFlow(null, flow_prop, endpoint_props);
        // Start the consumer
        System.out.println("Connected. Awaiting message (for 10 minutes)...");
        cons.start();
        // Consume-only session is now hooked up and running!
        BytesXMLMessage msg = cons.receive(600000);  // wait max 10 minutes for a message
        if (msg != null) {
            System.out.printf("Message received!%n%s%n",msg.dump());
            // When the ack mode is set to SUPPORTED_MESSAGE_ACK_CLIENT, 
            // guaranteed delivery messages are acknowledged after 
            // processing
            msg.ackMessage();
        } else {
            System.out.println("No message received... timed out.");
        }
        // Close consumer
        cons.close();
        System.out.println("Exiting.");
        session.closeSession();
    }
}
