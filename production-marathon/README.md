# Production Marathon Deployment

The Production Marathon deployment supports the deployment of the CAF Audit Web Service on Mesos/Marathon. This folder contains the marathon environment and template files that are required to deploy the service application.

## Service Configuration

### Marathon Template
The `marathon.json.b` template file describes the marathon deployment information required for the CAF Audit Web Service. The template file uses property substitution to get values for configurable properties **required** for service deployment. These properties are configured in the marathon environment file. 

### Marathon Environment
The `marathon.env` file supports configurable property settings necessary for service deployment. These include:

- `DOCKER_REGISTRY`: This setting configures the docker repository that the CAF Audit Web Service image will be pulled from. 

- `CAF_AUDIT_SERVICE_PORT`: This property configures the port that the CAF Audit Web Service listens on. 

- `CAF_ELASTIC_HOST_AND_PORT`: This setting configures a comma separated list of Elasticsearch HOST:PORT value pairs. e.g. 192.168.56.10:9300,192.168.56.20:9300.

- `CAF_ELASTIC_CLUSTER_NAME`: This configures the name of the Elasticsearch cluster. e.g. elasticsearch. 

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

	source ./marathon.env ; \
		cat marathon.json.b \
		| perl -pe 's/\$\{(\w+)\}/(exists $ENV{$1} && length $ENV{$1} > 0 ? $ENV{$1} : "NOT_SET_$1")/eg' \
		| curl -H "Content-Type: application/json" -d @- http://localhost:8080/v2/groups/
