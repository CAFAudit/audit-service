#!/usr/bin/env bash

###
# CAF Audit Service 
###
export CAF_AUDIT_SERVICE_PORT=25080

###
# Elasticsearch
###
## A comma separated list of Elasticsearch HOST:PORT value pairs.
export CAF_ELASTIC_HOST_AND_PORT=192.168.56.10:9300
## The name of the Elasticsearch cluster.
export CAF_ELASTIC_CLUSTER_NAME=elasticsearch-cluster
## The number of primary shards that an Elasticsearch index should have.
export CAF_ELASTIC_NUMBER_OF_SHARDS=5
## The number of replica shards (copies) that each primary shard should have.
export CAF_ELASTIC_NUMBER_OF_REPLICAS=1
