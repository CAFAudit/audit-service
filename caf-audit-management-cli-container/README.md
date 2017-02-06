# Vertica Integration with Apache Kafka Container

This is the docker container for the Vertica/Kafka integration [library](https://github.hpe.com/caf/audit-service/tree/develop/caf-audit-management-cli). It launches a Vertica job scheduler, a tool for continuous loading of data from Apache Kafka into Vertica. The scheduler is used for all tenants and is launched as it's own service, independent of the [CAF Audit Management Web Service](https://github.hpe.com/caf/audit-service/tree/develop/caf-audit-management-service).
