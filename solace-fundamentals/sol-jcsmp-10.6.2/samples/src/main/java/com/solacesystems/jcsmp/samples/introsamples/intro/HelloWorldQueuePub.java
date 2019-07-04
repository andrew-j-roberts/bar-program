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
 *  HelloWorldQueuePub
 *
 *  This sample shows the basics of creating session, connecting a session,
 *  provisioning an exclusive queue, and publishing a message to the queue. 
 *  This is meant to be a very basic example for demonstration purposes.
 */

package com.solacesystems.jcsmp.samples.introsamples.intro;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class HelloWorldQueuePub {

    public static void main(String... args) throws JCSMPException, InterruptedException {
        // Check command line arguments
        if (args.length < 4) {
            System.out.println("Usage: HelloWorldQueuePub <msg_backbone_ip:port> <vpn> <client-username> <queue-name>");
            System.out.println();
            System.out.println(" Note: the client-username provided must have adequate permissions in its client");
            System.out.println("       profile to send and receive guaranteed messages, and to create endpoints.");
            System.out.println("       Also, the message-spool for the VPN must be configured with >0 capacity.");
            System.exit(-1);
        }
        System.out.println("HelloWorldQueuePub initializing...");
        // Create a JCSMP Session
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, args[0]);  // msg-backbone ip:port
        properties.setProperty(JCSMPProperties.VPN_NAME, args[1]);  // message-vpn
        // client-username (assumes no password)
        properties.setProperty(JCSMPProperties.USERNAME, args[2]);
        final String queueName = args[3];
        final JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);
        final CountDownLatch latch = new CountDownLatch(1); // used for synchronizing b/w threads

        System.out.printf("Attempting to provision the queue: '%s' on the appliance.%n",queueName);
        final EndpointProperties endpointProps = new EndpointProperties();
        // set queue permissions to "consume" and access-type to "exclusive"
        endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        // create the queue object locally
        final Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
        // Actually provision it, and do not fail if it already exists
        session.provision(queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

        session.connect();
        /** Anonymous inner-class for handling publishing events */
        final XMLMessageProducer prod = session.getMessageProducer(
                new JCSMPStreamingPublishEventHandler() {
                    public void responseReceived(String messageID) {
                        System.out.printf("Producer received response for msg ID #%s%n",messageID);
                        latch.countDown();  // unblock main thread
                   }
                    public void handleError(String messageID, JCSMPException e, long timestamp) {
                        System.out.printf("Producer received error for msg ID %s @ %s - %s%n",
                                messageID,timestamp,e);
                        latch.countDown();  // unblock main thread
                    }
                });

        // Publish-only session is now hooked up and running!
        System.out.printf("Connected. About to send message to queue '%s'...%n",queue.getName());
        TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        msg.setDeliveryMode(DeliveryMode.PERSISTENT);
        String text = "Hello world! "+DateFormat.getDateTimeInstance().format(new Date());
        msg.setText(text);
        
        // Send message directly to the queue
        prod.send(msg, queue);

        try {
            latch.await(); // block here until message has been sent, and latch will flip
        } catch (InterruptedException e) {
            System.out.println("I was awoken while waiting");
        }
        System.out.println("Message sent. Exiting.");

        // Close session
        session.closeSession();
    }
}
