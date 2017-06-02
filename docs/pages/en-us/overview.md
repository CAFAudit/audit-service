---
layout: default
title: Auditing Overview

banner:
    icon: 'assets/img/auditing-graphic.png'
    title: Auditing
    subtitle: Traceability, accountability, analytics, archiving and reporting of application tenant events.
    links:
        - title: GitHub
          url: https://github.com/CAFAudit/audit-service
---

# Overview

The Audit service provides applications with a reliable, high-performance solution for recording events pertaining to user and system applications. 

This documented audit trail of user and system activity has many uses.  It provides traceability and individual accountability.  Audit records can also be used for analytical purposes, including security violation detection and abnormal usage patterns.  Audit trails have legal standing and can help protect the organization with a proven record of user and system activity.  Audit records can also be exported for archival and reporting purposes.

## Introduction

The Audit Service is designed to provide auditing of user and system actions by defining the required events and the information associated with each event.  An application audit event definition file can be used to generate an application-specific, client-side auditing library.  The Audit service is multi-tenant aware.  Applications send events using the generated client-side auditing library to an endpoint such as the Audit Web Service API or directly to Elasticsearch where they are indexed according to each tenant.

## User and System Actions

Applications define user and system actions in an audit event definition file.  The definition is utilized to generate a client-side auditing library which raises the defined audit events.

For more details on the audit event definition file, see [Getting Started](Getting-Started).

## Audit Library

The audit events definition file is used by a code generation plugin to auto-generate a client-side Java library, which provides type safety and sends audit event messages to an endpoint.

For more details on the Java Client-Side Audit Library, see [Getting Started](Getting-Started#generating-a-client-side-auditing-library).

## Elasticsearch

Elasticsearch is an opensource, distributable, scalable, enterprise grade search engine.  Elasticsearch is accessible through an extensive RESTful API and can provide multi-tenant capable full text searches that support data discovery.  Official clients are available in Java, .NET (C#), Python, Groovy and many other languages.

For more details on Elasticsearch deployment, see [Audit Service Deploy Project in GitHub](https://github.com/CAFAudit/audit-service-deploy).  
For more details on Elasticsearch, see [Elasticsearch](https://www.elastic.co/products/elasticsearch).

## Audit Web Service

The Audit Web Service API provides a RESTful interface for indexing audit event messages into Elasticsearch. Audit events, in the form of REST POST JSON requests, are sent to the Audit Web Service API which then connects to Elasticsearch and indexes the details of the audit event message for the tenant application.

For more details on Web Service, see [Web Service](Web-Service).
