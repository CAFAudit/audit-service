# Production Docker Swarm Deployment

The Production Docker Stack Deployment supports the deployment of the CAF Audit Web Service on Docker Swarm. This folder contains the `docker-stack.yml` file and environment file for Elasticsearch.

## Service Configuration

### Docker Stack
The `docker-stack.yml` file describes the Docker deployment information required for the CAF Audit Web Service. The file uses property substitution to retrieve values from Environment variables. A number of these Environment variables are **required** for the CAF Audit Web Service deployment. These Environment variables are configurable in the Elasticsearch environment file.

### Docker Environment
The `elasticsearch.env` file supports configurable property settings necessary for service deployment.
* `CAF_ELASTIC_HOST_AND_PORT` : A comma separated list of Elasticsearch HOST:PORT value pairs  
* `CAF_ELASTIC_CLUSTER_NAME` : The name of the Elasticsearch cluster  
* `CAF_ELASTIC_NUMBER_OF_SHARDS` : The number of primary shards that an Elasticsearch index should have  
* `CAF_ELASTIC_NUMBER_OF_REPLICAS` : The number of replica shards (copies) that each primary shard should have  

### Additional Docker Configuration
The `docker-stack.yml` file specifies default values for a number of additional settings which you may choose to modify directly for your custom deployment. These include:  

#### Deploy

##### Restart Policy
* `condition` : One of none, on-failure or any.
* `delay` : How long to wait between restart attempts, specified as a duration.
* `max_attempts` : How many times to attempt to restart a container before giving up.
* `window` : How long to wait before deciding if a restart has succeeded, specified as a duration.

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
* Edit `elasticsearch.env` to ensure that the CAF Audit Web Service is connected to the correct Elasticsearch cluster
  * CAF\_ELASTIC\_HOST\_AND\_PORT=<ELASTICSEARCH\_HOST\_AND\_PORT> 
  * CAF\_ELASTIC\_CLUSTER\_NAME=<ELASTICSEARCH\_CLUSTER\_NAME>
* Edit `docker-stack.yml` as necessary to update the properties as required
  * Ensure the version of the CAF Audit Web Service is correctly set
* Execute `docker stack deploy --compose-file=docker-stack.yml auditWebServiceStack`  
* The CAF Audit Web Service containers will start up

To tear down the stack:
* Execute `docker stack rm auditWebServiceStack`