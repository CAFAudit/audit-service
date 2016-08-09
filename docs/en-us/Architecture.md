---
layout: default
title: Architecture
---

# Architecture

In CAF Auditing, audit events are logged on a per application per tenant basis.  The Audit Management Web Service is used to facilitate the addition of new applications and new tenants.

In order to use CAF Auditing with an application you must first specify the audit events that the application uses, and the parameters that are associated with each of the events. The events are specified in an XML file known as the Audit Event Definition File.

When the Audit Event Definition File has been authored it can be used in two ways:

1. to generate an application-specific client-side auditing library
2. to register the application with the Auditing Management Web Service

As stated the Audit Event Definition File can be used to generate an application-specific client-side auditing library. The generated client-side Java library is used to send audit event messages to the Apache Kafka messaging service.

On the server-side, with the use of the Audit Management Web Service API, the application's Audit Event Definition File is used to create an audit event schema for the application within Vertica. The Web Service API is then used to register a tenant with one or more applications. It creates application audit event tables for the tenant and configures the Kafka-Vertica Scheduler to load the audit events into the tables.

Apache Kafka receives Audit events for an application's tenant from the client-side library and partitions them into per application per tenant topics. The Kafka-Vertica Scheduler listens to these topics and streams the events to the tenant's application audit table in Vertica.

## Overview

CAF Auditing is built on Apache Kafka for the messaging of the audit events and HPE Vertica for the storage of the audit events. Both of these technologies offer high availability, throughput, scalability and performance to the overall solution. Additionally Vertica offers strong data analytics capabilities and comes with a pre-built Kafka integration which can be used to continually load data from Kafka.

A Mesos/Marathon environment is used to run the Audit Management Web Service and Kafka-Vertica Scheduler components, and to provide redundancy for these services.

### Audit Management Component Architecture

The figure below illustrates the overall flow and relationship of components in the Audit Management Service.

![Architecture](images/AuditManagementArchitectureDraft.png)

1. Setting up your application for Auditing requires defining an Audit Event Definition XML File. The file is used for: 
	- Generation of the client-side audit library.
	- Registration for auditing on the server-side.
2. Using the caf-audit-maven-plugin the client-side Java library is generated from the Audit Event Definition XML File.
3. The Audit Event Definition XML containing the application and its events for auditing is registered with the server-side Audit Management Web Service API's POST /applications endpoint.
	1. The Audit Management Web Service creates application and event schema based on the Audit Event Definition XML.
4. A tenant with a list of audited applications is registered with the server-side Audit Management Web Service API's POST /tenants endpoint.
	1. The Web Service creates tables for each application that the tenant is registered with in Vertica for storage of the tenant's application events.
	2. The Web Service associates a topic for each application that the tenant is registered with in the Kafka-Vertica Scheduler.
5. The audited application makes calls to the generated client-side library to send tenant audit events to Kafka messaging.
	1. Kafka receives messages from the client-side library and stores them on per application per tenant topics.
	2. Kafka-Vertica Scheduler listens to the per application per tenant topics that are registered with it and streams them into the tenant's application table in Vertica.

### Audit Event Definition File

In order to use CAF Auditing in an application, the events for auditing that the application uses must be specified along with the parameters that are associated with each of the events. These events are specified in an Audit Event Definition File. You can read more about the Audit Event Definition File and its XML schema in the [Getting Started Guide](https://github.hpe.com/caf/caf-audit/blob/develop/docs/en-us/Getting-Started.md).

#### Vertica Database Schema and Tables

Providing the Audit Management Web Service /applications endpoint with the example Audit Event Definition XML will create schemas and tables in the `CAFAudit` database for the application.

![Audit Management Application Events Table With Sample Application](images/AuditManagementApplicationEventsWithSampleAppVertica.png)

The above figure's `ApplicationEvents` table under the `AuditManagement` schema contains a row for a registered SampleApp's Audit Event Definition XML. The `applicationId` column contains the Application ID provided in the Audit Event Definition XML and the `eventsXML` column contains the XML passed to the API. The `eventsXML` is used to by the Audit Management Web Service to create a tenant's application table with columns to match the audit event data types.

Registering a tenant with the Audit Management Web Service /tenants endpoint creates an entry under the `AuditManagement` schema's `TenantApplications` for the tenant.

![Audit Management Tenant Applications Table With Tenant ID](images/AuditManagementTenantApplicationsWithTenantApplication.png)

The above figure shows a row for the registered tenant; `tenantId` with an associated application's `applicationId`. This table keeps track of which tenants are using which applications.

Registering a new tenant creates a new schema under the `CAFAudit` database for the tenant called `account_<tenantId>` where audit event data for the tenant's applications will be held. Audit Management will also create a `kafka_rej` table for holding the tenant's rejected audit events.

![CAF Audit Account 1 Sample App Table Columns](images/account_1AuditSampleAppColumns.png)

The above figure shows an `account_1` schema with an `AuditSampleApp` table and the columns for audit event data for the application.

![CAF Audit Account 1 Kafka Reject Table Columns](images/account_1RejectTable.png)

The above figure shows the `account_1` schema with a `kafka_rej` table and columns for rejected audit event data.