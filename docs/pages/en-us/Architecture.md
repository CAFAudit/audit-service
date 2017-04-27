---
layout: default
title: Architecture

banner:
    icon: 'assets/img/auditing-graphic.png'
    title: Auditing
    subtitle: Traceability, accountability and archiving of application tenant events.
    links:
        - title: GitHub
          url: https://github.com/CAFAudit/audit-service
---

# Architecture

The Audit library logs audit events on a per tenant, per application basis and facilitates the addition of new tenants and new applications.

In order to use Auditing with an application, you must first specify the audit events that the application uses and the parameters that you want to associate with each of the events. The events are specified in an XML file known as the audit event definition file and are used to generate an application-specific, client-side auditing library that sends the application events for auditing to Elasticsearch.

Elasticsearch receives application audit events for a tenant from the client-side library and adds the application audit event to the tenant's index.

## Overview

Auditing is built on Elasticsearch for the messaging and storage of the audit events. Elasticsearch offers high availability, throughput, scalability, and performance to the overall solution. Additionally, Elasticsearch offers strong data analytics capabilities.

### Audit Management Component Architecture

The figure below illustrates the overall flow and relationship of components in the Audit service.

![Architecture](images/AuditElasticArchitecture.png)

1. Setting up your application for Auditing requires defining an audit event definition XML file. The file is used for generation of the client-side audit library.
2. Using the `caf-audit-maven-plugin`, the client-side Java library is generated from the audit event definition XML file.
3. The audited application makes calls to the generated client-side library to send tenant audit events to Elasticsearch which stores them in per tenant indices.

### Audit Event Definition File

In order to use Auditing in an application, the application's auditing events must be specified along with the parameters that are associated with each of the events. These events are specified in an audit event definition file. You can read more about the audit event definition file and its XML schema in the [Getting Started Guide](Getting-Started.md).

### Elasticsearch Indices and Type Mappings

On a tenant's first call of the Audit library, an index is created for the them. A tenant's Elasticsearch index, type and document's ID (`index/type/_id`) is seen as `audit_tenant_<tenantId>/cafAuditEvent/<applicationAuditEvent>`.

    GET /audit_tenant_1/_mapping/cafAuditEvent
    {
      "audit_tenant_1": {
        "mappings": {
          "cafAuditEvent": {
            "dynamic_templates": [
              {
                "CAFAuditString": {
                  "match": "*_CAStr",
                  "mapping": {
                    "type": "keyword"
                  }
                }
              },
              {
                "CAFAuditKeyword": {
                  "match": "*_CAKyw",
                  "mapping": {
                    "type": "keyword"
                  }
                }
              },
              {
                "CAFAuditText": {
                  "match": "*_CATxt",
                  "mapping": {
                    "type": "text"
                  }
                }
              },
              {
                "CAFAuditLong": {
                  "match": "*_CALng",
                  "mapping": {
                    "type": "long"
                  }
                }
              },
              {
                "CAFAuditInteger": {
                  "match": "*_CAInt",
                  "mapping": {
                    "type": "integer"
                  }
                }
              },
              {
                "CAFAuditShort": {
                  "match": "*_CASrt",
                  "mapping": {
                    "type": "short"
                  }
                }
              },
              {
                "CAFAuditDouble": {
                  "match": "*_CADbl",
                  "mapping": {
                    "type": "double"
                  }
                }
              },
              {
                "CAFAuditFloat": {
                  "match": "*_CAFlt",
                  "mapping": {
                    "type": "float"
                  }
                }
              },
              {
                "CAFAuditDate": {
                  "match": "*_CADte",
                  "mapping": {
                    "type": "date"
                  }
                }
              },
              {
                "CAFAuditBoolean": {
                  "match": "*_CABln",
                  "mapping": {
                    "type": "boolean"
                  }
                }
              }
            ],
            "properties": {
              "applicationId": {
                "type": "keyword"
              },
              "correlationId": {
                "type": "keyword"
              },
              "eventCategoryId": {
                "type": "keyword"
              },
              "eventOrder": {
                "type": "integer"
              },
              "eventTime": {
                "type": "date"
              },
              "eventTimeSource": {
                "type": "keyword"
              },
              "eventTypeId": {
                "type": "keyword"
              },
              "processId": {
                "type": "keyword"
              },
              "threadId": {
                "type": "keyword"
              },
              "userId": {
                "type": "keyword"
              }
            }
          }
        }
      }
    }

The above JSON, returned from Elasticsearch, shows us the fixed and custom field type mappings for a tenant's index. An audit event's information is stored in fixed fields and the event's parameters are mapped to their appropriate types based on their field name suffixes added by the Audit library.

A tenant application's audit events sent from the client-side library are added that tenant's index.

    GET /audit_tenant_1/cafAuditEvent/_search
    {
      "took": 3,
      "timed_out": false,
      "_shards": {
        "total": 5,
        "successful": 5,
        "failed": 0
      },
      "hits": {
        "total": 2,
        "max_score": 1,
        "hits": [
          {
            "_index": "audit_tenant_1",
            "_type": "cafAuditEvent",
            "_id": "AVuvyhWuI0NChd-OZTz-",
            "_score": 1,
            "_source": {
              "processId": "a040cdab-778d-4634-8b64-4fe4deedaa93",
              "threadId": "1",
              "eventOrder": "1",
              "eventTime": "2016-11-15T14:30:00",
              "eventTimeSource": "HOST1",
              "userId": "JoeBloggs@yourcompany.com",
              "correlationId": "correlation1",
              "eventCategoryId": "documentEvents",
              "eventTypeId": "deleteDocument",
              "applicationId": "DocumentWebServiceApp",
              "docId_CALng": "123456",
              "authorisedBy_CAStr": "JoesphBloggins@yourcompany.com"
            }
          },
          {
            "_index": "audit_tenant_1",
            "_type": "cafAuditEvent",
            "_id": "AVuvySPNI0NChd-OZTzH",
            "_score": 1,
            "_source": {
              "processId": "a040cdab-778d-4634-8b64-4fe4deedaa93",
              "threadId": "1",
              "eventOrder": "0",
              "eventTime": "2016-11-15T14:12:12",
              "eventTimeSource": "HOST1",
              "userId": "JoeBloggs@yourcompany.com",
              "correlationId": "correlation1",
              "eventCategoryId": "documentEvents",
              "eventTypeId": "viewDocument",
              "applicationId": "DocumentWebServiceApp",
              "docId_CALng": "123456"
            }
          }
        ]
      }
    }

The above JSON, returned from Elasticsearch, shows us all of the audit events belonging to a tenant.
