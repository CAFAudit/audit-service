## CAF Audit Management Service Container

This is a docker container for the CAF Audit Management Service. It consists of a Tomcat web server that connects to the Vertica database specified in the Marathon JSON files.
It uses the java:8 base image.

### Configuration

#### Environment Variables

##### CAF\_DATABASE\_URL
The connection string URL used by the CAF Audit Management Service to connect to the Vertica database. This URL has the format  jdbc:vertica://VerticaHost:portNumber/databaseName

##### CAF\_DATABASE\_SERVICE\_ACCOUNT
The username of the Vertica database account used by the CAF Audit Management Service to access the Vertica database.

##### CAF\_DATABASE\_SERVICE\_ACCOUNT\_PASSWORD
The password of the Vertica database account used by the CAF Audit Management Service to access the Vertica database.

##### CAF\_DATABASE\_LOADER\_ACCOUNT
The username of the Vertica database account used to configure the loading of data from Kafka into Vertica.

##### CAF\_DATABASE\_LOADER\_ACCOUNT\_PASSWORD
The password of the Vertica database account used to configure the loading of data from Kafka into Vertica.

##### CAF\_DATABASE\_READER\_ROLE
The name of the Vertica database role that will be used for reporting users.

##### CAF\_KAFKA\_BROKERS
The address (HOST:PORT) of the Kafka broker.

##### CAF\_AUDIT\_MANAGEMENT\_DISABLE
This is used to disable the audit management web service.

##### CAF\_AUDIT\_MANAGEMENT\_API\_CONFIG\_PATH
The path to the directory containing the config.properties which can be used as an alternative means of specifying the environment variables.



