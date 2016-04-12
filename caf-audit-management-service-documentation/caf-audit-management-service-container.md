## CAF Audit Management Service Container

This is a docker container for the CAF Audit Management Service. It consists of a Tomcat web server that connects to the Vertica database specified in the Marathon JSON files.
It uses the java:8 base image.

### Configuration

#### Environment Variables

##### CAF_DATABASE_URL
The connection string URL used by the CAF Audit Management Service to connect to the Vertica database. This URL has the format  jdbc:vertica://VerticaHost:portNumber/databaseName

##### CAF_DATABASE_SERVICE_ACCOUNT
The username of the Vertica database account used by the CAF Audit Management Service to access the Vertica database.

##### CAF_DATABASE_SERVICE_ACCOUNT_PASSWORD
The password of the Vertica database account used by the CAF Audit Management Service to access the Vertica database.

##### CAF_DATABASE_LOADER_ACCOUNT
The username of the Vertica database account used to configure the loading of data from Kafka into Vertica.

##### CAF_DATABASE_LOADER_ACCOUNT_PASSWORD
The password of the Vertica database account used to configure the loading of data from Kafka into Vertica.

##### CAF_DATABASE_READER_ROLE
The name of the Vertica database role that will be used for reporting users.

##### CAF_KAFKA_BROKERS
The address (HOST:PORT) of the Kafka broker.

##### CAF_AUDIT_MANAGEMENT_CLI
The Docker image that contains the Audit Management utility for configuring/launching Vertica schedulers. e.g. rh7-artifactory.hpswlabs.hp.com:8443/caf/audit:1.0

##### CAF_MARATHON_URL
The endpoint of Marathon REST API.

##### CAF_AUDIT_SCHEDULER_MARATHON_CPUS
The amount of CPU of each audit scheduler container.

##### CAF_AUDIT_SCHEDULER_MARATHON_MEM
The amount of RAM of each audit scheduler container.

##### CAF_AUDIT_SCHEDULER_MARATHON_CONTAINER_DOCKER_CREDENTIALS
This is the docker login credentials file.

##### CAF_AUDIT_SCHEDULER_MARATHON_CONTAINER_DOCKER_FORCEPULLIMAGE
The forcePullImage setting of each audit scheduler container.

##### CAF_AUDIT_SCHEDULER_MARATHON_CONTAINER_DOCKER_NETWORK
The network type of each audit scheduler container.

##### CAF_AUDIT_MANAGEMENT_DISABLE
This is used to disable the audit management web service.

##### CAF_AUDIT_MANAGEMENT_API_CONFIG_PATH
The path to the directory containing the config.properties which can be used as an alternative means of specifying the environment variables.



