---
layout: default
title: Auditing Features
---

# Key Features

The Audit service maintains a documented audit trail of user and system activity in a centralized database. The many benefits of using this Audit service include traceability and accountability but the audit records can also be used for analytical, archiving and reporting purposes. The key features of the Audit service include: 

**Support for Multiple Applications** - User and system audit events are defined per application. The Audit service offers an easy and user-friendly way to register these events using the Audit Management Web Service API.

**Multi-Tenant Aware** - Multiple tenants are supported for security and scalability. These are registered using the Audit Management Web Service API. Multi-tenancy support also facilitates audit reporting on a per tenant basis. 

**Auto-Generated Client Library** - The Audit service includes an auto-generated client library for type safety. This client library also makes it easier to send user and system audit events to the messaging system.

**High-Throughput Messaging** - Apache Kafka is utilized internally which is a high-throughput distributed messaging system that is designed to be fast.

**Scalable** - The Audit service is designed for scalability as the Kafka cluster can be elastically and transparently expanded without downtime. Data streams are partitioned over a cluster of machines to provide streams much larger than those available through a single machine.

**Durable** - Audit event messages are persisted to the file system and are retained for a fixed amount of time. These messages are also replicated within the Kafka cluster to prevent data loss. 

**High-Availability** - Vertica is used to store the user and system audit event details but the Audit service can still raise audit event messages even if the Vertica database is being upgraded or is temporarily unavailable.  

**High-Performance Streaming** - A high-performance streaming mechanism loads data very efficiently from the Kafka messaging system into Vertica with exactly-once semantics. For high availability, multiple instances of this streaming mechanism can be launched. Only one remains active with others on stand-by in case of failure. This allows the streaming of audit event messages into Vertica without interruption.

