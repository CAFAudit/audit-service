# Apache Kafka 

[Apache Kafka](http://kafka.apache.org/) is a distributed, partitioned, replicated commit log service that provides the functionality of a messaging system.

## Deployment

[Apache Kafka Quick Start Guidelines](http://kafka.apache.org/082/documentation.html#quickstart) provides a detailed set of instructions for installation and start-up. A summary of these steps have been captured below using the current stable version release (i.e. 0.8.2.2).

[Download and install Java](http://openjdk.java.net/install/).

[Download](https://www.apache.org/dyn/closer.cgi?path=/kafka/0.8.2.2/kafka_2.10-0.8.2.2.tgz) the 0.8.2.2 binary and un-tar it: 

$ tar -xzf kafka_2.10-0.8.2.2.tgz 

$ cd kafka_2.10-0.8.2.2

##### Start the server

First start the Kafka zookeeper: 

$ bin/zookeeper-server-start.sh config/zookeeper.properties

[2016-01-13 06:50:23,785] INFO Reading configuration from: config/zookeeper.properties (org.apache.zookeeper.server.quorum.QuorumPeerConfig)

...

Now start the Kafka server: 

$ bin/kafka-server-start.sh config/server.properties

[2016-01-13 07:03:06,909] INFO Verifying properties (kafka.utils.VerifiableProperties)

[2016-01-13 07:03:07,027] INFO Property broker.id is overridden to 0 (kafka.utils.VerifiableProperties) 

...


## Topics and Consumers

### Topic Creation

The Client-side Auditing Library will be used to send audit event messages through to the Apache Kafka messaging service. This will result in topics being created in Kafka. These are named using the format **AuditEventTopic.[ApplicationName].[Tenantid]** where [ApplicationName] is the ApplicationId specified in the Audit Event Definition File and [TenantId] is the tenant identifier.

To see the list of topics created, run the list topic command:

$ bin/kafka-topics.sh --list --zookeeper [localhost:2181](http://localhost:2181/)

#### Consumer

Kafka comes with a command line client that can be used to dump out messages from any topic to standard output. To see messages sent from the Client-side Auditing Library, run the following:

$ bin/kafka-console-consumer.sh --zookeeper [localhost:2181](http://localhost:2181/) --topic AuditEventTopic.[ApplicationName].[Tenantid] --from-beginning

where AuditEventTopic.[ApplicationName].[Tenantid] is the name of the topic of interest.

## Integrating Vertica with Apache Kafka

Vertica 7.2.x provides an [Apache Kafka integration](http://my.vertica.com/docs/7.2.x/PDF/HP_Vertica_7.2.x_Integrating_Apache_Kafka.pdf) feature to automatically load data as it streams through Kafka. It does this through the Kafka job scheduler which continuously consumes data from the Kafka message bus into the database. This scheduler comes pre-packaged and installed with the Vertica 7.2.x rpm.

The job scheduler should be configured and launched before new tenants are added to the system.
