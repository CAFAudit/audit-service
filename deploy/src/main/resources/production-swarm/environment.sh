#!/usr/bin/env bash

###
# CAF Audit Service 
###
export CAF_AUDIT_SERVICE_PORT=25080

###
# Elasticsearch
###
## A comma separated list of Elasticsearch HOST:PORT value pairs.
export CAF_ELASTIC_HOST_AND_PORT_VALUES=192.168.56.10:9200
## The number of primary shards that an Elasticsearch index should have.
export CAF_ELASTIC_NUMBER_OF_SHARDS=5
## The number of replica shards (copies) that each primary shard should have.
export CAF_ELASTIC_NUMBER_OF_REPLICAS=1
