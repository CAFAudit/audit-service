#!/usr/bin/env bash

###
# CAF Audit Service 
###
export CAF_AUDIT_SERVICE_PORT=25080

###
# Elasticsearch
###
## The protocol used to connect to the Elasticsearch server.
export CAF_ELASTIC_PROTOCOL=http
## A comma separated list of Elasticsearch HOST values.
export CAF_ELASTIC_HOST_VALUES=192.168.56.10
## The number of primary shards that an Elasticsearch index should have.
export CAF_ELASTIC_NUMBER_OF_SHARDS=5
## The number of replica shards (copies) that each primary shard should have.
export CAF_ELASTIC_NUMBER_OF_REPLICAS=1
## The REST port of the Elasticsearch server listens on.
export CAF_ELASTIC_PORT_VALUE=9200
## ElasticSearch username.
export CAF_ELASTIC_USERNAME=
## ElasticSearch password.
export CAF_ELASTIC_PASSWORD=
