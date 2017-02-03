# Auditing Database Tables

### Vertica Analytic Database

A vagrant file is available for quickly provisioning a Vertica environment - see https://github.hpe.com/caf/vagrant-vertica

### Application Audit Events File

When registering the audit events XML file using the Audit Management Web Service, the XML is recorded in a table named ApplicationEvents under the AuditManagement schema.

### Adding a New Tenant

Every time a new tenant is added, a new row is inserted into the TenantApplications table under the AuditManagement schema. This table records the mappings between tenants and application specific audit events XML. Two new schemas for the tenant are also created in the Vertica database:

#### account_&lt;tenantid&gt;

Created with the tenant identifier using the format account_&lt;tenantid&gt;, this schema contains tables for storing audit event message data and any rejections as part of the Vertica integration with Apache Kafka:

| Table | Description |
|----------|---------------|
| audit&lt;applicationid&gt; | Created with the application identifier using the format audit&lt;applicationid&gt;, this table is used to store the audit event message data sent to Apache Kafka from the client-side auditing library. |
| kafka_rej | Vertica stores any rejections in the kafka_rej table |

#### auditscheduler

A Kafka Vertica job scheduler should be configured and launched before new tenants are added to the system. It should be named 'auditscheduler'. The schema for the scheduler has the following tables:

| Table | Description |
|----------|--------------|
| kafka_clusters | Lists Kafka clusters and their component brokers. |
| kafka_events | Internal log table. kafka_events includes the log level, the target, topic, or partition information that relates to the log message, and any errors that occurred during micro-batch executions. |
| kafka_lock | Displays information on the current Kafka lock status, which allows only one scheduler to run at a time in this schema. |
| kafka_offsets | Controls the offset information for each triple and stores information about the progress within the stream for each topic or partition. |
| kafka_scheduler | Holds the metadata related to the active scheduler for this schema. |
| kafka_scheduler_history | Shows the history of launched scheduler instances. |
| kafka_targets | Contains the metadata for all Vertica target tables, along with their respective rejection tables. Kafka_ targets also holds the COPY information necessary for the corresponding micro-batch. Because the target, not the Kafka topic, determine batches, each row in this table corresponds to a separate microbatch. |
