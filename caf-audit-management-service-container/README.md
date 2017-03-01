# Audit Management Web Service Container

This docker image contains the [CAF Audit Management Web Service](https://github.hpe.com/caf/audit-service/tree/develop/caf-audit-management-service) hosted in Apache Tomcat 8.

The supplied Marathon [properties](https://github.hpe.com/caf/chateau/blob/develop/services/audit-management/properties.json) can be used to deploy this container on Mesosphere’s [Marathon](https://mesosphere.github.io/marathon/) orchestration platform.

To set up Apache Kafka click [here](https://github.hpe.com/caf/audit-service/blob/develop/caf-audit-management-service-container/documentation/apache-kafka.md).

To see how the Audit Events File is used to create database schemas click [here](https://github.hpe.com/caf/audit-service/blob/develop/caf-audit-management-service-container/documentation/auditing-database-tables.md).

For Audit Reporting click [here](https://github.hpe.com/caf/audit-reporting).

## Deploying the Audit Management Web Service

### Docker Images

Download docker image for the CAF Audit Management Service from Artifactory:

docker pull rh7-artifactory.svs.hpeswlab.net:8443/caf/audit-management-service:1.4

### Marathon Loader

Download the marathon-loader artifact from [Artifactory](http://rh7-artifactory.svs.hpeswlab.net:8081/artifactory/libs-release-local/com/hpe/caf/marathon-loader/2.8.0-5/marathon-loader-2.8.0-5-jar-with-dependencies.jar).

The marathon application loader is used to start the Audit Management Web Service docker container.

### Audit Management Web Service Container

#### Configuration

See the configuration for CAF Audit Management Service [here](https://github.hpe.com/caf/chateau/tree/develop/services/audit-management).

##### marathon-properties.json

Use the [marathon-properties.json](https://github.hpe.com/caf/chateau/blob/develop/services/audit-management/properties.json) file with marathon loader to deploy the audit-management-web-service. 


#### Launch

Copy the container configuration marathon template folder (i.e. [marathon-template-json](https://github.hpe.com/caf/chateau/tree/develop/services/audit-management)) and the corresponding [marathon-properties.json](https://github.hpe.com/caf/chateau/blob/develop/services/audit-management/properties.json) file to the same folder containing the marathon application loader artifact.

To start the Audit Management Web Service container, run the marathon application loader with:

java -jar marathon-loader-2.8.0-5-jar-with-dependencies.jar -m "./marathon-template-json" -v "./marathon-properties.json" -e http://localhost:8080 -mo "./marathon-config"

where -e is used to specify the Marathon endpoint

This will launch the container which includes both the Web Service and UI.

## Feature Testing
The testing for Auditing is defined [here](testcases)