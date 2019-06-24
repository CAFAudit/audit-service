# Production Marathon Deployment

The Production Marathon deployment supports the deployment of the CAF Audit Web Service on Mesos/Marathon. This folder contains the marathon environment and template files that are required to deploy the service application.

## Prerequisites

If you do not have a production ready deployment of Elasticsearch to test the production CAF Audit Web Service deployment against, follow the [production-prereqs/README.md](production-prereqs/README.md). These instructions will guide you on how to setup a non-production instance of Elasticsearch on Mesos/Marathon for the purposes of smoke testing.

## Service Configuration

### Marathon Template
The `marathon.json.b` template file describes the marathon deployment information required for the CAF Audit Web Service. The template file uses property substitution to get values for configurable properties **required** for service deployment. These properties are configured in the marathon environment file. 

### Marathon Environment
The `environment.sh` file supports configurable property settings necessary for service deployment. These include:

- `CAF_AUDIT_SERVICE_PORT`: This property configures the port that the CAF Audit Web Service listens on. 

- `CAF_ELASTIC_HOST_AND_PORT_VALUES`: This setting configures a comma separated list of Elasticsearch HOST:PORT value pairs. e.g. 192.168.56.10:9200,192.168.56.20:9200.

Please note that the CAF Audit Web Service cannot be deployed unless all of the above properties are configured in the marathon environment file.

### Additional Marathon Configuration
The `marathon.json.b` deployment template file specifies default values for a number of additional settings which you may choose to modify directly for your custom deployment. These include:

##### Application CPU, Memory and Instances

- `cpus` : This setting can be used to configure the amount of CPU of each CAF Audit Web Service container. This does not have to be a whole number. Default value: 0.25.

- `mem`: This configures the amount of RAM of each CAF Audit Web Service container. Note that this property does not configure the amount of RAM available to the container but is instead an upper limit. If the container's RAM exceeds this value it will cause docker to destroy and restart the container. Default value: 768.

- `instances`: This setting specifies the number of instances of the CAF Audit Web Service container to start on launch. Default value: 1.

##### Elasticsearch Index Settings

- `CAF_ELASTIC_NUMBER_OF_SHARDS`: This environment setting can be used to configure the number of primary shards that an Elasticsearch index should have. Default value: 5.

- `CAF_ELASTIC_NUMBER_OF_REPLICAS`: This setting configures the number of replica shards (copies) that each primary shard should have. Default value: 1.

##### Logging Level

- `CAF_LOG_LEVEL`: This property setting controls the logging level for the CAF Audit Web Service. The logging levels supported include WARN, ERROR, INFO, TRACE, DEBUG and ALL. Default value: INFO.

## Service Deployment
In order to deploy the service application, issue the following command from the 'production-marathon' directory:

	source ./environment.sh ; \
		cat marathon.json.b \
		| perl -pe 's/\$\{(\w+)\}/(exists $ENV{$1} && length $ENV{$1} > 0 ? $ENV{$1} : "NOT_SET_$1")/eg' \
		| curl -H "Content-Type: application/json" -d @- http://localhost:8080/v2/groups/caf

## Smoke Testing

After the CAF Audit Web Service has been deployed for production use, the smoke testing instructions should be followed to ensure service operation [production-smoke-testing/README.md](production-smoke-testing/README.md).
