# Production Docker Swarm Deployment

The Production Docker Stack Deployment supports the deployment of the CAF Audit Web Service on Docker Swarm. This folder contains the `docker-stack.yml` file and an `environment.sh` file.

## Service Configuration

### Docker Stack
The `docker-stack.yml` file describes the Docker deployment information required for the CAF Audit Web Service. The file uses property substitution to retrieve values from Environment variables. A number of these Environment variables are **required** for the CAF Audit Web Service deployment. These Environment variables are configurable in the `environment.sh` file.

### Docker Environment
The `environment.sh` file supports configurable property settings necessary for service deployment.
	#!/usr/bin/env bash
	
	###
	# CAF Audit Service 
	###
	export CAF_AUDIT_SERVICE_PORT=25080
	
	###
	# Elasticsearch
	###
	## The protocol used to connect to the Elasticsearch server.
	export CAF_ELASTIC_PROTOCOL=http
	## A comma separated list of Elasticsearch HOST values.
	export CAF_ELASTIC_HOST_VALUES=192.168.56.10
	## The number of primary shards that an Elasticsearch index should have.
	export CAF_ELASTIC_NUMBER_OF_SHARDS=5
	## The number of replica shards (copies) that each primary shard should have.
	export CAF_ELASTIC_NUMBER_OF_REPLICAS=1
	## The REST port of the Elasticsearch server listens on.
	export CAF_ELASTIC_PORT_VALUE=9200
	## Elasticsearch username.
	export CAF_ELASTIC_USERNAME=
	## Elasticsearch password.
	export CAF_ELASTIC_PASSWORD=

The `environment.sh` file specifies default values for the environment variables, however these values may require updating depending on the deployment environment.

#### Deploy

##### Replicas
* `mode` : Either global (exactly one container per swarm node) or replicated (a specified number of containers) (default replicated).
* `replicas` : If the service is replicated (which is the default), specify the number of containers that should be running at any given time.

##### Resources > Limits
* `cpus`: This setting can be used to configure the amount of CPU for each container and prevents the web service inside the container from using more than that specified. This does not have to be a whole number.
* `memory`: This configures the maximum amount of RAM each container can use. If the container's RAM exceeds this value it will cause docker to destroy and restart the container.

##### Resources > Reservations
* `cpus`: This setting can be used to configure the amount of CPU that should be held or reserved for each container. This does not have to be a whole number.
* `memory`: This configures the amount of RAM that should be held or reserved for each container.

##### Update Config
* `parallelism` : The number of containers to update at a time
* `delay` : The time to wait between updating a group of containers

## Execution

To deploy the stack:
* Edit the `environment.sh` to ensure that the CAF Audit Web Service is connected to the correct Elasticsearch cluster.
* Ensure the version of the CAF Audit Web Service in `docker-stack.yml` is the correct version to be deployed
* Execute `source environment.sh`
* Execute `docker stack deploy --compose-file=docker-stack.yml auditWebServiceStack`  
* The CAF Audit Web Service containers will start up

To tear down the stack:
* Execute `docker stack rm auditWebServiceStack`
