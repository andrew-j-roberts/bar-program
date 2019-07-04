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
 *  HelloWorldPub
 *
 *  This sample shows the basics of creating session, connecting a session,
 *  and publishing a direct message to a topic. This is meant to be a very
 *  basic example for demonstration purposes.
 */

package com.solacesystems.jcsmp.samples.introsamples.intro;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldPub {

    // Would have used this if I could figure out the dependencies... Ken and I had trouble.
    //private static final Logger logger = LoggerFactory.getLogger(HelloWorldPub.class);

    // EXAMPLE ARGUMENTS: tcp://mrrwtxvkmpdxv.messaging.solace.cloud:20000 bar1-publisher@msgvpn-zpfs1b9g8px 4r43sbnrcsh8cj2r57oav619k7 bar1/demo 1000 250
    public static void main(String... args) throws JCSMPException {

        // Validate command line args
        if (args.length < 4 || args[1].split("@").length != 2) {
            System.out.println("Usage: HelloWorldPub <host:port> <client-username@message-vpn> [client-password] <num-msgs> <msg-interval>");
            System.exit(-1);
        }
        if (args[1].split("@")[0].isEmpty()) {
            System.out.println("No client-username entered");
            System.exit(-1);
        }
        if (args[1].split("@")[1].isEmpty()) {
            System.out.println("No message-vpn entered");
            System.exit(-1);
        }

        // Store command line args
        final String hostWithPort = args[0];
        final String clientUsername = args[1].split("@")[0];
        final String msgVpn = args[1].split("@")[1];
        final String password = args[2];
        final String topicName = args[3];
        final int numMsgs = Integer.parseInt(args[4]);
        final int delayMs = Integer.parseInt(args[5]);

        System.out.println("TopicPublisher initializing...");

        // Create a JCSMP Session
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, hostWithPort);     // host:port
        properties.setProperty(JCSMPProperties.USERNAME, clientUsername); // client-username
        properties.setProperty(JCSMPProperties.VPN_NAME,  msgVpn); // message-vpn
        if (args.length > 2) {
            properties.setProperty(JCSMPProperties.PASSWORD, password); // client-password
        }
        final JCSMPSession session =  JCSMPFactory.onlyInstance().createSession(properties);

        session.connect();

        /** Anonymous inner-class for handling publishing events */
        XMLMessageProducer prod = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
            @Override
            public void responseReceived(String messageID) {
                System.out.println("Producer received response for msg: " + messageID);
            }
            @Override
            public void handleError(String messageID, JCSMPException e, long timestamp) {
                System.out.println(String.format("Producer received error for msg: %s@%s - %s%n",
                        messageID,timestamp,e));
            }
        });
        // Publish-only session is now hooked up and running!

        // Define topic and create message object
        TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        final Topic topic = JCSMPFactory.onlyInstance().createTopic(topicName);
        System.out.println(String.format("Connected. About to send %s messages at a %sms interval to topic '%s'...%n", numMsgs, delayMs, topic.getName()));

        // Publish user-provided number of messages at user-defined interval
        Thread sleepTimer = new Thread();
        for (int i = 0; i < numMsgs; i++) {
            try {
                String text = Integer.toString(i);
                msg.setText(text);
                prod.send(msg,topic);
                sleepTimer.sleep(delayMs);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        System.out.println("Messages sent. Exiting.");
        session.closeSession();
    }
}
