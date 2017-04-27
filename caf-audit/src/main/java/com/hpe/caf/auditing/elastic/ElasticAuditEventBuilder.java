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

import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.rest.RestStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ElasticAuditEventBuilder implements AuditEventBuilder {

    private static final Logger LOG = LogManager.getLogger(ElasticAuditEventBuilder.class.getName());

    private static final String ES_INDEX_PREFIX = "audit_tenant_";
    private static final String ES_TYPE = "cafAuditEvent";

    private static final String PROCESS_ID_FIELD = "processId";
    private static final String THREAD_ID_FIELD = "threadId";
    private static final String EVENT_ORDER_FIELD = "eventOrder";
    private static final String EVENT_TIME_FIELD = "eventTime";
    private static final String EVENT_TIME_SOURCE_FIELD = "eventTimeSource";
    private static final String APP_ID_FIELD = "applicationId";
    private static final String USER_ID_FIELD = "userId";
    private static final String CORRELATION_ID_FIELD = "correlationId";
    private static final String EVENT_CATEGORY_ID_FIELD = "eventCategoryId";
    private static final String EVENT_TYPE_ID_FIELD = "eventTypeId";

    private static final String KEYWORD_SUFFIX = "_AKyw";
    private static final String SHORT_SUFFIX = "_ASrt";
    private static final String INT_SUFFIX = "_AInt";
    private static final String LONG_SUFFIX = "_ALng";
    private static final String FLOAT_SUFFIX = "_AFlt";
    private static final String DOUBLE_SUFFIX = "_ADbl";
    private static final String BOOLEAN_SUFFIX = "_ABln";
    private static final String DATE_SUFFIX = "_ADte";

    private final TransportClient transportClient;
    private final ElasticAuditIndexManager indexManager;
    private String tenantId;
    private final Map<String, Object> auditEvent = new HashMap<>();

    public ElasticAuditEventBuilder(TransportClient transportClient, AuditCoreMetadataProvider coreMetadataProvider, ElasticAuditIndexManager indexManager){
        this.transportClient = transportClient;
        this.indexManager = indexManager;

        //  Add fixed audit event fields to Map.
        addCommonFields(coreMetadataProvider);
    }

    private void addCommonFields(AuditCoreMetadataProvider coreMetadataProvider)
    {
        auditEvent.put(PROCESS_ID_FIELD.concat(KEYWORD_SUFFIX), coreMetadataProvider.getProcessId().toString());
        auditEvent.put(THREAD_ID_FIELD.concat(LONG_SUFFIX), coreMetadataProvider.getThreadId());
        auditEvent.put(EVENT_ORDER_FIELD.concat(LONG_SUFFIX), coreMetadataProvider.getEventOrder());
        auditEvent.put(EVENT_TIME_FIELD.concat(DATE_SUFFIX), coreMetadataProvider.getEventTime().toString());
        auditEvent.put(EVENT_TIME_SOURCE_FIELD.concat(KEYWORD_SUFFIX), coreMetadataProvider.getEventTimeSource());
    }

    @Override
    public void setApplication(String applicationId) {
        auditEvent.put(APP_ID_FIELD.concat(KEYWORD_SUFFIX), applicationId);
    }

    @Override
    public void setUser(String userId) {
        auditEvent.put(USER_ID_FIELD.concat(KEYWORD_SUFFIX), userId);
    }

    @Override
    public void setTenant(String tenantId){
        //  The tenant identifier is used as part of the Elasticsearch index name.
        //  There are restrictions around the naming of the index including it must be lowercase
        //  and not contain commas.
        if(tenantId.contains(",")) {
            //  Report invalid comma usage.
            String errorMessage = "Invalid characters (i.e commas) in the tenant identifier: " + tenantId;
            LOG.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.tenantId = tenantId.toLowerCase();

        // Create Elasticsearch index for the specified tenant.
        indexManager.getIndex(ES_INDEX_PREFIX.concat(this.tenantId));
    }

    @Override
    public void setCorrelationId(String correlationId) {
        auditEvent.put(CORRELATION_ID_FIELD.concat(KEYWORD_SUFFIX), correlationId);
    }

    @Override
    public void setEventType(String eventCategoryId, String eventTypeId) {
        auditEvent.put(EVENT_CATEGORY_ID_FIELD.concat(KEYWORD_SUFFIX), eventCategoryId);
        auditEvent.put(EVENT_TYPE_ID_FIELD.concat(KEYWORD_SUFFIX), eventTypeId);
    }

    @Override
    public void addEventParameter(String name, String columnName, String value) {
        // Append _AKyw suffix to field name for 'string' types.
        auditEvent.put(columnName.concat(KEYWORD_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, short value) {
        // Append _ASrt suffix to field name for 'short' types.
        auditEvent.put(columnName.concat(SHORT_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, int value) {
        // Append _AInt suffix to field name for 'int' types.
        auditEvent.put(columnName.concat(INT_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, long value) {
        // Append _ALng suffix to field name for 'long' types.
        auditEvent.put(columnName.concat(LONG_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, float value) {
        // Append _AFlt suffix to field name for 'float' types.
        auditEvent.put(columnName.concat(FLOAT_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, double value) {
        // Append _ADbl suffix to field name for 'double' types.
        auditEvent.put(columnName.concat(DOUBLE_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, boolean value) {
        // Append _ABln suffix to field name for 'boolean' types.
        auditEvent.put(columnName.concat(BOOLEAN_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, Date value) {
        // Append _ADte suffix to field name for 'date' types.
        auditEvent.put(columnName.concat(DATE_SUFFIX), value);
    }

    @Override
    public void send() throws Exception {
        try {
            //  Index audit event message into Elasticsearch.
            final IndexResponse indexResponse = transportClient
                    .prepareIndex(ES_INDEX_PREFIX + tenantId, ES_TYPE)
                    .setSource(auditEvent)
                    .get();

            final RestStatus status = indexResponse.status();
            if (status != RestStatus.CREATED) {
                //  Unexpected response so report this.
                String errorMessage = "Unexpected Elasticsearch response status. Expected 'CREATED' but received '" + status.toString() + "'";
                LOG.error(errorMessage);
                throw new Exception(errorMessage);
            }
            LOG.debug("Audit event message successfully indexed in Elasticsearch. Index: " + indexResponse.getIndex() + ", Type: " + indexResponse.getType() + ", Id: " + indexResponse.getId());

        } catch (Exception e) {
            LOG.error("Error when indexing audit event message " + auditEvent.toString(), e);
            throw e;
        }
    }
}
