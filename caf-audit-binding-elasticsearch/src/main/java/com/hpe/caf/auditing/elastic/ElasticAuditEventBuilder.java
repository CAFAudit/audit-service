/*
 * Copyright 2015-2024 Open Text.
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
import com.hpe.caf.auditing.AuditIndexingHint;
import com.hpe.caf.auditing.exception.AuditingException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;

public class ElasticAuditEventBuilder implements AuditEventBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticAuditEventBuilder.class.getName());

    private final OpenSearchClient openSearchClient;
    private String tenantId;
    private final Map<String, Object> auditEvent = new HashMap<>();

    public ElasticAuditEventBuilder(OpenSearchClient openSearchClient,
                                    AuditCoreMetadataProvider coreMetadataProvider){
        this.openSearchClient = openSearchClient;
        //  Add fixed audit event fields to Map.
        addCommonFields(coreMetadataProvider);
    }

    private void addCommonFields(AuditCoreMetadataProvider coreMetadataProvider)
    {
        auditEvent.put(ElasticAuditConstants.FixedFieldName.PROCESS_ID_FIELD, coreMetadataProvider.getProcessId().toString());
        auditEvent.put(ElasticAuditConstants.FixedFieldName.THREAD_ID_FIELD, coreMetadataProvider.getThreadId());
        auditEvent.put(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD, coreMetadataProvider.getEventOrder());
        auditEvent.put(ElasticAuditConstants.FixedFieldName.EVENT_TIME_FIELD, coreMetadataProvider.getEventTime().toString());
        auditEvent.put(ElasticAuditConstants.FixedFieldName.EVENT_TIME_SOURCE_FIELD, coreMetadataProvider.getEventTimeSource());
    }

    @Override
    public void setApplication(String applicationId) {
        auditEvent.put(ElasticAuditConstants.FixedFieldName.APPLICATION_ID_FIELD, applicationId);
    }

    @Override
    public void setUser(String userId) {
        auditEvent.put(ElasticAuditConstants.FixedFieldName.USER_ID_FIELD, userId);
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
    }

    @Override
    public void setCorrelationId(String correlationId) {
        auditEvent.put(ElasticAuditConstants.FixedFieldName.CORRELATION_ID_FIELD, correlationId);
    }

    @Override
    public void setEventType(String eventCategoryId, String eventTypeId) {
        auditEvent.put(ElasticAuditConstants.FixedFieldName.EVENT_CATEGORY_ID_FIELD, eventCategoryId);
        auditEvent.put(ElasticAuditConstants.FixedFieldName.EVENT_TYPE_ID_FIELD, eventTypeId);
    }

    @Override
    public void addEventParameter(String name, String columnName, String value) {
        auditEvent.put(getEventParamName(name, columnName).concat(ElasticAuditConstants.CustomFieldSuffix.KEYWORD_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, String value, AuditIndexingHint indexingHint) {
        if (indexingHint != null) {
            switch (indexingHint) {
                case KEYWORD:
                    auditEvent.put(getEventParamName(name, columnName).concat(ElasticAuditConstants.CustomFieldSuffix.KEYWORD_SUFFIX), value);
                    break;

                case FULLTEXT:
                    auditEvent.put(getEventParamName(name, columnName).concat(ElasticAuditConstants.CustomFieldSuffix.TEXT_SUFFIX), value);
                    break;

                default:
                    //  Unexpected indexing hint.
                    String errorMessage = "Unexpected Elasticsearch indexing hint. Expected " + AuditIndexingHint.FULLTEXT + " or " + AuditIndexingHint.FULLTEXT + " but received '" + indexingHint.toString() + "'";
                    LOG.error(errorMessage);
                    throw new RuntimeException(errorMessage);
            }
        } else {
            //  Indexing hint is null.
            auditEvent.put(getEventParamName(name, columnName).concat(ElasticAuditConstants.CustomFieldSuffix.KEYWORD_SUFFIX), value);
        }
    }

    @Override
    public void addEventParameter(String name, String columnName, short value) {
        auditEvent.put(getEventParamName(name, columnName).concat(ElasticAuditConstants.CustomFieldSuffix.SHORT_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, int value) {
        auditEvent.put(getEventParamName(name, columnName).concat(ElasticAuditConstants.CustomFieldSuffix.INT_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, long value) {
        auditEvent.put(getEventParamName(name, columnName).concat(ElasticAuditConstants.CustomFieldSuffix.LONG_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, float value) {
        auditEvent.put(getEventParamName(name, columnName).concat(ElasticAuditConstants.CustomFieldSuffix.FLOAT_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, double value) {
        auditEvent.put(getEventParamName(name, columnName).concat(ElasticAuditConstants.CustomFieldSuffix.DOUBLE_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, boolean value) {
        auditEvent.put(getEventParamName(name, columnName).concat(ElasticAuditConstants.CustomFieldSuffix.BOOLEAN_SUFFIX), value);
    }

    @Override
    public void addEventParameter(String name, String columnName, Date value) {
        auditEvent.put(getEventParamName(name, columnName).concat(ElasticAuditConstants.CustomFieldSuffix.DATE_SUFFIX), value);
    }

    private static String getEventParamName
    (
            final String name,
            final String columnName
    )
    {
        return ((columnName == null) ? name : columnName);
    }

    @Override
    public void send() throws IOException, AuditingException{
        try {


            //  Index audit event message into Elasticsearch.
            final IndexRequest<Map<String, Object>> indexRequest = new IndexRequest.Builder<Map<String, Object>>()
                .index(tenantId.concat(ElasticAuditConstants.Index.SUFFIX))
                .document(auditEvent)
                .build();

            final IndexResponse indexResponse = openSearchClient.index(indexRequest);

            final Result status = indexResponse.result();
            if (status != Result.Created) {
                //  Unexpected response so report this.
                String errorMessage = "Unexpected Elasticsearch response status. Expected 'CREATED' but received '" + status.toString() + "'";
                LOG.error(errorMessage);
                throw new AuditingException(errorMessage);
            }
            LOG.debug("Audit event message successfully indexed in Elasticsearch. Index: " + indexResponse.index() + ", Id: " + indexResponse.id());

        } catch (final IOException e) {
            LOG.error("Error when indexing audit event message " + auditEvent.toString(), e);
            throw e;
        }
    }
}
