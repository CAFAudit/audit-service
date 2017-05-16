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
package com.hpe.caf.auditing.webserviceclient;

import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.elastic.ElasticAuditConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class WebserviceClientAuditEventBuilder implements AuditEventBuilder {

    private static final Logger LOG = LogManager.getLogger(WebserviceClientAuditEventBuilder.class.getName());

    private final String webserviceHostAndPort;

    private final Map<String, Object> auditEventCommonFields = new HashMap<>();

    private final List<EventParam> auditEventParams = new ArrayList<>();

    public WebserviceClientAuditEventBuilder(final String webserviceHostAndPort,
                                             final AuditCoreMetadataProvider coreMetadataProvider) {
        this.webserviceHostAndPort = webserviceHostAndPort;

        //  Add fixed audit event fields to Map.
        addCommonFields(coreMetadataProvider);
    }

    private void addCommonFields(AuditCoreMetadataProvider coreMetadataProvider)
    {
        auditEventCommonFields.put(ElasticAuditConstants.FixedFieldName.PROCESS_ID_FIELD, coreMetadataProvider.getProcessId().toString());
        auditEventCommonFields.put(ElasticAuditConstants.FixedFieldName.THREAD_ID_FIELD, coreMetadataProvider.getThreadId());
        auditEventCommonFields.put(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD, coreMetadataProvider.getEventOrder());
        auditEventCommonFields.put(ElasticAuditConstants.FixedFieldName.EVENT_TIME_FIELD, coreMetadataProvider.getEventTime().toString());
        auditEventCommonFields.put(ElasticAuditConstants.FixedFieldName.EVENT_TIME_SOURCE_FIELD, coreMetadataProvider.getEventTimeSource());
    }

    @Override
    public void setApplication(String applicationId) {
        auditEventCommonFields.put(ElasticAuditConstants.FixedFieldName.APPLICATION_ID_FIELD, applicationId);
    }

    @Override
    public void setUser(String userId) {
        auditEventCommonFields.put(ElasticAuditConstants.FixedFieldName.USER_ID_FIELD, userId);
    }

    @Override
    public void setTenant(String tenantId) {
        //  The tenant identifier is used as part of the Elasticsearch index name.
        //  There are restrictions around the naming of the index including it must be lowercase
        //  and not contain commas.
        if(tenantId.contains(",")) {
            //  Report invalid comma usage.
            String errorMessage = "Invalid characters (i.e commas) in the tenant identifier: " + tenantId;
            LOG.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        auditEventCommonFields.put(ElasticAuditConstants.FixedFieldName.TENANT_ID_FIELD, tenantId.toLowerCase());
    }

    @Override
    public void setCorrelationId(String correlationId) {
        auditEventCommonFields.put(ElasticAuditConstants.FixedFieldName.CORRELATION_ID_FIELD, correlationId);
    }

    @Override
    public void setEventType(String eventCategoryId, String eventTypeId) {
        auditEventCommonFields.put(ElasticAuditConstants.FixedFieldName.EVENT_CATEGORY_ID_FIELD, eventCategoryId);
        auditEventCommonFields.put(ElasticAuditConstants.FixedFieldName.EVENT_TYPE_ID_FIELD, eventTypeId);
    }

    @Override
    public void addEventParameter(String name, String columnName, String value) {
        auditEventParams.add(new EventParam(name, "string", columnName, value));
    }

    @Override
    public void addEventParameter(String name, String columnName, short value) {
        auditEventParams.add(new EventParam(name, "short", columnName, value));
    }

    @Override
    public void addEventParameter(String name, String columnName, int value) {
        auditEventParams.add(new EventParam(name, "int", columnName, value));
    }

    @Override
    public void addEventParameter(String name, String columnName, long value) {
        auditEventParams.add(new EventParam(name, "long", columnName, value));
    }

    @Override
    public void addEventParameter(String name, String columnName, float value) {
        auditEventParams.add(new EventParam(name, "float", columnName, value));
    }

    @Override
    public void addEventParameter(String name, String columnName, double value) {
        auditEventParams.add(new EventParam(name, "double", columnName, value));
    }

    @Override
    public void addEventParameter(String name, String columnName, boolean value) {
        auditEventParams.add(new EventParam(name, "boolean", columnName, value));
    }

    @Override
    public void addEventParameter(String name, String columnName, Date value) {
        auditEventParams.add(new EventParam(name, "date", columnName, value));
    }

    /**
     * Sends the constructed Audit Event to the Webservice HTTP Endpoint
     * @throws Exception
     */
    @Override
    public void send() throws Exception {

        //  Get the constructed Audit Event as a JSON string
        String auditEventJson = getAuditEventAsJsonString();

        //  An Audit Event in Json format was not returned, throw exception
        if (auditEventJson == null || auditEventJson.isEmpty()) {
            String errorMessage = "No Audit Event JSON to send to the Audit webservice";
            LOG.error(errorMessage);
            throw new WebserviceClientException(errorMessage);
        }

        URL url = new URL("http://" + webserviceHostAndPort + "/caf-audit-service/v1/auditevents");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //  Try to send Audit Event to the Audit Webservice HTTP endpoint
        try {
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            byte[] auditEventJsonBytes = auditEventJson.getBytes("UTF-8");

            //  The number of bytes to send is known; set fixed length streaming mode
            conn.setFixedLengthStreamingMode(auditEventJsonBytes.length);

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(auditEventJsonBytes);
            outputStream.close();

            int responseCode = conn.getResponseCode();

            // Check that the response code was returned as expected. Print a WARN if the response code was a 200 but
            // not 204. Print an error if the response code is anything else.
            int expectedResponseCode = 204;
            if (responseCode >= 200 && responseCode < 300) {
                if (responseCode != expectedResponseCode) {
                    LOG.warn("Webservice accepted request but returned response code " + responseCode
                            + " when the expected response code is " + expectedResponseCode);
                }
            } else {
                String errorMessage = "Webservice returned response code " + responseCode + " when the expected " +
                        "response code is " + expectedResponseCode;
                LOG.error(errorMessage);
                throw new WebserviceClientException(errorMessage);
            }
            LOG.info("Audit event request sent and received response code " + responseCode + " from the webservice");
        } finally {
            conn.disconnect();
        }
    }

    private String getAuditEventAsJsonString() throws IOException {
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();

        jsonBuilder.startObject();
        for (Map.Entry<String, Object> auditEventCommonField : auditEventCommonFields.entrySet()) {
            jsonBuilder.field(auditEventCommonField.getKey(), auditEventCommonField.getValue());
        }

        if (auditEventParams != null && !auditEventParams.isEmpty()) {
            jsonBuilder.startArray("eventParams");
            for (EventParam auditEventParam : auditEventParams) {
                jsonBuilder.startObject()
                        .field("paramName", auditEventParam.getParamName())
                        .field("paramType", auditEventParam.getParamType())
                        .field("paramColumnName", auditEventParam.getParamColumnName())
                        .field("paramValue", auditEventParam.getParamValue())
                        .endObject();
            }
            jsonBuilder.endArray();
        }
        jsonBuilder.endObject();
        jsonBuilder.close();

        return jsonBuilder.string();
    }

    private class EventParam {

        public EventParam(String paramName, String paramType, String paramColumnName,
                          Object paramValue) {
            this.paramName = paramName;
            this.paramType = paramType;
            this.paramColumnName = paramColumnName;
            this.paramValue = paramValue;
        }

        private String paramName;
        private String paramType;
        private String paramColumnName;
        private Object paramValue;

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }

        public String getParamType() {
            return paramType;
        }

        public void setParamType(String paramType) {
            this.paramType = paramType;
        }

        public String getParamColumnName() {
            return paramColumnName;
        }

        public void setParamColumnName(String paramColumnName) {
            this.paramColumnName = paramColumnName;
        }

        public Object getParamValue() {
            return paramValue;
        }

        public void setParamValue(Object paramValue) {
            this.paramValue = paramValue;
        }
    }
}
