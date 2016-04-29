# Audit Management Web Service

![Overview](images/overview.png)

In order to use CAF Auditing in an application, the auditing events that the application uses must be specified along with the parameters that are associated with each of the events in an [Audit Event Definition File](https://github.hpe.com/caf/caf-audit-schema/blob/develop/README.md).

The CAF Audit Management Web Service API provides a set of endpoints to facilitate the registration of these application defined audit events and the creation of tenants within an application.

The **POST /applications** endpoint is used to register audit events used by each application defined in the Audit Event Definition File. This initializes the Vertica database with a set of management tables which are created under a schema named AuditManagement.

The **POST /tenants** endpoint is used to add new tenants. One or more applications can be specified when adding a new tenant. This will result in application specific storage tables being created under tenant specific schemas in the Vertica database. The Vertica schedulers tasked with streaming the data from Apache Kafka into the Vertica database are also configured and launched as part of this operation.

## Deployment

This is available as a Docker container with Apache Tomcat - see [caf-audit-management-service-container](https://github.hpe.com/caf/caf-audit-management-service-container)

## Usage

To start using the web service, the endpoints can be exercised by accessing the Web UI at the following URL:

	http://<docker.ip.address>:<port>/caf-audit-management-ui

Replace `<docker.ip.address>` and `<port>` as necessary.