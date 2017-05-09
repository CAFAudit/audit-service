/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.caf.auditing.elastic;

import com.hpe.caf.api.Configuration;

@Configuration
public class ElasticAuditConfiguration {

    private static String DEFAULT_CLUSTER_NAME = "elasticsearch-cluster";
    private static int DEFAULT_NUMBER_OF_SHARDS = 5;
    private static int DEFAULT_NUMBER_OF_REPLICAS = 1;

    public ElasticAuditConfiguration() {

    }

    //  Comma separated list of Elasticsearch host:port value pairs.
    private String hostAndPortValues;

    //  Name of the cluster. Defaults to "elasticsearch".
    private String clusterName = DEFAULT_CLUSTER_NAME;

    //  The number of primary shards that an index should have. Defaults to 5.
    private int numberOfShards = DEFAULT_NUMBER_OF_SHARDS;

    //  The number of replica shards (copies) that each primary shard should have. Defaults to 1.
    private int numberOfReplicas = DEFAULT_NUMBER_OF_REPLICAS;

    public String getHostAndPortValues() { return hostAndPortValues; }

    public void setHostAndPortValues(String hostAndPortValues) {
        this.hostAndPortValues = hostAndPortValues;
    }

    public String getClusterName() { return (clusterName != null) ? clusterName : DEFAULT_CLUSTER_NAME;}

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public int getNumberOfShards() { return (numberOfShards != 0) ? numberOfShards : DEFAULT_NUMBER_OF_SHARDS; }

    public void setNumberOfShards(int numberOfShards) { this.numberOfShards = numberOfShards; }

    public int getNumberOfReplicas() { return (numberOfReplicas != 0) ? numberOfReplicas : DEFAULT_NUMBER_OF_REPLICAS; }

    public void setNumberOfReplicas(int numberOfReplicas) {
        this.numberOfReplicas = numberOfReplicas;
    }
}
