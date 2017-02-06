# Vertica Integration with Apache Kafka

`caf-audit-management-cli` is a library that supports the launching of a Vertica Job scheduler, a tool for continuous loading of data from Apache Kafka into Vertica.
 
## Deployment

This is available as a Docker container - see [caf-audit-management-cli-container](https://github.hpe.com/caf/audit-service/tree/develop/caf-audit-management-cli-container)

## Usage

The [CAF Audit Management Web Service](https://github.hpe.com/caf/audit-service/tree/develop/caf-audit-management-service) utilizes this library during tenant addition to associate the Kafka topic, which represents the feed of audit event messages with the Vertica job scheduler.