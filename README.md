# Vertica Integration with Apache Kafka Container

This is the docker container for the Vertica/Kafka integration [library](https://github.hpe.com/caf/caf-audit-management-cli). It launches a Vertica job scheduler for each tenant created within an application. The scheduler is dedicated to the tenant and is launched as it's own service, independent of the [CAF Audit Management Web Service](https://github.hpe.com/caf/caf-audit-management-service).
