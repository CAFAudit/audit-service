# Audit Management Web Service Container

This docker image contains the [CAF Audit Management Web Service](https://github.hpe.com/caf/caf-audit-management-service) hosted in Apache Tomcat 8.

The supplied Marathon [properties](https://github.hpe.com/caf/caf-audit-management-service-container/blob/develop/configuration/marathon-properties.json) can be used to deploy this container on Mesosphere’s [Marathon](https://mesosphere.github.io/marathon/) orchestration platform.

To set up Apache Kafka click [here](https://github.hpe.com/caf/caf-audit-management-service-container/blob/develop/documentation/apache-kafka.md).

To see how the Audit Events File is used to create database schemas click [here](https://github.hpe.com/caf/caf-audit-management-service-container/blob/develop/documentation/auditing-database-tables.md).

For Audit Reporting click [here](https://github.hpe.com/caf/audit-reporting).

## Deploying the Audit Management Web Service

### Docker Images

Download docker images for both the CAF Audit Management Service and the Audit Management utility for configuring/launching Vertica schedulers from Artifactory:

docker pull rh7-artifactory.hpswlabs.hp.com:8443/caf/audit-management-service:1.3

docker pull rh7-artifactory.hpswlabs.hp.com:8443/caf/audit:1.0

### Marathon Loader

Download the marathon-loader artifact from Nexus or Artifactory:

repository: [http://cmbg-maven.autonomy.com/nexus/content/repositories/releases/](http://cmbg-maven.autonomy.com/nexus/content/repositories/releases/)

repository mirror: http://rh7-artifactory.hpswlabs.hp.com:8081/artifactory/policyengine-release/

groupId: com.hpe.caf
artifactId: marathon-loader
version: 2.1
classifier: jar-with-dependencies

The marathon application loader is used to start the Audit Management Web Service docker container.

### Audit Management Web Service Container

#### Configuration

See the configuration for CAF Audit Management Service [here](https://github.hpe.com/caf/caf-audit-management-service-container/blob/develop/configuration/marathon-properties.md).

##### marathon-properties.json

Use the [marathon-properties.json](https://github.hpe.com/caf/caf-audit-management-service-container/blob/develop/configuration/marathon-properties.json) file with marathon loader to deploy the audit-management-web-service. 


#### Launch

Copy the container configuration marathon template folder (i.e. [marathon-template-json](https://github.hpe.com/caf/caf-audit-management-service-container/tree/develop/configuration)) and the corresponding [marathon-properties.json](https://github.hpe.com/caf/caf-audit-management-service-container/blob/develop/configuration/marathon-properties.json) file to the same folder containing the marathon application loader artifact.

To start the Audit Management Web Service container, run the marathon application loader with:

java -jar marathon-loader-2.1-jar-with-dependencies.jar -m "./marathon-template-json" -v "./marathon-properties.json" -e http://localhost:8080 -mo "./marathon-config"

where -e is used to specify the Marathon endpoint

This will launch the container which includes both the Web Service and UI.
