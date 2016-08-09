---
layout: default
title: Auditing Overview
---

# Overview

The Audit service provides applications with a reliable and high performance solution to record events pertaining to user and system applications. 

This documented audit trail of user and system activity can provide many uses. It can be used to provide traceability and individual accountability. Audit records can be used for analytical purposes including security violation detection and abnormal usage patterns. Audit trails have legal standing and can help protect the organization with a proven record of user and system activity. Audit records can also be exported for archival and reporting purposes.

## Introduction
The Audit service allows you to audit user and system actions by defining the required events and the information associated with each event. This definition is registered with the Audit Management Web Service and is also used to create a Java SDK to record the audited events. The Audit service is multi-tenant aware, which requires an application producing events to register each new tenant. Applications send events using the generated Java SDK to Apache Kafka from which the Apache Kafka Vertica Integration loads the audit event messages into a table for the tenant of the application producing messages.

## User and System Actions
Applications will define user and system actions in an audit events definition file. This definition will be registered using the Audit Management Web Service API to create the Vertica database schema to store the audit event messages. The definition is also utilized in order to generate a Java SDK which is used to raise the defined audit events.

For more details on the audit events definition schema, see [Getting Started](https://github.hpe.com/caf/caf-audit-management-service/blob/develop/docs/en-us/Getting-Started.md).

## Java SDK
The audit events definition file is used by a code generation plugin to auto-generate a Java library which is used to send audit event messages to the Apache Kafka messaging service.

For more details on the Auditing Java SDK, see [Client-API](https://github.hpe.com/caf/caf-audit-management-service/blob/develop/docs/en-us/Client-API.md).

## Apache Kafka
Apache Kafka is a distributed, partitioned, replicated commit log service that provides the functionality of a messaging system. The Auditing Java SDK sends user and system audit events to Apache Kafka. These events messages are then streamed from Apache Kafka into the Vertica database schema.

For more details on the Apache Kafka deployment, see [Getting Started](https://github.hpe.com/caf/caf-audit-management-service/blob/develop/docs/en-us/Getting-Started.md).

## Vertica
User and system audit events are stored in Vertica. The database schema for these events is created when each new tenant is registered using the Audit Management Web Service API. Vertica provides a high-performance loading mechanism for streaming the data from Apache Kafka into the Vertica database.

For more details on the Vertica deployment, see [Getting Started](https://github.hpe.com/caf/caf-audit-management-service/blob/develop/docs/en-us/Getting-Started.md).

## Management Web API
The Audit Management Web Service API is a RESTful web service that makes it easy for you to register application audit events and new tenants.

To see the web methods in the Audit Management Web Service API, see [Server-API](https://github.hpe.com/caf/caf-audit-management-service/blob/develop/docs/en-us/Server-API.md).

For more details on the architecture of the Audit Management Web Service API, see [Architecture](https://github.hpe.com/caf/caf-audit-management-service/blob/develop/docs/en-us/Architecture.md).

For instructions on deploying and using the Audit Management Web Service API, see [Getting Started](https://github.hpe.com/caf/caf-audit-management-service/blob/develop/docs/en-us/Getting-Started.md).

