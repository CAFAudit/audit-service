---
layout: default
title: Auditing Features

banner:
    icon: 'assets/img/auditing-graphic.png'
    title: Auditing
    subtitle: Traceability, accountability, analytics, archiving and reporting of application tenant events.
    links:
        - title: GitHub
          url: https://github.com/CAFAudit/audit-service
---

# Features

The Audit service maintains a documented audit trail of user and system activity in a centralized database. The benefits of using the Audit service include traceability and accountability, but the audit records can also be used for analytical, archiving and reporting purposes.

## Support for Multiple Applications
User and system audit events are defined per application.

## Multi-Tenant Aware
The service supports multiple tenants in a secure and scalable way.  Multi-tenancy support also facilitates audit reporting on a per tenant basis. 

## Auto-Generated Client Library
The service includes an auto-generated client-side library for type safety.  This client library also makes it easier to send user and system audit events to Elasticsearch.

## Scalable
The service is extensively for scalabe as the Elasticsearch is designed with scalability in mind and can scale horizontally without downtime.

## Durable
The service is designed for durability as Elasticsearch provides data redundancy across it's highly scalable cluster.

## High-Availability
The service is highly available as Elasticsearch replicates and distributes data across it's scalable, durable cluster.  
<br/>  
<br/>  
