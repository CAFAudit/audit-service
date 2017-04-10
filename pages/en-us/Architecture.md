---
layout: default
title: Architecture
---

# Architecture

Auditing logs audit events on a per application, per tenant basis.  The Audit Management web service facilitates the addition of new applications and new tenants.

In order to use Auditing with an application, you must first specify the audit events that the application uses, and the parameters that you want to associate with each of the events. The events are specified in an XML file known as the audit event definition file.

After you author the audit event definition file, you can use it in two ways:

1. To generate an application-specific, client-side auditing library
2. To register the application with the Auditing Management web service

After you generate an application-specific, client-side auditing library, the application uses the Java library to send audit event messages to the Apache Kafka messaging service.

On the server-side, with the Audit Management web service API, the application's audit event definition file is used to create an audit event schema for the application within Vertica. The web service API can then register a tenant with one or more applications. It creates application audit event tables for the tenant and associates application tenant topics with the Kafka-Vertica Scheduler.

Apache Kafka receives Audit events for an application's tenant from the client-side library and partitions them into per application, per tenant topics. The Kafka-Vertica Scheduler listens to these topics and streams the events to the tenant's application audit table in Vertica.

## Overview

Auditing is built on Apache Kafka for the messaging of the audit events and HPE Vertica for the storage of the audit events. Both of these technologies offer high availability, throughput, scalability, and performance to the overall solution. Additionally, Vertica offers strong data analytics capabilities and comes with a pre-built Kafka integration, which can continually load data from Kafka.

A Mesos/Marathon environment runs the Audit Management web service and Kafka-Vertica Scheduler components, and provides redundancy for these services.

### Audit Management Component Architecture

The figure below illustrates the overall flow and relationship of components in the Audit Management service.

![Architecture](https://cafaudit.github.io/audit-service/pages/en-us/images/AuditManagementArchitectureDraft.png)

1. Setting up your application for Auditing requires defining an audit event definition XML file. The file is used for:
	- Generation of the client-side audit library.
	- Registration for auditing on the server-side.
2. Using the caf-audit-maven-plugin, the client-side Java library is generated from the audit event definition XML file.
3. The audit event definition XML containing the application and its events for auditing is registered with the server-side Audit Management web service API's POST /applications endpoint.
	1. The Audit Management web service creates application and event schema based on the audit event definition XML.
4. A tenant with a list of audited applications is registered with the server-side Audit Management web service API's POST /tenants endpoint.
	1. The web service creates tables for each application with which the tenant is registered in Vertica for storage of the tenant's application events.
	2. The web service associates a topic for each application that the tenant is registered with in the Kafka-Vertica Scheduler.
5. The audited application makes calls to the generated client-side library to send tenant audit events to Kafka messaging.
	1. Kafka receives messages from the client-side library and stores them in per application, per tenant topics.
	2. Kafka-Vertica Scheduler listens to the topics that are registered with it and streams them into the tenant's application table in Vertica.

### Audit Event Definition File

In order to use Auditing in an application, the application's auditing events must be specified along with the parameters that are associated with each of the events. These events are specified in an audit event definition file. You can read more about the audit event definition file and its XML schema in the [Getting Started Guide](Getting-Started.md).

### Vertica Database Schema and Tables

Providing the Audit Management web service /applications endpoint with the example audit event definition XML will create schemas and tables in the `CAFAudit` database for the application.

![Audit Management Application Events Table With Sample Application](https://cafaudit.github.io/audit-service/pages/en-us/images/AuditManagementApplicationEventsWithSampleAppVertica.png)

The above figure's `ApplicationEvents` table, under the `AuditManagement` schema, contains a row for a registered SampleApp's audit event definition XML. The `applicationId` column contains the Application ID provided in the audit event definition XML and the `eventsXML` column contains the XML passed to the API. Audit Management web service uses `eventsXML` to create a tenant's application table with columns to match the audit event data types.

Registering a tenant with the Audit Management web service /tenants endpoint creates an entry under the `AuditManagement` schema's `TenantApplications` for the tenant.

![Audit Management Tenant Applications Table With Tenant ID](https://cafaudit.github.io/audit-service/pages/en-us/images/AuditManagementTenantApplicationsWithTenantApplication.png)

The figure shows a row for the registered tenant; `tenantId` with an associated application's `applicationId`. This table keeps track of which tenants use which applications.

Registering a new tenant creates a new schema under the `CAFAudit` database for the tenant called `account_<tenantId>`, where audit event data for the tenant's applications will be held. Audit Management also creates a `kafka_rej` table for holding the tenant's rejected audit events.

![CAF Audit Account 1 Sample App Table Columns](https://cafaudit.github.io/audit-service/pages/en-us/images/account_1AuditSampleAppColumns.png)

The figure shows an `account_1` schema with an `AuditSampleApp` table and the columns for audit event data for the application.

![CAF Audit Account 1 Kafka Reject Table Columns](https://cafaudit.github.io/audit-service/pages/en-us/images/account_1RejectTable.png)

The figure shows the `account_1` schema with a `kafka_rej` table and columns for rejected audit event data.
