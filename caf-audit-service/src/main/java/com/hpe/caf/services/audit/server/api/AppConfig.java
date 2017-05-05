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
package com.hpe.caf.services.audit.server.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Configuration class for the audit service api. Includes elasticsearch connection properties.
 */
@Configuration
@PropertySource(value = "file:${CAF_AUDIT_SERVICE_API_CONFIG_PATH}/config.properties", ignoreResourceNotFound = true)
public class AppConfig {
    @Autowired
    private Environment environment;

    //  Comma separated list of Elasticsearch host:port value pairs.
    public String getElasticHostAndPort(){
        return environment.getProperty("CAF_ELASTIC_HOST_AND_PORT");
    }

    //  Name of the cluster. Defaults to "elasticsearch".
    public String getElasticClusterName() { return environment.getProperty("CAF_ELASTIC_CLUSTER_NAME"); }

    //  The number of primary shards that an index should have.
    public int getElasticNumberOfShards() {
        final String numberOfShards = environment.getProperty("CAF_ELASTIC_NUMBER_OF_SHARDS");
        if (numberOfShards == null) {
            return 0;
        }
        else {
            return Integer.parseInt(numberOfShards);
        }
    }

    //  The number of replica shards (copies) that each primary shard should have.
    public int getElasticNumberOfReplicas() {
        final String numberOfReplicas = environment.getProperty("CAF_ELASTIC_NUMBER_OF_REPLICAS");
        if (numberOfReplicas == null) {
            return 0;
        }
        else {
            return Integer.parseInt(numberOfReplicas);
        }
    }
}
