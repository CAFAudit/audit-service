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

    //  Index suffix and type details.
    public final static class Index {
        public static final String SUFFIX = "_audit";
        public static final String TYPE_MAPPING_RESOURCE = "CafAuditEventTenantIndexMappings.json";
    }

    //  Fixed field names.
    public final static class FixedFieldName {
        public static final String APPLICATION_ID_FIELD = "applicationId";
        public static final String PROCESS_ID_FIELD = "processId";
        public static final String THREAD_ID_FIELD = "threadId";
        public static final String EVENT_ORDER_FIELD = "eventOrder";
        public static final String EVENT_TIME_FIELD = "eventTime";
        public static final String EVENT_TIME_SOURCE_FIELD = "eventTimeSource";
        public static final String USER_ID_FIELD = "userId";
        public static final String TENANT_ID_FIELD = "tenantId";
        public static final String CORRELATION_ID_FIELD = "correlationId";
        public static final String EVENT_TYPE_ID_FIELD = "eventTypeId";
        public static final String EVENT_CATEGORY_ID_FIELD = "eventCategoryId";
    }

    //  Suffixes appended to custom fields in the Elasticsearch index.
    public final static class CustomFieldSuffix{
        public static final String KEYWORD_SUFFIX = "_CAKyw";
        public static final String TEXT_SUFFIX = "_CATxt";
        public static final String SHORT_SUFFIX = "_CAShort";
        public static final String INT_SUFFIX = "_CAInt";
        public static final String LONG_SUFFIX = "_CALng";
        public static final String FLOAT_SUFFIX = "_CAFlt";
        public static final String DOUBLE_SUFFIX = "_CADbl";
        public static final String BOOLEAN_SUFFIX = "_CABln";
        public static final String DATE_SUFFIX = "_CADte";
    }

    public final static class ConfigEnvVar {
        public static final String CAF_ELASTIC_HOST_AND_PORT_VALUES = "CAF_ELASTIC_HOST_AND_PORT_VALUES";
        public static final String CAF_ELASTIC_NUMBER_OF_SHARDS = "CAF_ELASTIC_NUMBER_OF_SHARDS";
        public static final String CAF_ELASTIC_NUMBER_OF_REPLICAS = "CAF_ELASTIC_NUMBER_OF_REPLICAS";
    }

    public final static class ConfigDefault {
        public static final int CAF_ELASTIC_NUMBER_OF_SHARDS = 5;
        public static final int CAF_ELASTIC_NUMBER_OF_REPLICAS = 1;
    }

}
