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

    public ElasticAuditConfiguration() {

    }

    //  Comma separated list of Elasticsearch host:port value pairs.
    private String hostAndPortValues;

    //  Name of the cluster. Defaults to "elasticsearch".
    private String clusterName = "elasticsearch";

    //  The number of primary shards that an index should have. Defaults to 5.
    private int numberOfShards = 5;

    //  The number of replica shards (copies) that each primary shard should have. Defaults to 1.
    private int numberOfReplicas = 1;

    public String getHostAndPortValues() { return hostAndPortValues; }

    public void setHostAndPortValues(String hostAndPortValues) {
        this.hostAndPortValues = hostAndPortValues;
    }

    public String getClusterName() { return clusterName; }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public int getNumberOfShards() { return numberOfShards; }

    public void setNumberOfShards(int numberOfShards) {
        this.numberOfShards = numberOfShards;
    }

    public int getNumberOfReplicas() { return numberOfReplicas; }

    public void setNumberOfReplicas(int numberOfReplicas) {
        this.numberOfReplicas = numberOfReplicas;
    }
}
