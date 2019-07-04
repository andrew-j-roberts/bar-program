/**
 * DirectPubSub.java
 * 
 * This sample demonstrates:
 *  - Subscribing to a topic for direct messages.
 *  - Publishing direct messages to a topic.
 *  - Receiving messages with a message handler.
 *
 * This sample shows the basics of creating a context, creating a
 * session, connecting a session, subscribing to a topic, and publishing
 * direct messages to a topic. This is meant to be a very basic example, 
 * so there are minimal session properties and a message handler that simply 
 * prints any received message to the screen.
 * 
 * Although other samples make use of common code to perform some of the
 * most common actions, many of those common methods are explicitly
 * included in this sample to emphasize the most basic building blocks of
 * any application.
 * 
 * Copyright 2006-2019 Solace Corporation. All rights reserved.
 */

package com.solacesystems.jcsmp.samples.introsamples;

import com.solacesystems.jcsmp.*;
import com.solacesystems.jcsmp.samples.introsamples.common.ArgParser;
import com.solacesystems.jcsmp.samples.introsamples.common.SampleApp;
import com.solacesystems.jcsmp.samples.introsamples.common.SampleUtils;
import com.solacesystems.jcsmp.samples.introsamples.common.SessionConfiguration;
import com.solacesystems.jcsmp.samples.introsamples.common.SessionConfiguration.AuthenticationScheme;

import java.security.SecureRandom;

public class DirectPubSub extends SampleApp {
	
	JCSMPSession session = null;
	SessionConfiguration conf = null;
	
	XMLMessageConsumer cons = null;
    XMLMessageProducer prod = null;

    Integer messageCount = 0;

	public static final String binaryAttachment = "Hello World";    
    	
	public DirectPubSub() {
	}
    
	void printUsage(boolean secure) {
		String strusage = ArgParser.getCommonUsage(secure);
		System.out.println(strusage);
	}
	
	public static void main(String[] args) {
		DirectPubSub directPubSub = new DirectPubSub();
		directPubSub.run(args);
	}
	
	void run(String[] args) {
		
		// Parse command-line arguments.
		ArgParser parser = new ArgParser();
		if (parser.parse(args) != 0) {
			printUsage(parser.isSecure());
		} else {
			conf = parser.getConfig();
		}
		if (conf == null)
			finish(1);
		
		// Create a new Session. The Session properties are extracted from the
		// SessionConfiguration that was populated by the command line parser.
		//
		// Note: In other samples, a common method is used to create the Sessions.
		// However, to emphasize the most basic properties for Session creation,
		// this method is directly included in this sample.
		try {
			// Create session from JCSMPProperties. Validation is performed by
			// the API, and it throws InvalidPropertiesException upon failure.
			System.out.println("About to create session.");
			System.out.println("Configuration: " + conf.toString());			
			
			JCSMPProperties properties = new JCSMPProperties();

			properties.setProperty(JCSMPProperties.HOST, conf.getHost());
			properties.setProperty(JCSMPProperties.USERNAME, conf.getRouterUserVpn().get_user());

			if (conf.getRouterUserVpn().get_vpn() != null) {
				properties.setProperty(JCSMPProperties.VPN_NAME, conf.getRouterUserVpn().get_vpn());
			}
			
			properties.setProperty(JCSMPProperties.PASSWORD, conf.getRouterPassword());
	        
			// With reapply subscriptions enabled, the API maintains a
			// cache of added subscriptions in memory. These subscriptions
			// are automatically reapplied following a channel reconnect.
			properties.setBooleanProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS, true);

	        // Disable certificate checking
	        properties.setBooleanProperty(JCSMPProperties.SSL_VALIDATE_CERTIFICATE, false);

	        if (conf.getAuthenticationScheme().equals(AuthenticationScheme.BASIC)) {
	            properties.setProperty(JCSMPProperties.AUTHENTICATION_SCHEME, JCSMPProperties.AUTHENTICATION_SCHEME_BASIC);   
	        } else if (conf.getAuthenticationScheme().equals(AuthenticationScheme.KERBEROS)) {
	            properties.setProperty(JCSMPProperties.AUTHENTICATION_SCHEME, JCSMPProperties.AUTHENTICATION_SCHEME_GSS_KRB);   
	        }

	        // Channel properties
	        JCSMPChannelProperties cp = (JCSMPChannelProperties) properties
				.getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
	        
			if (conf.isCompression()) {
				// Compression is set as a number from 0-9 where 0 means "disable
				// compression" and 9 means max compression. The default is no
				// compression.
				// Selecting a non-zero compression level auto-selects the
				// compressed SMF port on the appliance, as long as no SMF port is
				// explicitly specified.
				cp.setCompressionLevel(9);
			}
			
			cp.setConnectRetries(5);

			// SET THIS SO THAT WE CAN DISPLAY MESSAGE DISCARDS
			properties.setProperty(JCSMPProperties.MESSAGE_CALLBACK_ON_REACTOR, true);

			session =  JCSMPFactory.onlyInstance().createSession(properties);			
			
		} catch (InvalidPropertiesException ipe) {
			System.err.println("Error during session creation: ");
			ipe.printStackTrace();
			finish(1);
		}

		try {
			
			// Acquire a message consumer and open the data channel to the appliance.
			System.out.println("About to connect to appliance.");
	        session.connect();
			// The message handler is invoked for each Direct message received
			// by the Session.
			//
			// Message handler code is executed within the API thread, which means
			// that it should deal with the message quickly or queue the message
			// for further processing in another thread.
			//
			// Note: In other samples, a common message handler is used. However,
			// to emphasize this programming paradigm, the message
			// receive handler is directly included in this sample.
			cons = session.getMessageConsumer(new XMLMessageListener() {
				public void onReceive(BytesXMLMessage msg) {
					if(msg.getDiscardIndication()) {
						System.out.println("DISCARDED!");
					}
					System.out.println("Received message " + msg);
					try {Thread.sleep(2000);}
					catch(Exception e){System.out.println(e);}

//					messageCount++;
//					System.out.println("Received message # " + messageCount);

				}
				public void onException(JCSMPException e) {
					System.out.println(e);
				}
			});
			
			// Use a Topic subscription.
			Topic topic = JCSMPFactory.onlyInstance().createTopic(SampleUtils.SAMPLE_TOPIC);
			System.out.printf("Setting topic subscription '%s'...\n", topic.getName());
			session.addSubscription(topic);
			System.out.println("Connected!");

			// Receive messages.
			cons.start();
			
			// Acquire a message producer.
			prod = session.getMessageProducer(new PrintingPubCallback());

			/*
			 * BAR 1 DEMO
			 */

//			// ELIDING
//			// - Subscriber's Client Profile is set to have eliding enabled, and to receive a message every 250ms
//			// - Publisher is setting the eliding eligible flag on the messages being published
//			// - To demonstrate this capability, we'll send 100 messages at 125ms intervals and show that the consumer only receives half of them
//			for (int msgsSent = 0; msgsSent < 100; ++msgsSent) {
//
//				XMLMessage msg = JCSMPFactory.onlyInstance().createMessage(BytesXMLMessage.class);
//				msg.writeAttachment(SampleUtils.attachmentText.getBytes());
//				msg.setDeliveryMode(DeliveryMode.DIRECT);
//
//				// Configure eliding, send at 125ms intervals
//				msg.setElidingEligible(true);
//				prod.send(msg, topic);
//				Thread.sleep(125);
//			}

			// USER COS LEVELS AND DISCARD INDICATIONS
			// - To demonstrate this capability, we're going to first send 1000 msgs @ COS 1
			XMLMessage msg = JCSMPFactory.onlyInstance().createMessage(BytesXMLMessage.class);
			byte[] bytes = new byte[2048];
			new SecureRandom().nextBytes(bytes);
			msg.writeAttachment(bytes);
			msg.setCos(User_Cos.USER_COS_1);
			msg.setDeliveryMode(DeliveryMode.DIRECT);

			for (int msgsSent = 0; msgsSent < 100; ++msgsSent) {
				prod.send(msg, topic);
			}

			Thread.sleep(100000);
			// Stop the consumer and remove the subscription.
			//Thread.sleep(10000000);
			cons.stop();
			session.removeSubscription(topic);

			finish(0);
		} catch (Exception ex) {
			// Normally, we would differentiate the handling of various exceptions, but
			// to keep this sample simple, all exceptions
			// are handled in the same way.
			System.err.println("Encountered an Exception: " + ex.getMessage());
			ex.printStackTrace(System.err);
			finish(1);
		}
	}
	
	protected void finish(final int status) {
		if (cons != null) {
			cons.close();
		}
		
		if (session != null) {
			session.closeSession();
		}
		
		System.exit(status);
	}	
}
