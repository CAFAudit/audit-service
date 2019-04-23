# Smoke Testing

This guide covers the deployment and usage of CAF Audit Monkey for smoke testing of production Elasticsearch CAF Audit Web Service via Docker. The CAF Audit Monkey is used to test the logging of audit events directly to Elasticsearch and through the CAF Audit Web Service.

## CAF Audit Web Service Deployment

If you have not already done so, follow the [production-marathon/README.md](../README.md) for deployment of the production CAF Audit Web Service prior to smoke testing.

## CAF Audit Monkey Usage

The Audit Monkey provides the ability to send Audit Events both directly to Elasticsearch and via the CAF Audit Web Service.

Further information on the CAF Audit Monkey can be found [here](https://github.com/CAFAudit/audit-service/tree/develop/caf-audit-monkey).

### Sending Audit Events Direct to Elasticsearch

From your Docker host command-line, run the Audit Monkey sending [2] Audit Events, for Tenant Id [directtestid], [direct] to Elasticsearch in [Standard] mode using [1] thread. Replace the `ES_HOSTNAME`, `ES_PORT` and `ES_CLUSTERNAME` environment variables with the details of the Elasticsearch deployed for smoke testing purposes:

```
docker run -e ES_HOSTNAME=<Elasticsearch_Node> -e ES_PORT=<Elasticsearch_Node_Transport_Port> -e ES_CLUSTERNAME=<Elasticsearch_Cluster_Name> -e CAF_AUDIT_TENANT_ID=directtestid -e CAF_AUDIT_MODE=direct -e CAF_AUDIT_MONKEY_MODE=standard -e CAF_AUDIT_MONKEY_NUM_OF_EVENTS=2 -e CAF_AUDIT_MONKEY_NUM_OF_THREADS=1 cafaudit/audit-monkey:3.3.0
```

#### Verification of Direct to Elasticsearch Audit Events

The following CURL command will return all of the Tenant Id's [directtestid] audit events stored in Elasticsearch. There should be two hits returned:

```
curl --request GET --url 'http://<Elasticsearch_Node>:<Elasticsearch_Node_HTTP_Port>/directtestid_audit/cafAuditEvent/_search?pretty='
```

### Sending Audit Events via CAF Audit Web Service

From your Docker host command-line, run the Audit Monkey sending [2] Audit Events, for Tenant Id [wstestid], through the [Audit Web Service] in [Standard] mode using [1] thread. Replace the `WS_HOSTNAME` and `WS_PORT` environment variables with the details of the CAF Audit Web Service deployed for smoke testing purposes:

```
docker run -e CAF_AUDIT_TENANT_ID=wstestid -e CAF_AUDIT_MODE=webservice -e WS_HOSTNAME=<CAF_Audit_Web_Service_Host> -e WS_PORT=<CAF_Audit_Web_Service_Port> -e CAF_AUDIT_MONKEY_MODE=standard -e CAF_AUDIT_MONKEY_NUM_OF_EVENTS=2 -e CAF_AUDIT_MONKEY_NUM_OF_THREADS=1 cafaudit/audit-monkey:3.3.0
```
#### Verification of Audit Events

The following CURL command will return all of the Tenant Id's [wstestid] audit events stored in Elasticsearch. There should be two hits returned:

```
curl --request GET --url 'http://<Elasticsearch_Node>:<Elasticsearch_Node_HTTP_Port>/wstestid_audit/cafAuditEvent/_search?pretty='
```
