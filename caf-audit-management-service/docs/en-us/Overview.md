---
layout: default
title: Auditing Overview
---

# Overview

The Audit service provides applications with a reliable, high-performance solution for recording events pertaining to user and system applications. 

This documented audit trail of user and system activity has many uses. It provides traceability and individual accountability. Audit records can also be used for analytical purposes, including security violation detection and abnormal usage patterns. Audit trails have legal standing and can help protect the organization with a proven record of user and system activity. Audit records can also be exported for archival and reporting purposes.

## Introduction
The Audit service allows you to audit user and system actions by defining the required events and the information associated with each event. This definition is registered with the Audit Management web service and is also used to create a Java SDK to record the audited events. The Audit service is multi-tenant aware, which requires an application producing events to register each new tenant. Applications send events using the generated Java SDK to Apache Kafka, which in turn uses Vertica integration to load the audit event messages into a table for the tenant of the application-producing messages.

## User and System Actions
Applications define user and system actions in an audit event definition file. This definition is registered using the Audit Management web service API to create the Vertica database schema for storing the audit event messages. The definition is also utilized to generate a Java SDK, which raises the defined audit events.

For more details on the audit events definition schema, see [Getting Started](../../../caf-audit/docs/pages/en-us/Getting-Started.md).

## Java SDK
The audit events definition file is used by a code generation plugin to auto-generate a Java library, which sends audit event messages to the Apache Kafka messaging service.

For more details on the Auditing Java SDK, see [Client-API](../../../caf-audit/docs/pages/en-us/Client-API.md).

## Apache Kafka
Apache Kafka is a distributed, partitioned, replicated commit log service that provides the functionality of a messaging system. The Auditing Java SDK sends user and system audit events to Apache Kafka. These event messages are then streamed from Apache Kafka into the Vertica database schema.

For more details on the Apache Kafka deployment, see [Getting Started](../../../caf-audit/docs/pages/en-us/Getting-Started.md).

## Vertica
User and system audit events are stored in Vertica. The database schema for these events is created when each new tenant is registered using the Audit Management web service API. Vertica provides a high-performance loading mechanism for streaming the data from Apache Kafka into the Vertica database.

For more details on the Vertica deployment, see [Getting Started](../../../caf-audit/docs/pages/en-us/Getting-Started.md).

## Management Web API
The Audit Management web service API is a RESTful web service that makes it easy for you to register application audit events and new tenants.

To see the web methods in the Audit Management web service API, see [Server-API](../../../caf-audit/docs/pages/en-us/Server-API.md).

For more details on the architecture of the Audit Management web service API, see [Architecture](../../../caf-audit/docs/pages/en-us/Architecture.md).

For instructions on deploying and using the Audit Management web service API, see [Getting Started](../../../caf-audit/docs/pages/en-us/Getting-Started.md).

