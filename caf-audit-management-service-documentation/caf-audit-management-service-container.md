## CAF Audit Management Service Container

This is a docker container for the CAF Audit Management Service. It consists of a Tomcat web server that connects to the Vertica database specified in the Marathon JSON files.
It uses the java:8 base image.

### Configuration

#### Environment Variables

##### database.url
The connection string URL used by the CAF Audit Management Service to connect to the Vertica database. This URL has the format  jdbc:vertica://VerticaHost:portNumber/databaseName

#### database.username
The username of the Vertica database account used by the CAF Audit Management Service to access the Vertica database.

#### database.password
The password of the Vertica database account used by the CAF Audit Management Service to access the Vertica database.

#### kafka.brokers
The address of the Kafka broker. e.g. 192.168.56.20:9092

#### caf.audit.management.cli
Docker image that contains the Audit Management Utility. e.g. rh7-artifactory.hpswlabs.hp.com:8443/caf-audit:1.0

#### AUDIT\_MANAGEMENT\_API\_CONFIG\_PATH
The path to the directory containing the config.properties which can be used as an alternative means of specifying the database environment variables.
