---
layout: default
title: Auditing Features
last_updated: Last modified by Connor Mulholland on August 3, 2016
---

# Key Features

The Audit service maintains a documented audit trail of user and system activity in a centralized database. The many benefits of using this Audit service include traceability and accountability but the audit records can also be used for analytical, archiving and reporting purposes. The key features of the Audit service include: 

**Support for Multiple Applications** - user and system audit events are defined per application. The Audit service offers and easy and user-friendly way to register these events using the Audit Management Web Service API.

**Multi-Tenant Capable** - the Audit service is multi-tenant aware. New tenants can be registered easily using the Audit Management Web Service API. This also facilitates audit reporting on a per tenant basis. ... security and scalability.  

**Auto-Generated Client Library** - the Audit service includes an auto-generated client library for type safety. It also makes it easier to send user and system audit events to the messaging system.

**High-Throughput Messaging** - the Audit service integrates with Apache Kafka, a high-throughput distributed messaging system that is designed to be fast, scalable, and durable.

**Scalable** - the Audit service is designed for sclability. ...as load increases, new partitons can be added to the kafka cluster to facilitate an ever exanding ...as the number of audit events increase or you reach your limit, you can add more nodes to facilitate the additional load.

Kafka is designed to allow a single cluster to serve as the central data backbone for a large organization. It can be elastically and transparently expanded without downtime. Data streams are partitioned and spread over a cluster of machines to allow data streams larger than the capability of any single machine and to allow clusters of co-ordinated consumers


**Durable** - the Audit service integrates with Apache Kafka, a high-throughput distributed messaging system that is designed to be fast, scalable, and durable.

Messages are persisted on disk and replicated within the cluster to prevent data loss. Each broker can handle terabytes of messages without performance impact.

**High-Availability** - the Audit service stores user and system audit events in Vertica which can be clustered for high availability.
This should be around kafka high-availability as this is what is important for auditing, not Vertica.
Even if the DB system is being upgraded or temporarily unavailable, audit events can still be written.

**Streaming** - the Audit service utilizes a high-performance loading mechanism which automatically streams audit events from Apache Kafka into Vertica.

loads data into Vertica very efficiently
designed by Vertica development team that knows how to do this best
directly from kafka message bus into Vertica
continous consumes data with exactly-once semantics

**load multiple instances of audit schedulers**  

