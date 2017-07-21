# Production Prerequisites

This guide covers the deployment of non-production Elasticsearch on Mesos/Marathon for purposes such as smoke testing.

This folder contains the marathon environment and template files for deployment of non-production Elasticsearch on Mesos/Marathon.

## Elasticsearch Configuration

### Marathon Template

The `marathon-testing-elasticsearch.json.b` template file describes the marathon deployment information required for Elasticsearch. The template file uses property substitution to get values for configurable properties **required** for service deployment. These properties are configured in the environment-testing environment file.

### Marathon Environment

The `environment-testing.sh` file supports configurable property settings necessary for Elasticsearch deployment. These include:

- `CAF_TESTING_ELASTICSEARCH_HTTP_SERVICE_PORT`: This property configures the port that the Elasticsearch HTTP Service is configured to listen on.

- `CAF_TESTING_ELASTICSEARCH_TRANSPORT_SERVICE_PORT`: This property configures the port that the Elasticsearch Transport Service is configured to listen on.

- `CAF_ELASTIC_CLUSTER_NAME`: This configures the name of the Elasticsearch cluster. e.g. audit-smoketest-elasticsearch.

Please note that Elasticsearch cannot be deployed unless all of the above properties are configured in the marathon environment file.

## Elasticsearch Deployment

In order to deploy Elasticsearch, issue the following command from the 'production-marathon/production-prereqs' directory:

	source ./environment-testing.sh ; \
		cat marathon-testing-elasticsearch.json.b \
		| perl -pe 's/\$\{(\w+)\}/(exists $ENV{$1} && length $ENV{$1} > 0 ? $ENV{$1} : "NOT_SET_$1")/eg' \
		| curl -H "Content-Type: application/json" -d @- http://localhost:8080/v2/groups/caf
