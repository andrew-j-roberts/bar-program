                   Messaging API for Java Sample Applications

INTRODUCTION

   These  samples  provide  a  basic introduction to using the Solace Messaging API for
   Java (JCSMP) in messaging applications. Common uses, such as sending  a  mes-
   sage,  receiving  a message, asynchronous messaging, and subscription manage-
   ment, are described in detail in these samples.

   Before working with these samples, ensure that you have read  and  understood
   the basic concepts found in the Solace Messaging APIs Developer Guide.


SOFTWARE REQUIREMENTS

   The following third-party software tools are required for building  and  run-
   ning the JCSMP samples:

     o Apache Ant version 1.6.5

     o Java SDK 6.0 or above

   The  following  libraries  are  required for building and
   running the JCSMP samples:

   Solace Libraries:

     o sol-jcsmp-<version>.jar

   3rd Party Libraries:

     o commons-codec-1.6.jar

     o commons-lang-2.2.jar

     o commons-logging-1.1.1.jar

     o gnujaxp.jar

     o jsr173_api.jar


INTRODUCTORY SAMPLES LIST

   The following introductory samples are included:

     AdPubAck
        Guaranteed Delivery publishing with handling of mes-
        sage acknowledgements

     AsyncCacheRequest
        Performs an asynchronous cache request

     BlockingSubscriber
        Retrieves  a  message from the application thread in
        blocking mode

     DirectPubSub
        Publish/Subscribe with Direct messages

     DtoPubSub
        Publish/Subscribe with Deliver-To-One features

     EventMonitor
        Monitoring appliance events  using  a  relevant  appliance
        event subscription

     intro/HelloWorldPub 
        This sample shows the basics of creating session, 
        connecting a session, and publishing a direct 
        message to a topic. 
 
     intro/HelloWorldQueuePub 
        This sample shows the basics of creating session, 
        connecting a session, provisioning an exclusive 
        queue, and publishing a message to the queue. 
 
     intro/HelloWorldQueueSub 
        This sample shows the basics of creating session, 
        connecting a session, and subscribing to a queue 
        and provisioning it if it does not exist. 
 
     intro/HelloWorldSub
        This sample shows the basics of creating session, 
        connecting a session, subscribing to a topic, and 
        receiving a message.

     MessageSelectorsOnQueue
        Creating  a  message flow to a queue using a message
        selector to select which messages should  be  deliv-
        ered

     MessageTTLAndDeadMessageQueue
        Provision  endpoints  which  support message TTL and
        message expiry

     NoLocalPubSub
        Demonstrates the use of  the  NO_LOCAL  session  and
        flow property

     PerfSempRequest
        Serial  execution of SEMP queries at high rate. This
        sample shows how  to  reuse  a  java.net.URL  object
        instance to perform several SEMP requests (serially)
        to a appliance over the same connection.

     QueueProvisionAndBrowse
        Provision and browse Queues
        
     QueueProvisionAndRequestActiveFlowIndication
        Provision Queues and request active flow indication
        when creating flows

     Replication
        Demonstrates the use of an unacked list when used with 
        replication.
        
     RRDirectRequester
        Demonstrates how to implement a requestor that sends a request to a
        replier using direct messaging.  This sample can also interoperate with
        a RRDirectReplier sample from another Solace API.
        
     RRDirectReplier
        Demonstates how to implement a replier that accepts requests and reply
        to them using direct messaging.  This sample can also interoperate with
        a RRDirectRequestor sample from another Solace API.
        
     RRGuaranteedRequester
        Demonstrates how to implement a requestor that sends a request to a
        replier using guaranteed messaging.  This sample can also interoperate with
        a RRGuaranteedReplier sample from another Solace API.
        
     RRGuaranteedReplier
        Demonstates how to implement a replier that accepts requests and reply
        to them using guaranteed messaging.  This sample can also interoperate with
        a RRGuaranteedRequestor sample from another Solace API.

     SDTPubSubMsgIndep
        Demonstrates  sending  and receiving direct messages
        with a map structured data type.

     SecureSession
        Demonstrates setting up a secure connection to the 
        appliance.
        
     SempHttpSetRequest
        Demonstrates SEMP request over HTTP port 80

     SempPagingRequests
        Demonstrates SEMP requests with paging

     SempGetOverMB
        Demonstrates simple SEMP requests over  the  message
        bus

     SimpleFlowToQueue
        Demonstrates  creating a flow to a durable or tempo-
        rary queue, and client acknowledgement of messages
        
     SimpleFlowToTopic
        Demonstrates creating a flow to a  durable  or  non-
        durable  topic endpoint, and auto-acknowledgement of
        messages

     SubscribeOnBehalfOfClient
        Shows how to subscribe on behalf of another client

     SyncCacheRequest
        Performs a synchronous cache request

     TopicToQueueMapping
        Shows how to add topic subscriptions to  Queue  end-
        points

     Transactions
        Shows transacted session usage using a request/reply
        scenario.
        
   The source for these samples is in:
   samples/src/com/solacesystems/jcsmp/samples/introsamples

HOW TO BUILD THE SAMPLES

   To  build  the  samples,  go to the samples directory and invoke "ant build".
   Note that this command performs clean before starting the build process.


CONFIGURING THE SOLACE APPLIANCE

   Some samples rely on the presence of a sample durable Queue  and  Topic  End-
   point.   In  addition  to  configuring  the appliance to authenticate the sample
   applications successfully, you must do the following:

     o Create a queue named my_sample_queue

     o Create a durable Topic Endpoint named
       my_sample_topicendpoint

     o Ensure the message-vpn  you  use  is  marked  as  the
       appliance's management message-vpn

     o For  SolCache samples, ensure a cache is setup on the
       Solace appliance


HOW TO RUN THE SAMPLES

   A startup script is provided to set up the Java CLASSPATH and start any  pro-
   vided  sample.  To run the startup script, go to the bin directory, and enter
   the following command:

   On LINUX:
     run.sh introsamples.CLASSNAME -h applianceip[:port] \
     -u username[@vpn] [-w password]
     or
     run.sh introsamples.intro.CLASSNAME <msg_backbone_ip:port> \
     <vpn> <client-username> <topic-name|queue-name>
   On Windows:
     run.bat introsamples.CLASSNAME -h applianceip[:port] \
     -u username[@vpn] [-w password]
     or
     run.bat introsamples.intro.CLASSNAME <msg_backbone_ip:port> \
     <vpn> <client-username> <topic-name|queue-name>
   If you are running the samples on UNIX/LINUX, ensure that execute permissions
   are enabled for run.sh.


Copyright 2009-2019 Solace Corporation. All rights reserved. 
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to use and copy the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
UNLESS STATED ELSEWHERE BETWEEN YOU AND SOLACE CORPORATION, THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
