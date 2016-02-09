## CAF Audit Management Service Container

This is a docker container for the CAF Audit Management Service. It consists of a Tomcat web server that connects to the Vertica database specified in the Marathon JSON files.
It uses the java:8 base image.

### Configuration

#### Environment Variables

##### database.url
The connection string URL used by the CAF Audit Management Service to connect to the Vertica database. This URL has the format  jdbc:vertica://VerticaHost:portNumber/databaseName

#### database.schema
The name of the Vertica database logical schema in which CAF Audit applications are created and managed.

#### database.username
The username of the Vertica database account used by the CAF Audit Management Service to access the Vertica database.

#### database.password
The password of the Vertica database account used by the CAF Audit Management Service to access the Vertica database.

#### AUDIT\_MANAGEMENT\_API\_CONFIG\_PATH
The path to the directory containing the config.properties which can be used as an alternative means of specifying the database environment variables.
