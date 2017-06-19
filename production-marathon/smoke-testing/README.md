# Deployment for Smoke Testing

This guide covers the deployment of Elasticsearch with the CAF Audit Web Service and CAF Audit Monkey for smoke testing. The CAF Audit Monkey is used to test the logging of audit events directly to Elastic and through the CAF Audit Web Service.

This folder contains the marathon environment and template files that are required to deploy Elasticsearch for smoke testing of the CAF Audit Web Service on Mesos/Marathon.

## Elasticsearch Configuration

### Marathon Template

The `marathon-testing-elasticsearch.json.b` template file describes the marathon deployment information required for Elasticsearch. The template file uses property substitution to get values for configurable properties **required** for service deployment. These properties are configured in the marathon-testing environment file. 

### Marathon Environment

The `marathon-testing.env` file supports configurable property settings necessary for Elasticsearch deployment. These include:

- `CAF_TESTING_ELASTICSEARCH_HTTP_SERVICE_PORT`: This property configures the port that the Elasticsearch HTTP Service is configured to listen on. 

- `CAF_TESTING_ELASTICSEARCH_TRANSPORT_SERVICE_PORT`: This property configures the port that the Elasticsearch Transport Service is configured to listen on. 

- `CAF_ELASTIC_CLUSTER_NAME`: This configures the name of the Elasticsearch cluster. e.g. audit-smoketest-elasticsearch. 

Please note that Elasticsearch cannot be deployed unless all of the above properties are configured in the marathon environment file.

## Elasticsearch Deployment

In order to deploy Elasticsearch, issue the following command from the 'production-marathon/smoke-testing' directory:

	source ./marathon-testing.env ; \
		cat marathon-testing-elasticsearch.json.b \
		| perl -pe 's/\$\{(\w+)\}/(exists $ENV{$1} && length $ENV{$1} > 0 ? $ENV{$1} : "NOT_SET_$1")/eg' \
		| curl -H "Content-Type: application/json" -d @- http://localhost:8080/v2/groups/caf

## CAF Audit Web Service Deployment

After Elasticsearch has started follow the [production-marathon/README.md](../README.md) for deployment of the CAF Audit Web Service and configure the `marathon.env` properties to match with the Elasticsearch deployed for smoke testing.

## CAF Audit Monkey Usage

The Audit Monkey provides the ability to send Audit Events both directly to Elasticsearch and via the CAF Audit Web Service.

Further information on the CAF Audit Monkey can be found [here](https://github.com/CAFAudit/audit-service/tree/develop/caf-audit-monkey).

### Sending Audit Events Direct to Elasticsearch

From your Docker host command-line, run the Audit Monkey sending [2] Audit Events, for Tenant Id [directtestid], [direct] to Elasticsearch in [Standard] mode using [1] thread. Replace the `ES_HOSTNAME`, `ES_PORT` and `ES_CLUSTERNAME` environment variables with the details of the Elasticsearch deployed for smoke testing purposes:

```
docker run -e ES_HOSTNAME=<Elasticsearch_Node> -e ES_PORT=<Elasticsearch_Node_Transport_Port> -e ES_CLUSTERNAME=<Elasticsearch_Cluster_Name> -e CAF_AUDIT_TENANT_ID=directtestid -e CAF_AUDIT_MODE=direct -e CAF_AUDIT_MONKEY_MODE=standard -e CAF_AUDIT_MONKEY_NUM_OF_EVENTS=2 -e CAF_AUDIT_MONKEY_NUM_OF_THREADS=1 cafaudit/audit-monkey:3.2.0
```

#### Verification of Direct to Elasticsearch Audit Events

The following CURL command will return all of the Tenant Id's [directtestid] audit events stored in Elasticsearch. There should be two hits returned:

```
curl --request GET --url 'http://<Elasticsearch_Node>:<Elasticsearch_Node_HTTP_Port>/directtestid_audit/cafAuditEvent/_search?pretty='
```

### Sending Audit Events via CAF Audit Web Service

From your Docker host command-line, run the Audit Monkey sending [2] Audit Events, for Tenant Id [wstestid], through the [Audit Web Service] in [Standard] mode using [1] thread. Replace the `WS_HOSTNAME` and `WS_PORT` environment variables with the details of the CAF Audit Web Service deployed for smoke testing purposes:

```
docker run -e CAF_AUDIT_TENANT_ID=wstestid -e CAF_AUDIT_MODE=webservice -e WS_HOSTNAME=<CAF_Audit_Web_Service_Host> -e WS_PORT=<CAF_Audit_Web_Service_Port> -e CAF_AUDIT_MONKEY_MODE=standard -e CAF_AUDIT_MONKEY_NUM_OF_EVENTS=2 -e CAF_AUDIT_MONKEY_NUM_OF_THREADS=1 cafaudit/audit-monkey:3.2.0
```
#### Verification of Audit Events

The following CURL command will return all of the Tenant Id's [wstestid] audit events stored in Elasticsearch. There should be two hits returned:

```
curl --request GET --url 'http://<Elasticsearch_Node>:<Elasticsearch_Node_HTTP_Port>/wstestid_audit/cafAuditEvent/_search?pretty='
```
