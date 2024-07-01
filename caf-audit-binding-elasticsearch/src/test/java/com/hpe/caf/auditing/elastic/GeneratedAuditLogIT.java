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

import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.services.audit.api.AuditLog;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.auditing.exception.AuditConfigurationException;
import com.hpe.caf.util.processidentifier.ProcessIdentifier;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue.ValueType;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SearchType;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.indices.DeleteIndexResponse;
import org.opensearch.client.transport.OpenSearchTransport;

public class GeneratedAuditLogIT {

    private static final String testTenant = "tenant1";

    private static String ES_HOSTNAME;
    private static String ES_HOSTNAME_AND_PORT;
    private static int ES_PORT;
    private static String CAF_ELASTIC_PROTOCOL;
    private static String CAF_ELASTIC_USERNAME;
    private static String CAF_ELASTIC_PASSWORD;

    @BeforeAll
    public static void setup() throws Exception {
        // Test the Auditing library in direct mode
        System.setProperty("CAF_AUDIT_MODE", "elasticsearch");

        CAF_ELASTIC_PROTOCOL = System.getProperty("CAF_ELASTIC_PROTOCOL", System.getenv("CAF_ELASTIC_PROTOCOL"));
        CAF_ELASTIC_USERNAME = System.getProperty("CAF_ELASTIC_USERNAME", System.getenv("CAF_ELASTIC_USERNAME"));
        CAF_ELASTIC_PASSWORD = System.getProperty("CAF_ELASTIC_PASSWORD", System.getenv("CAF_ELASTIC_PASSWORD"));
        ES_HOSTNAME = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
        ES_PORT = Integer.parseInt(System.getProperty("es.port", System.getenv("es.port")));
        ES_HOSTNAME_AND_PORT = String.format("%s:%s", ES_HOSTNAME, ES_PORT);
    }

    @AfterEach
    public void cleanUp() throws AuditConfigurationException {
        try (OpenSearchTransport openSearchTransport
            = OpenSearchTransportFactory.getOpenSearchTransport(
                CAF_ELASTIC_PROTOCOL,
                ES_HOSTNAME_AND_PORT,
                CAF_ELASTIC_USERNAME,
                CAF_ELASTIC_PASSWORD)) {
            deleteIndex(new OpenSearchClient(openSearchTransport), testTenant + ElasticAuditConstants.Index.SUFFIX);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void auditSimpleEventTest() throws Exception {
        Date date = new Date();
        String correlationId = getCorrelationId();

        try (AuditConnection auditConnection = AuditConnectionFactory.createConnection();
             com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {
            AuditLog.auditTestEvent1(auditChannel, testTenant, "user1", correlationId,
                    "stringType1", "stringType2", "stringType3", "stringType4",
                    Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true, date);

            Hit<JsonData> searchHit = getAuditEvent(correlationId);
            JsonObject source = searchHit.source().toJson().asJsonObject();

            assertFixedField(ProcessIdentifier.getProcessId().toString(), ElasticAuditConstants.FixedFieldName.PROCESS_ID_FIELD, source);

            assertEquals(Thread.currentThread().getId(),
                                source.getJsonNumber(ElasticAuditConstants.FixedFieldName.THREAD_ID_FIELD).longValue());

            //Event order is tested in eventOrderTest()

            String eventTimeField = source.getString(ElasticAuditConstants.FixedFieldName.EVENT_TIME_FIELD);
            final Instant eventDateTime = Instant.parse(eventTimeField);
            assertTrue(eventDateTime.isAfter(date.toInstant()));

            assertFixedField(InetAddress.getLocalHost().getHostName(), ElasticAuditConstants.FixedFieldName.EVENT_TIME_SOURCE_FIELD, source);
            assertFixedField("TestAuditEvents", ElasticAuditConstants.FixedFieldName.APPLICATION_ID_FIELD, source);
            assertFixedField("TestCategory1", ElasticAuditConstants.FixedFieldName.EVENT_CATEGORY_ID_FIELD, source);
            assertFixedField("TestEvent1", ElasticAuditConstants.FixedFieldName.EVENT_TYPE_ID_FIELD, source);

            assertEquals(testTenant + ElasticAuditConstants.Index.SUFFIX, searchHit.index());
            assertFixedField("user1", ElasticAuditConstants.FixedFieldName.USER_ID_FIELD, source);
            assertFixedField(correlationId, ElasticAuditConstants.FixedFieldName.CORRELATION_ID_FIELD, source);
            assertField(Short.MAX_VALUE, "ShortType", source);
            assertField(Integer.MAX_VALUE, "IntType", source);
            assertField(Long.MAX_VALUE, "LongType", source);
            assertField(Float.MAX_VALUE, "FloatType", source);
            assertField(Double.MAX_VALUE, "DoubleType", source);
            assertField(true, "BooleanType", source);
            assertField(date, "DateType", source);
        }
    }

    @Test
    public void eventOrderTest() throws Exception{
        int event1Order;
        int event2Order;

        try (
                AuditConnection auditConnection = AuditConnectionFactory.createConnection();
                com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {
            {
                Date date = new Date();
                String correlationId = getCorrelationId();
                AuditLog.auditTestEvent1(auditChannel, testTenant, "user1", correlationId,
                        "stringType1", "stringType2", "stringType3", "stringType4",
                        Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true, date);

                Hit<JsonData> searchHit = getAuditEvent(correlationId);
                JsonObject source = searchHit.source().toJson().asJsonObject();
                event1Order = source.getInt(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD);
            }

            {
                Date date = new Date();
                String correlationId = getCorrelationId();
                AuditLog.auditTestEvent1(auditChannel, testTenant, "user1", correlationId,
                        "stringType1", "stringType2", "stringType3", "stringType4",
                        Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true, date);

                Hit<JsonData> searchHit = getAuditEvent(correlationId);
                JsonObject source = searchHit.source().toJson().asJsonObject();
                event2Order = source.getInt(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD);
            }

            assertTrue(event1Order < event2Order, "Event 1 order was not less than event 2 order");
        }
    }

    private String getCorrelationId(){
        return UUID.randomUUID().toString();
    }

    private Hit<JsonData> getAuditEvent(String correlationId) throws AuditConfigurationException {
        try (OpenSearchTransport openSearchTransport
            = OpenSearchTransportFactory.getOpenSearchTransport(
                CAF_ELASTIC_PROTOCOL,
                ES_HOSTNAME_AND_PORT,
                CAF_ELASTIC_USERNAME,
                CAF_ELASTIC_PASSWORD)) {
            final OpenSearchClient openSearchClient = new OpenSearchClient(openSearchTransport);
            //The default queryType is https://www.elastic.co/blog/understanding-query-then-fetch-vs-dfs-query-then-fetch
            final SearchRequest searchRequest = new SearchRequest.Builder()
            .index("*" + ElasticAuditConstants.Index.SUFFIX)
            .searchType(SearchType.QueryThenFetch)
            .query(q -> q
                .match(m -> m
                    .field(ElasticAuditConstants.FixedFieldName.CORRELATION_ID_FIELD)
                    .query(FieldValue.of(correlationId))
                    )
                 )
            .from(0)
            .size(10)
            .build();

            HitsMetadata<JsonData> hits = openSearchClient.search(searchRequest, JsonData.class).hits();
            for (int attempts = 0; attempts < 5; attempts++) {
                if (hits.total().value() > 0) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hits = openSearchClient.search(searchRequest, JsonData.class).hits();
            }

            assertEquals(1, hits.total().value(), "Expected search result not found");

            return hits.hits().get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertFixedField(String expected, String fieldName, JsonObject source){

        assertTrue(source.containsKey(fieldName), String.format("Field %s not returned", fieldName));
        assertEquals(ValueType.STRING, source.get(fieldName).getValueType());
        String sourceField = source.getString(fieldName);

        assertEquals(expected, sourceField);
    }

    private void assertField(String expected, String fieldName, JsonObject source){

        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.KEYWORD_SUFFIX);
        assertTrue(source.containsKey(fullFieldName), String.format("Field %s not returned", fullFieldName));
        assertEquals(ValueType.STRING, source.get(fullFieldName).getValueType());
        String sourceField = source.getString(fullFieldName);

        assertEquals(expected, sourceField);
    }

    private void assertField(Short expected, String fieldName, JsonObject source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.SHORT_SUFFIX);

        assertTrue(source.containsKey(fullFieldName), String.format("Field %s not returned", fullFieldName));
        assertEquals(ValueType.NUMBER, source.get(fullFieldName).getValueType());
        Short sourceField = source.getJsonNumber(fullFieldName).numberValue().shortValue();

        // Parse for short type because the Java search api returns shorts as integers
        assertEquals(expected, sourceField);
    }

    private void assertField(Integer expected, String fieldName, JsonObject source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.INT_SUFFIX);

        assertTrue(source.containsKey(fullFieldName), String.format("Field %s not returned", fullFieldName));
        assertEquals(ValueType.NUMBER, source.get(fullFieldName).getValueType());
        Integer sourceField = source.getInt(fullFieldName);

        assertEquals(expected, sourceField);
    }

    private void assertField(Long expected, String fieldName, JsonObject source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.LONG_SUFFIX);

        assertTrue(source.containsKey(fullFieldName), String.format("Field %s not returned", fullFieldName));
        assertEquals(ValueType.NUMBER, source.get(fullFieldName).getValueType());
        Long sourceField = source.getJsonNumber(fullFieldName).longValue();

        assertEquals(expected, sourceField);
    }

    private void assertField(Float expected, String fieldName, JsonObject source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.FLOAT_SUFFIX);

        assertTrue(source.containsKey(fullFieldName), String.format("Field %s not returned", fullFieldName));
        assertEquals(ValueType.NUMBER, source.get(fullFieldName).getValueType());
        Float sourceField = source.getJsonNumber(fullFieldName).numberValue().floatValue();

        // Parse for float type because the Java search api returns floats as doubles
        assertEquals(expected, sourceField);
    }

    private void assertField(Double expected, String fieldName, JsonObject source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.DOUBLE_SUFFIX);

        assertTrue(source.containsKey(fullFieldName), String.format("Field %s not returned", fullFieldName));
        assertEquals(ValueType.NUMBER, source.get(fullFieldName).getValueType());
        Double sourceField = source.getJsonNumber(fullFieldName).doubleValue();

        assertEquals(expected, sourceField);
    }

    private void assertField(Boolean expected, String fieldName, JsonObject source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.BOOLEAN_SUFFIX);

        assertTrue(source.containsKey(fullFieldName), String.format("Field %s not returned", fullFieldName));
        assertTrue(List.of(ValueType.TRUE, ValueType.FALSE).contains(source.get(fullFieldName).getValueType()));
        Boolean sourceField = source.getBoolean(fullFieldName);

        assertEquals(expected, sourceField);
    }

    private void assertField(Date expected, String fieldName, JsonObject source) throws ParseException {
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.DATE_SUFFIX);

        assertTrue(source.containsKey(fullFieldName), String.format("Field %s not returned", fullFieldName));
        assertEquals(ValueType.NUMBER, source.get(fullFieldName).getValueType());
        Long sourceField = source.getJsonNumber(fullFieldName).longValue();

        //  Transform the returned value into a DateTime object as the Java search api returns dates as a strings
        Instant dateTime = Instant.ofEpochMilli(sourceField);

        assertEquals(expected.toInstant(), dateTime);
    }

    private static void deleteIndex(OpenSearchClient client, String indexId)
    {
        ElasticAuditRetryOperation retryDelete = new ElasticAuditRetryOperation();
        while (retryDelete.shouldRetry()) {
            try {
                final DeleteIndexResponse deleteIndexResponse = client.indices().delete(d -> d.index(indexId.toLowerCase()));

                if (deleteIndexResponse.acknowledged()) {
                    // If Elastic acknowledged our delete wait a second to allow it time to delete the index
                    Thread.sleep(1000);
                    break;
                }

                // Retry deletion if Elastic did not acknowledge the delete request.
                try {
                    retryDelete.retryNeeded();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}