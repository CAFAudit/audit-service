# Audit Management Web Service

![Overview](images/overview.png)

 
The CAF Audit Management Web Service API provides a set of endpoints to facilitate the registration of application defined audit events as well as the addition of new tenants.

The **POST /applications** endpoint is used to register audit events used by each application defined in an Audit Event Definition File. This initializes the Vertica database with a set of management tables which are created under a schema named AuditManagement.

The **POST /tenants** endpoint is used to add new tenants. One or more applications can be specified when adding a new tenant. This will result in application specific storage tables being created under tenant specific schemas in the Vertica database. The Vertica schedulers tasked with streaming the data from Apache Kafka into the Vertica database are also configured and launched as part of this operation.

## Deployment

This is available as a Docker container with Apache Tomcat - see [https://github.hpe.com/caf/caf-audit-management-service-container](https://github.hpe.com/caf/caf-audit-management-service-container "caf-audit-management-service-container")