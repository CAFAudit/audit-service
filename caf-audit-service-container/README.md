# Audit Web Service Container

This is a docker container for the [CAF Audit Web Service](https://github.com/CAFAudit/audit-service/tree/develop/caf-audit-service). It consists of a Tomcat web server that connects to Elasticsearch. It uses the java:8 base image. [Audit-service-deploy](https://github.com/CAFAudit/audit-service-deploy) can be used to deploy this container on Docker.

### Configuration

#### Environment Variables

##### CAF\_ELASTIC\_HOST\_AND\_PORT
Comma separated list of Elasticsearch HOST:PORT value pairs.

##### CAF\_ELASTIC\_CLUSTER\_NAME
Name of the Elasticsearch cluster.

##### CAF\_ELASTIC\_NUMBER\_OF\_SHARDS
The number of primary shards that an Elasticsearch index should have.

##### CAF\_ELASTIC\_NUMBER\_OF\_REPLICAS
The number of replica shards (copies) that each primary shard should have.

##### CAF\_AUDIT\_SERVICE\_API\_CONFIG\_PATH

The path to the directory containing the config.properties which can be used as an alternative means of specifying the environment variables.

## Audit Service Links

[Overview](https://cafaudit.github.io/audit-service/pages/en-us/overview)

[Features](https://cafaudit.github.io/audit-service/pages/en-us/Features)

[Getting Started](https://cafaudit.github.io/audit-service/pages/en-us/Getting-Started)

[Architecture](https://cafaudit.github.io/audit-service/pages/en-us/Architecture)

[API](https://cafaudit.github.io/audit-service/pages/en-us/Client-API)
