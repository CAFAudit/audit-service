/*
 * Copyright 2015-2020 Micro Focus or one of its affiliates.
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonWriter;
import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.AuditIndexingHint;
import com.hpe.caf.auditing.exception.AuditingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class WebServiceClientAuditEventBuilder implements AuditEventBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(WebServiceClientAuditEventBuilder.class.getName());

    private static final String AUDIT_WS_CONN_TIMEOUT = "AUDIT_WS_CONN_TIMEOUT";

    private static int webServiceConnectionTimeout;

    private final Proxy httpProxy;

    private final URL webServiceEndpointUrl;

    private final Map<String, Object> auditEventCommonFields = new HashMap<>();

    private final List<EventParam> auditEventParams = new ArrayList<>();

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter())
            .create();

    /**
     * Webservice Client Audit Event Builder object is use to build up application audit events and send them to the
     * Audit Webservice.
     * @param webServiceEndpointUrl webService HTTP endpoint URL object
     * @param httpProxy the proxy that HTTP requests to the webService endpoint will be routed via
     * @param coreMetadataProvider the Audit Event Metadata for creation of system data Audit Event fixed fields
     */
    public WebServiceClientAuditEventBuilder(final URL webServiceEndpointUrl, final Proxy httpProxy,
                                             final AuditCoreMetadataProvider coreMetadataProvider) {
        this.webServiceEndpointUrl = webServiceEndpointUrl;
        this.httpProxy = httpProxy;

        this.webServiceConnectionTimeout = getWebServiceConnectionTimeout();

        //  Add fixed audit event fields to Map.
        addCommonFields(coreMetadataProvider);
    }

    private void addCommonFields(AuditCoreMetadataProvider coreMetadataProvider)
    {
        auditEventCommonFields.put(WebServiceClientConstants.FixedFieldName.PROCESS_ID_FIELD, coreMetadataProvider.getProcessId().toString());
        auditEventCommonFields.put(WebServiceClientConstants.FixedFieldName.THREAD_ID_FIELD, coreMetadataProvider.getThreadId());
        auditEventCommonFields.put(WebServiceClientConstants.FixedFieldName.EVENT_ORDER_FIELD, coreMetadataProvider.getEventOrder());
        auditEventCommonFields.put(WebServiceClientConstants.FixedFieldName.EVENT_TIME_FIELD, coreMetadataProvider.getEventTime().toString());
        auditEventCommonFields.put(WebServiceClientConstants.FixedFieldName.EVENT_TIME_SOURCE_FIELD, coreMetadataProvider.getEventTimeSource());
    }

    @Override
    public void setApplication(String applicationId) {
        auditEventCommonFields.put(WebServiceClientConstants.FixedFieldName.APPLICATION_ID_FIELD, applicationId);
    }

    @Override
    public void setUser(String userId) {
        auditEventCommonFields.put(WebServiceClientConstants.FixedFieldName.USER_ID_FIELD, userId);
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

        auditEventCommonFields.put(WebServiceClientConstants.FixedFieldName.TENANT_ID_FIELD, tenantId.toLowerCase());
    }

    @Override
    public void setCorrelationId(String correlationId) {
        auditEventCommonFields.put(WebServiceClientConstants.FixedFieldName.CORRELATION_ID_FIELD, correlationId);
    }

    @Override
    public void setEventType(String eventCategoryId, String eventTypeId) {
        auditEventCommonFields.put(WebServiceClientConstants.FixedFieldName.EVENT_CATEGORY_ID_FIELD, eventCategoryId);
        auditEventCommonFields.put(WebServiceClientConstants.FixedFieldName.EVENT_TYPE_ID_FIELD, eventTypeId);
    }

    @Override
    public void addEventParameter(String name, String columnName, String value) {
        auditEventParams.add(new EventParam(name, "string", columnName, value));
    }

    @Override
    public void addEventParameter(String name, String columnName, String value, AuditIndexingHint indexingHint) {
        auditEventParams.add(new EventParam(name, "string", columnName, value, indexingHint));
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
     * @throws IOException if a HTTP connection cannot be opened to the webService or HTTP request output stream could
     * not be opened
     * @throws AuditingException if JSON string could not be built from audit event parameters
     */
    @Override
    public void send() throws IOException, AuditingException {

        //  Get the constructed Audit Event as a JSON string
        String auditEventJson = getAuditEventAsJsonString();

        //  An Audit Event in Json format was not returned, throw exception
        if (auditEventJson == null || auditEventJson.isEmpty()) {
            String errorMessage = "No Audit Event JSON to send to the Audit WebService";
            LOG.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        byte[] auditEventJsonBytes = auditEventJson.getBytes("UTF-8");

        final HttpURLConnection webServiceHttpUrlConnection =
                getWebServiceHttpEndpointUrlConnection(auditEventJsonBytes.length);

        //  Try to send the JSON request to the WebService HTTP Endpoint Connection output stream
        try (OutputStream outputStream = webServiceHttpUrlConnection.getOutputStream()) {
            outputStream.write(auditEventJsonBytes);
            outputStream.close();
        }

        try {
            //  Try to retrieve response from the Audit WebService HTTP endpoint request
            int responseCode = webServiceHttpUrlConnection.getResponseCode();

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
                throw new AuditingException(errorMessage);
            }
            LOG.info("Audit event request sent and received response code " + responseCode + " from the WebService");
        } finally {
            webServiceHttpUrlConnection.disconnect();
        }
    }

    private static void configureHttpUrlConnection(final HttpURLConnection httpUrlConnection,
                                                   final int streamingModeLength) {
        httpUrlConnection.setConnectTimeout(webServiceConnectionTimeout);
        httpUrlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        httpUrlConnection.setDoOutput(true);
        //  The number of bytes to send is known; set fixed length streaming mode
        httpUrlConnection.setFixedLengthStreamingMode(streamingModeLength);
    }

    private HttpURLConnection getWebServiceHttpEndpointUrlConnection(final int streamingModeLength) throws IOException {
        final HttpURLConnection webServiceHttpUrlConn;
        // If there is no HTTP proxy, create a new unproxied HTTP or HTTPS URL Connection. Else create a new
        // proxied HTTP or HTTPS URL Connection.
        if (httpProxy == null) {
            if (webServiceEndpointUrl.getProtocol().equalsIgnoreCase("https")) {
                webServiceHttpUrlConn = (HttpsURLConnection) webServiceEndpointUrl.openConnection();
            } else {
                webServiceHttpUrlConn = (HttpURLConnection) webServiceEndpointUrl.openConnection();
            }
        } else {
            if (webServiceEndpointUrl.getProtocol().equalsIgnoreCase("https")) {
                webServiceHttpUrlConn = (HttpsURLConnection) webServiceEndpointUrl.openConnection(httpProxy);
            } else {
                webServiceHttpUrlConn = (HttpURLConnection) webServiceEndpointUrl.openConnection(httpProxy);
            }
        }
        configureHttpUrlConnection(webServiceHttpUrlConn, streamingModeLength);
        return webServiceHttpUrlConn;
    }

    private static int getWebServiceConnectionTimeout() {
        int webServiceTimeout;
        try {
            webServiceTimeout = Integer.parseInt(System.getProperty(AUDIT_WS_CONN_TIMEOUT,
                    System.getenv(AUDIT_WS_CONN_TIMEOUT)));
        } catch (NumberFormatException efe) {
            LOG.debug("Unable to parse timeout value from "
                    + AUDIT_WS_CONN_TIMEOUT + " system or environment variable, defaulting timeout to 30 seconds");
            webServiceTimeout = 30000;
        }
        return webServiceTimeout;
    }

    private String getAuditEventAsJsonString() throws IOException {

        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(stringWriter);

        jsonWriter.beginObject();
        for (Map.Entry<String, Object> auditEventCommonField : auditEventCommonFields.entrySet()) {
            jsonWriter.name(auditEventCommonField.getKey());
            gson.toJson(auditEventCommonField.getValue(), auditEventCommonField.getValue().getClass(),
                    jsonWriter);
        }

        if (auditEventParams != null && !auditEventParams.isEmpty()) {
            jsonWriter.name("eventParams");
            jsonWriter.beginArray();
            for (EventParam auditEventParam : auditEventParams) {
                jsonWriter.beginObject();
                jsonWriter.name("paramName").value(auditEventParam.getParamName());
                jsonWriter.name("paramType").value(auditEventParam.getParamType());
                // If the audit event param has an indexing hint set, lower case and add it to paramIndexingHint field
                if (auditEventParam.getParamIndexingHint() != null) {
                    jsonWriter.name("paramIndexingHint")
                            .value(auditEventParam.getParamIndexingHint().toString().toLowerCase());
                }
                jsonWriter.name("paramValue");
                gson.toJson(auditEventParam.getParamValue(), auditEventParam.getParamValue().getClass(),
                        jsonWriter);

                jsonWriter.name("paramColumnName").value(auditEventParam.getParamColumnName());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
        }
        jsonWriter.endObject();
        jsonWriter.flush();
        jsonWriter.close();

        return stringWriter.toString();
    }

    private class EventParam {

        public EventParam(String paramName, String paramType, String paramColumnName, Object paramValue,
                          AuditIndexingHint paramIndexingHint) {
            this.paramName = paramName;
            this.paramType = paramType;
            this.paramColumnName = paramColumnName;
            this.paramValue = paramValue;
            this.paramIndexingHint = paramIndexingHint;
        }

        public EventParam(String paramName, String paramType, String paramColumnName, Object paramValue) {
            this.paramName = paramName;
            this.paramType = paramType;
            this.paramColumnName = paramColumnName;
            this.paramValue = paramValue;
        }

        private String paramName;
        private String paramType;
        private String paramColumnName;
        private Object paramValue;
        private AuditIndexingHint paramIndexingHint;

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

        public AuditIndexingHint getParamIndexingHint() {
            return paramIndexingHint;
        }

        public void setParamIndexingHint(AuditIndexingHint paramIndexingHint) {
            this.paramIndexingHint = paramIndexingHint;
        }

        public Object getParamValue() {
            return paramValue;
        }

        public void setParamValue(Object paramValue) {
            this.paramValue = paramValue;
        }
    }

    public static class GsonUTCDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

        private final DateFormat dateFormat;

        public GsonUTCDateAdapter() {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override public synchronized JsonElement serialize(Date date, Type type,
                                                            JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(dateFormat.format(date));
        }

        @Override public synchronized Date deserialize(JsonElement jsonElement, Type type,
                                                       JsonDeserializationContext jsonDeserializationContext) {
            try {
                return dateFormat.parse(jsonElement.getAsString());
            } catch (ParseException e) {
                throw new JsonParseException(e);
            }
        }
    }
}
