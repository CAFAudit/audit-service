# Audit Web Service Container

This is a docker container for the [CAF Audit Web Service](https://github.com/CAFAudit/audit-service/tree/develop/caf-audit-service). It consists of a Tomcat web server that connects to Elasticsearch. It uses the java:8 base image. [Audit Service Deploy](https://github.com/CAFAudit/audit-service-deploy) can be used to deploy this container on Docker.

### Configuration

#### Environment Variables

##### CAF\_ELASTIC\_HOST\_AND\_PORT
Comma separated list of Elasticsearch HOST:PORT value pairs. e.g. 192.168.56.10:9300,192.168.56.20:9300

##### CAF\_ELASTIC\_CLUSTER\_NAME
Name of the Elasticsearch cluster. e.g. docker-cluster

##### CAF\_ELASTIC\_NUMBER\_OF\_SHARDS
The number of primary shards that an Elasticsearch index should have. e.g. 5

##### CAF\_ELASTIC\_NUMBER\_OF\_REPLICAS
The number of replica shards (copies) that each primary shard should have. e.g. 1

##### CAF\_AUDIT\_SERVICE\_API\_CONFIG\_PATH

The path to the directory containing the config.properties which can be used as an alternative means of specifying the environment variables. e.g. .\config-props

##### CAF\_LOG\_LEVEL

The Logging within the CAF Audit Web Service has been set to a base level of INFO. This logging configuration can be overridden in order to raise or lower the log levels.

The default Log Level can be overridden by setting `CAF_LOG_LEVEL` to a level of your choice and re-running the Audit Web Service

**Note: The value that `CAF_LOG_LEVEL` takes should be in lowercase**


For example: `export CAF_LOG_LEVEL=debug`


## Audit Service Links

[Overview](https://cafaudit.github.io/audit-service/pages/en-us/overview)

[Features](https://cafaudit.github.io/audit-service/pages/en-us/Features)

[Getting Started](https://cafaudit.github.io/audit-service/pages/en-us/Getting-Started)

[Architecture](https://cafaudit.github.io/audit-service/pages/en-us/Architecture)

[API](https://cafaudit.github.io/audit-service/pages/en-us/Client-API)
