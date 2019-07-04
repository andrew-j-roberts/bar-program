/**
 * MessageTTLAndDeadMessageQueue.java
 *
 * This sample demonstrates how to provision an endpoint (in this case a 
 * Queue) on the appliance which supports message TTL; how to send a message
 * with TTL enabled; how to allow an expired message to be collected by
 * the Dead Message Queue (DMQ).
 *
 * Copyright 2011-2019 Solace Corporation. All rights reserved.
 */

package com.solacesystems.jcsmp.samples.introsamples;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.solacesystems.jcsmp.*;
import com.solacesystems.jcsmp.samples.introsamples.common.ArgParser;
import com.solacesystems.jcsmp.samples.introsamples.common.SampleApp;
import com.solacesystems.jcsmp.samples.introsamples.common.SampleUtils;
import com.solacesystems.jcsmp.samples.introsamples.common.SessionConfiguration;

public class MessageTTLAndDeadMessageQueue extends SampleApp {
	Consumer cons = null;
	XMLMessageProducer prod = null;
	SessionConfiguration conf = null;
	static int rx_msg_count = 0;
	private static String DMQ_NAME = "#DEAD_MSG_QUEUE";
	private Queue deadMsgQ = null;

	void createSession(String[] args) throws InvalidPropertiesException {
		// Parse command-line arguments
		ArgParser parser = new ArgParser();
		if (parser.parse(args) == 0)
			conf = parser.getConfig();
		else
			printUsage(parser.isSecure());

		JCSMPProperties properties = new JCSMPProperties();
		properties.setProperty(JCSMPProperties.HOST, conf.getHost());
		properties.setProperty(JCSMPProperties.USERNAME, conf.getRouterUserVpn().get_user());
		if (conf.getRouterUserVpn().get_vpn() != null) {
			properties.setProperty(JCSMPProperties.VPN_NAME, conf.getRouterUserVpn().get_vpn());
		}
		properties.setProperty(JCSMPProperties.PASSWORD, conf.getRouterPassword());
		/*
		 *  SUPPORTED_MESSAGE_ACK_CLIENT means that the received messages on the Flow
		 *  must be explicitly acknowledged, otherwise the messages are redelivered to the client
		 *  when the Flow reconnects.
		 *  SUPPORTED_MESSAGE_ACK_CLIENT is used here to simply to show
		 *  SUPPORTED_MESSAGE_ACK_CLIENT. Clients can use SUPPORTED_MESSAGE_ACK_AUTO
		 *  instead to automatically acknowledge incoming Guaranteed messages.
		 */

		/** SET ACK MODE TO CLIENT FOR DEMO **/
		properties.setProperty(JCSMPProperties.MESSAGE_ACK_MODE, JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);

		// Disable certificate checking
		properties.setBooleanProperty(JCSMPProperties.SSL_VALIDATE_CERTIFICATE, false);

		if (conf.getAuthenticationScheme().equals(SessionConfiguration.AuthenticationScheme.BASIC)) {
			properties.setProperty(JCSMPProperties.AUTHENTICATION_SCHEME, JCSMPProperties.AUTHENTICATION_SCHEME_BASIC);
		} else if (conf.getAuthenticationScheme().equals(SessionConfiguration.AuthenticationScheme.KERBEROS)) {
			properties.setProperty(JCSMPProperties.AUTHENTICATION_SCHEME, JCSMPProperties.AUTHENTICATION_SCHEME_GSS_KRB);
		}

		// Channel properties
		JCSMPChannelProperties cp = (JCSMPChannelProperties) properties
				.getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
		if (conf.isCompression()) {
			/*
			 * Compression is set as a number from 0-9. 0 means
			 * "disable compression" (the default) and 9 means max compression.
			 * Selecting a non-zero compression level auto-selects the
			 * compressed SMF port on the appliance, as long as no SMF port is
			 * explicitly specified.
			 */
			cp.setCompressionLevel(9);
		}
		session = JCSMPFactory.onlyInstance().createSession(properties);
	}

	void printUsage(boolean secure) {
		String strusage = ArgParser.getCommonUsage(secure);
		strusage += "This sample:\n";
		strusage += "\t[-c]             Enable calculation of message expiration time\n";

		System.out.println(strusage);
		finish(1);
	}

	public static void main(String[] args) {
		MessageTTLAndDeadMessageQueue qsample = new MessageTTLAndDeadMessageQueue();
		qsample.run(args);
	}

	void checkCapability(final CapabilityType cap) {
		System.out.printf("Checking for capability %s...", cap);
		if (session.isCapable(cap)) {
			System.out.println("OK");
		} else {
			System.out.println("FAILED");
			finish(1);
		}
	}

	byte[] getBinaryData(int len) {
		final byte[] tmpdata = "the quick brown fox jumps over the lazy dog / flying spaghetti monster ".getBytes();
		final byte[] ret_data = new byte[len];
		for (int i = 0; i < len; i++)
			ret_data[i] = tmpdata[i % tmpdata.length];
		return ret_data;
	}

	void run(String[] args) {
		try{createSession(args);}
		catch (Exception e){System.out.println("WHOOPS");}
		int finishCode = 0;
		Queue ep_queue = null;
		try {

			// Connects the Session and acquires a message producer.
			session.connect();
			prod = session.getMessageProducer(new PrintingPubCallback());

			// Check capability for message expiration.
			checkCapability(CapabilityType.ENDPOINT_MESSAGE_TTL);
			// Check capability to provision endpoints.
			checkCapability(CapabilityType.ENDPOINT_MANAGEMENT);

			final String virtRouterName = (String) session.getProperty(JCSMPProperties.VIRTUAL_ROUTER_NAME);
			System.out.printf("Router's virtual router name: '%s'\n", virtRouterName);

			/*
			 * Provision the DMQ, if it is not already provisioned.
			 */
			EndpointProperties dmq_provision = new EndpointProperties();
			dmq_provision.setPermission(EndpointProperties.PERMISSION_DELETE);
			dmq_provision.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
			deadMsgQ = JCSMPFactory.onlyInstance().createQueue(DMQ_NAME);
			session.provision(deadMsgQ,dmq_provision,JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

			/*
			 * Provision a new Queue on the appliance, ignoring if it already
			 * exists. Set permissions, access type, and provisioning flags.
			 */
			EndpointProperties ep_provision = new EndpointProperties();
			// Set permissions to allow all
			ep_provision.setPermission(EndpointProperties.PERMISSION_DELETE);
			// Set access type to exclusive
			ep_provision.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
			// Quota to 100MB
			ep_provision.setQuota(100);
			// Set queue to respect message TTL
			ep_provision.setRespectsMsgTTL(Boolean.TRUE);
			/****
			 * ADD REDELIVERY LIMIT
			 ***/
			ep_provision.setMaxMsgRedelivery(1);

			String ep_qn = "sample_queue_MessageTTLAndDMQ_" + String.valueOf(System.currentTimeMillis() % 10000);
			ep_queue = JCSMPFactory.onlyInstance().createQueue(ep_qn);
			System.out.printf("Provision queue '%s' on the appliance...", ep_queue);
			session.provision(ep_queue, ep_provision, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
			System.out.println("OK");

			/**
			 * Publish five messages to the Queue using
			 * producer-independent messages acquired from JCSMPFactory.
			 *
			 * REDELIVERY LIMIT DEMO
			 **/
			BytesXMLMessage m = JCSMPFactory.onlyInstance().createMessage(BytesXMLMessage.class);
			m.setDeliveryMode(DeliveryMode.PERSISTENT);
			for (int i = 1; i <= 5; i++) {
				m.setUserData(String.valueOf(i).getBytes());
				m.writeAttachment(getBinaryData(i * 20));
				m.setDMQEligible(true);
				prod.send(m, ep_queue);
			}
			Thread.sleep(1000);
			System.out.println("Sent five messages.");

			/*
			 * Create a flow to consume messages on the Queue. There should be
			 * five messages on the Queue.
			 */
			rx_msg_count = 0;
			ConsumerFlowProperties flow_prop = new ConsumerFlowProperties();
			flow_prop.setEndpoint(ep_queue);
			cons = session.createFlow(new MessageDumpListener(), flow_prop);
			cons.start();
			Thread.sleep(2000);
			System.out.printf("Finished consuming messages, the number of message on the queue is '%s'.\n", rx_msg_count);
			cons.close();

			/*
			 * Create a new Flow to consume redelivered messages to the Queue.
			 */
			rx_msg_count = 0;
			flow_prop = new ConsumerFlowProperties();
			flow_prop.setEndpoint(ep_queue);
			cons = session.createFlow(new MessageDumpListener(), flow_prop);
			cons.start();
			Thread.sleep(2000);
			System.out.printf("Finished consuming redelivered messages, the number of message on the queue is '%s'.\n", rx_msg_count);
			cons.close();

			/*
			 *  Open a new Flow again, but this time the messages should have been sent to the DMQ.  Queue size === 0
			 */
			rx_msg_count = 0;
			flow_prop = new ConsumerFlowProperties();
			flow_prop.setEndpoint(ep_queue);
			cons = session.createFlow(new MessageDumpListener(), flow_prop);
			cons.start();
			Thread.sleep(2000);
			System.out.printf("Finished consuming redelivered messages, the number of message on the queue is '%s'.\n", rx_msg_count);
			cons.close();

			/****
			 * TTL DEMO
			 ****/

			/*
			 *  Send 5 messages with TTL of 3 seconds.
			 */
			m = JCSMPFactory.onlyInstance().createMessage(BytesXMLMessage.class);
			m.setDeliveryMode(DeliveryMode.PERSISTENT);
			for (int i = 1; i <= 5; i++) {
				m.setUserData(String.valueOf(i).getBytes());
				m.writeAttachment(getBinaryData(i * 20));
				m.setTimeToLive(3000);
				m.setDMQEligible(true);
				prod.send(m, ep_queue);
			}
			System.out.println("Sent five messages.");

			/*
			 * Let messages expire
			 */
			Thread.sleep(5000);

			/**
			 * See that the expired messages are added to the DMQ
			 */
			ConsumerFlowProperties dmq_props = new ConsumerFlowProperties();
			dmq_props.setEndpoint(deadMsgQ);
			rx_msg_count = 0;
			try {
				cons = session.createFlow(new MessageDumpListener(), dmq_props);
				cons.start();
				Thread.sleep(1000);
				System.out.printf("Finished browsing the DMQ, the number of message on the DMQ is '%s'.\n", rx_msg_count);
				/*
				 * Close the Flow.
				 */
				cons.close();
			} catch (JCSMPErrorResponseException ex) {
				System.out.println("DMQ is not properly configured on the appliance: " + ex.getMessage());
			}

		} catch (JCSMPTransportException ex) {
			System.err.println("Encountered a JCSMPTransportException, closing consumer channel... " + ex.getMessage());
			if (cons != null) {
				cons.close();
				// At this point the consumer handle is unusable; a new one
				// should be created by calling
				// cons = session.getMessageConsumer(...) if the
				// application logic requires the consumer channel to remain open.
			}
			finishCode = 1;
		} catch (JCSMPException ex) {
			System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
			// Possible causes:
			// - Authentication error: invalid username/password
			// - Provisioning error: unable to add subscriptions from CSMP
			// - Invalid or unsupported properties specified
			if (cons != null) {
				cons.close();
				// At this point the consumer handle is unusable; a new one
				// should be created
				// by calling cons = session.getMessageConsumer(...) if the
				// application
				// logic requires the consumer channel to remain open.
			}
			finishCode = 1;
		} catch (Exception ex) {
			System.err.println("Encountered an Exception... " + ex.getMessage());
			finishCode = 1;
		} finally {
			if (cons != null) {
				cons.close();
			}
			if (ep_queue != null) {
				System.out.printf("Deprovision queue '%s'...", ep_queue);
				try {
					session.deprovision(ep_queue, JCSMPSession.FLAG_IGNORE_DOES_NOT_EXIST);
				} catch(JCSMPException e) {
					e.printStackTrace();
				}
			}
			System.out.println("OK");
			finish(finishCode);
		}

	}

	static class MessageDumpListener implements XMLMessageListener {
		public void onException(JCSMPException exception) {
			exception.printStackTrace();
		}

		public void onReceive(BytesXMLMessage message) {
			//System.out.println("\n======== Received message ======== \n" + message.dump());
			rx_msg_count++;
		}
	}

}
