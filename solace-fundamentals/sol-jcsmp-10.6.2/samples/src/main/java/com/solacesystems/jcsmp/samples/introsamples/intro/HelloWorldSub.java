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
 * HelloWorldSub
 *
 * This sample shows the basics of creating session, connecting a session,
 * subscribing to a topic, and receiving a message. This is meant to be a
 * very basic example for demonstration purposes.
 */

package com.solacesystems.jcsmp.samples.introsamples.intro;

import java.util.concurrent.CountDownLatch;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldSub {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldPub.class);

    // EXAMPLE ARGUMENTS:  tcp://mrrwtxvkmpdxv.messaging.solace.cloud:20000  bar1-subscriber@msgvpn-zpfs1b9g8px 4r43sbnrcsh8cj2r57oav619k7 bar1/demo 1000
    public static void main(String... args) throws JCSMPException {

        // Validate command line args
        if (args.length < 2 || args[1].split("@").length != 2) {
            System.out.println("Usage: TopicSubscriber <host:port> <client-username@message-vpn> [client-password]");
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

        System.out.println("TopicSubscriber initializing...");
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, hostWithPort);     // host:port
        properties.setProperty(JCSMPProperties.USERNAME, clientUsername); // client-username
        properties.setProperty(JCSMPProperties.VPN_NAME,  msgVpn); // message-vpn
        if (args.length > 2) {
            properties.setProperty(JCSMPProperties.PASSWORD, password); // client-password
        }
        final Topic topic = JCSMPFactory.onlyInstance().createTopic("bar1/demo");
        final JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);

        session.connect();

        final CountDownLatch latch = new CountDownLatch(numMsgs); // used for
        // synchronizing b/w threads
        /** Anonymous inner-class for MessageListener
         *  This demonstrates the async threaded message callback */
        final XMLMessageConsumer cons = session.getMessageConsumer(new XMLMessageListener() {
            @Override
            public void onReceive(BytesXMLMessage msg) {
                if (msg instanceof TextMessage) {
                    System.out.println(String.format("TextMessage received: '%s'%n",
                            ((TextMessage)msg).getText()));
                } else {
                    System.out.println("Message received.");
                }
                System.out.println(String.format("Message Dump:%n%s%n",msg.dump()));
                latch.countDown();  // unblock main thread
            }

            @Override
            public void onException(JCSMPException e) {
                System.out.println(String.format("Consumer received exception: %s%n",e));
                latch.countDown();  // unblock main thread
            }
        });
        session.addSubscription(topic);
        System.out.println("Connected. Awaiting message...");
        cons.start();
        // Consume-only session is now hooked up and running!

        try {
            latch.await(); // block here until message received, and latch will flip
        } catch (InterruptedException e) {
            System.out.println("I was awoken while waiting");
        }
    }
}
