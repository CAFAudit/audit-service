/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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

public final class ElasticAuditConstants {

    public final static class ConfigEnvVar {
        public static final String CAF_ELASTIC_HOST_AND_PORT_VALUES = "CAF_ELASTIC_HOST_AND_PORT_VALUES";
        public static final String CAF_ELASTIC_CLUSTER_NAME = "CAF_ELASTIC_CLUSTER_NAME";
        public static final String CAF_ELASTIC_NUMBER_OF_SHARDS = "CAF_ELASTIC_NUMBER_OF_SHARDS";
        public static final String CAF_ELASTIC_NUMBER_OF_REPLICAS = "CAF_ELASTIC_NUMBER_OF_REPLICAS";
    }

    public final static class ConfigDefault {
        public static final String CAF_ELASTIC_CLUSTER_NAME = "elasticsearch-cluster";
        public static final int CAF_ELASTIC_NUMBER_OF_SHARDS = 5;
        public static final int CAF_ELASTIC_NUMBER_OF_REPLICAS = 1;
    }

}
