# Audit Web Service Container

This is a docker container for the [CAF Audit Web Service](https://github.com/CAFAudit/audit-service/tree/develop/caf-audit-service). It consists of a Tomcat web server that connects to Elasticsearch. It uses the [openSUSE Tomcat base image](https://github.com/CAFapi/opensuse-tomcat-image). [Audit Service Deploy](https://github.com/CAFAudit/audit-service-deploy) can be used to deploy this container on Docker.

### Configuration

#### Environment Variables

##### CAF\_ELASTIC\_HOST\_AND\_PORT\_VALUES
Comma separated list of Elasticsearch HOST:PORT value pairs. e.g. 192.168.56.10:9200,192.168.56.20:9200. Default value is elastic search host value constructed from alternate configurations CAF_ELASTIC_HOST_VALUES and CAF_ELASTIC_PORT_VALUE.

##### CAF\_ELASTIC\_HOST\_VALUES
This is the alternative configuration with comma separated list of Elasticsearch host names. eg. localhost

##### CAF\_ELASTIC\_PORT\_VALUE
This is the alternative configuration for REST port of the Elasticsearch server listens on. e.g 9200.

Note: `CAF_ELASTIC_HOST_AND_PORT_VALUES` will take precedence over `CAF_ELASTIC_HOST_VALUES` and `CAF_ELASTIC_PORT_VALUE` if all three environment variables have values.

##### CAF\_ELASTIC\_NUMBER\_OF\_SHARDS
The number of primary shards that an Elasticsearch index should have. e.g. 5. Default value: 5.

##### CAF\_ELASTIC\_NUMBER\_OF\_REPLICAS
The number of replica shards (copies) that each primary shard should have. e.g. 1. Default value: 1.

##### CAF\_AUDIT\_FORCE\_INDEX\_TEMPLATE\_UPDATE 
Should the index template be updated by force even if it already exists. eg. true. Default value: false.

##### CAF\_AUDIT\_SERVICE\_API\_CONFIG\_PATH

The path to the directory containing the config.properties which can be used as an alternative means of specifying the environment variables. e.g. .\config-props

##### CAF\_LOG\_LEVEL

The logging level for the CAF Audit Web Service. e.g. DEBUG

The Logging levels supported are:

* WARN
* ERROR
* INFO
* TRACE
* DEBUG
* ALL

A default logging level of INFO is applied if this environment variable is not configured.

<br></br>

## Audit Service Links

[Overview](https://cafaudit.github.io/audit-service/pages/en-us/overview)

[Features](https://cafaudit.github.io/audit-service/pages/en-us/Features)

[Getting Started](https://cafaudit.github.io/audit-service/pages/en-us/Getting-Started)

[Web Service](https://cafaudit.github.io/audit-service/pages/en-us/Web-Service)

[Architecture](https://cafaudit.github.io/audit-service/pages/en-us/Architecture)

[API](https://cafaudit.github.io/audit-service/pages/en-us/Client-API)
