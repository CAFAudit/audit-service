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
package com.hpe.caf.auditing.elastic;

import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.auditing.exception.AuditConfigurationException;
import com.hpe.caf.services.audit.api.AuditLog;
import com.hpe.caf.util.processidentifier.ProcessIdentifier;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class GeneratedAuditLogIT {

    private static final String testTenant = "tenant1";

    private static String ES_HOSTNAME;
    private static String ES_HOSTNAME_AND_PORT;
    private static int ES_PORT;
    private static String CAF_ELASTIC_PROTOCOL;

    @BeforeClass
    public static void setup() throws Exception {
        // Test the Auditing library in direct mode
        System.setProperty("CAF_AUDIT_MODE", "elasticsearch");
        
        CAF_ELASTIC_PROTOCOL = System.getProperty("CAF_ELASTIC_PROTOCOL", System.getenv("CAF_ELASTIC_PROTOCOL"));
        ES_HOSTNAME = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
        ES_PORT = Integer.parseInt(System.getProperty("es.port", System.getenv("es.port")));

        ES_HOSTNAME_AND_PORT = String.format("%s:%s", ES_HOSTNAME, ES_PORT);
    }

    @After
    public void cleanUp() throws AuditConfigurationException {
        try (RestHighLevelClient restHighLevelClient
                     = ElasticAuditRestHighLevelClientFactory.getHighLevelClient(CAF_ELASTIC_PROTOCOL, ES_HOSTNAME_AND_PORT)) {
            deleteIndex(restHighLevelClient, testTenant + ElasticAuditConstants.Index.SUFFIX);
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

            SearchHit searchHit = getAuditEvent(correlationId);
            Map<String, Object> source = searchHit.getSourceAsMap();

            assertFixedField(ProcessIdentifier.getProcessId().toString(), ElasticAuditConstants.FixedFieldName.PROCESS_ID_FIELD, source);

            Assert.assertEquals(Thread.currentThread().getId(), ((Integer) source.get(ElasticAuditConstants.FixedFieldName.THREAD_ID_FIELD)).longValue());

            //Event order is tested in eventOrderTest()

            Object eventTimeField = source.get(ElasticAuditConstants.FixedFieldName.EVENT_TIME_FIELD);
            DateTime eventDateTime = new DateTime(eventTimeField);
            Assert.assertTrue(eventDateTime.isAfter(new DateTime(date)));

            assertFixedField(InetAddress.getLocalHost().getHostName(), ElasticAuditConstants.FixedFieldName.EVENT_TIME_SOURCE_FIELD, source);
            assertFixedField("TestAuditEvents", ElasticAuditConstants.FixedFieldName.APPLICATION_ID_FIELD, source);
            assertFixedField("TestCategory1", ElasticAuditConstants.FixedFieldName.EVENT_CATEGORY_ID_FIELD, source);
            assertFixedField("TestEvent1", ElasticAuditConstants.FixedFieldName.EVENT_TYPE_ID_FIELD, source);

            Assert.assertEquals(testTenant + ElasticAuditConstants.Index.SUFFIX, searchHit.getIndex());
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

                SearchHit searchHit = getAuditEvent(correlationId);
                Map<String, Object> source = searchHit.getSourceAsMap();
                event1Order = (Integer) source.get(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD);
            }

            {
                Date date = new Date();
                String correlationId = getCorrelationId();
                AuditLog.auditTestEvent1(auditChannel, testTenant, "user1", correlationId,
                        "stringType1", "stringType2", "stringType3", "stringType4",
                        Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true, date);

                SearchHit searchHit = getAuditEvent(correlationId);
                Map<String, Object> source = searchHit.getSourceAsMap();
                event2Order = (Integer) source.get(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD);
            }

            Assert.assertTrue("Event 1 order was not less than event 2 order", event1Order < event2Order);
        }
    }

    private String getCorrelationId(){
        return UUID.randomUUID().toString();
    }

    private SearchHit getAuditEvent(String correlationId) throws AuditConfigurationException {
        try (RestHighLevelClient restHighLevelClient
                     = ElasticAuditRestHighLevelClientFactory.getHighLevelClient(CAF_ELASTIC_PROTOCOL, ES_HOSTNAME_AND_PORT)) {
            //The default queryType is https://www.elastic.co/blog/understanding-query-then-fetch-vs-dfs-query-then-fetch

            final SearchRequest searchRequest = new SearchRequest()
                    .indices("*" + ElasticAuditConstants.Index.SUFFIX)
                    .searchType(SearchType.QUERY_THEN_FETCH)
                    .source(new SearchSourceBuilder()
                            .query(QueryBuilders.matchQuery(ElasticAuditConstants.FixedFieldName.CORRELATION_ID_FIELD, correlationId))
                            .from(0)
                            .size(10)
                    );

            SearchHits searchHits = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT).getHits();

            for (int attempts = 0; attempts < 5; attempts++) {
                if (searchHits.getTotalHits().value > 0) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                searchHits = searchHits = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT).getHits();
            }

            Assert.assertEquals("Expected search result not found", 1, searchHits.getTotalHits().value);

            return searchHits.getHits()[0];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertFixedField(String expected, String fieldName, Map<String, Object> source){

        Assert.assertTrue(String.format("Field %s not returned", fieldName), source.containsKey(fieldName));
        Object sourceField = source.get(fieldName);

        Assert.assertEquals(String.class, sourceField.getClass());
        String value = (String) sourceField;
        Assert.assertEquals(expected, value);
    }

    private void assertField(String expected, String fieldName, Map<String, Object> source){

        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.KEYWORD_SUFFIX);
        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        Assert.assertEquals(String.class, sourceField.getClass());
        String value = (String) sourceField;
        Assert.assertEquals(expected, value);
    }

    private void assertField(Short expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.SHORT_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        // Parse for short type because the Java search api returns shorts as integers
        Assert.assertEquals(Integer.class, sourceField.getClass());
        Short value = ((Integer)(sourceField)).shortValue();
        Assert.assertEquals(expected, value);
    }

    private void assertField(Integer expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.INT_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        Assert.assertEquals(Integer.class, sourceField.getClass());
        Integer value = (Integer)sourceField;
        Assert.assertEquals(expected, value);
    }

    private void assertField(Long expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.LONG_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        Assert.assertEquals(Long.class, sourceField.getClass());
        Long value = (Long)sourceField;
        Assert.assertEquals(expected, value);
    }

    private void assertField(Float expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.FLOAT_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        // Parse for float type because the Java search api returns floats as doubles
        Assert.assertEquals(Double.class, sourceField.getClass());
        Float value = ((Double)sourceField).floatValue();
        Assert.assertEquals(expected, value);
    }

    private void assertField(Double expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.DOUBLE_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        Assert.assertEquals(Double.class, sourceField.getClass());
        Double value = (Double)sourceField;
        Assert.assertEquals(expected, value);
    }

    private void assertField(Boolean expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.BOOLEAN_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        Assert.assertEquals(Boolean.class, sourceField.getClass());
        Boolean value = (Boolean)sourceField;
        Assert.assertEquals(expected, value);
    }

    private void assertField(Date expected, String fieldName, Map<String, Object> source) throws ParseException {
        String fullFieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.DATE_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        //  Transform the returned value into a DateTime object as the Java search api returns dates as a strings
        Assert.assertEquals(String.class, sourceField.getClass());

        String sourceTime = (String)sourceField;
        DateTime dateTime = new DateTime(sourceTime);

        Assert.assertEquals(new DateTime(expected), dateTime);
    }

    private static void deleteIndex(RestHighLevelClient client, String indexId)
    {
        ElasticAuditRetryOperation retryDelete = new ElasticAuditRetryOperation();
        while (retryDelete.shouldRetry()) {
            try {
                final AcknowledgedResponse acknowledgedResponse = client.indices()
                        .delete(new DeleteIndexRequest().indices(indexId.toLowerCase()), RequestOptions.DEFAULT);

                if (acknowledgedResponse.isAcknowledged()) {
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