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

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionHelper;
import com.hpe.caf.services.audit.api.AuditLog;
import com.hpe.caf.util.processidentifier.ProcessIdentifier;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
    private static String ES_CLUSTERNAME;

    @BeforeClass
    public static void setup() throws Exception {
        // Test the Auditing library in direct mode
        System.setProperty("AUDIT_LIB_MODE", "direct");

        ES_HOSTNAME = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
        ES_PORT = Integer.parseInt(System.getProperty("es.port", System.getenv("es.port")));
        ES_CLUSTERNAME = System.getProperty("es.cluster.name", System.getenv("es.cluster.name"));

        ES_HOSTNAME_AND_PORT = String.format("%s:%s", ES_HOSTNAME, ES_PORT);
    }

    @After
    public void cleanUp() throws ConfigurationException {
        try (TransportClient transportClient
                     = ElasticAuditTransportClientFactory.getTransportClient(ES_HOSTNAME_AND_PORT, ES_CLUSTERNAME)) {
            deleteIndex(transportClient, testTenant + ElasticAuditConstants.Index.SUFFIX);
        }
    }

    @Test
    public void auditSimpleEventTest() throws Exception {
        Date date = new Date();
        String correlationId = getCorrelationId();

        try (AuditConnection auditConnection = AuditConnectionHelper.getElasticAuditConnection(ES_HOSTNAME_AND_PORT,
                        ES_CLUSTERNAME);
             com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {
            AuditLog.auditTestEvent1(auditChannel, testTenant, "user1", correlationId, "stringType1",
                    Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true, date);

            SearchHit searchHit = getAuditEvent(correlationId);
            Map<String, Object> source = searchHit.getSource();

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
                AuditConnection auditConnection = AuditConnectionHelper.getElasticAuditConnection(ES_HOSTNAME_AND_PORT,
                        ES_CLUSTERNAME);
                com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {
            {
                Date date = new Date();
                String correlationId = getCorrelationId();
                AuditLog.auditTestEvent1(auditChannel, testTenant, "user1", correlationId, "stringType1",
                        Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true, date);

                SearchHit searchHit = getAuditEvent(correlationId);
                Map<String, Object> source = searchHit.getSource();
                event1Order = (Integer) source.get(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD);
            }

            {
                Date date = new Date();
                String correlationId = getCorrelationId();
                AuditLog.auditTestEvent1(auditChannel, testTenant, "user1", correlationId, "stringType1",
                        Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true, date);

                SearchHit searchHit = getAuditEvent(correlationId);
                Map<String, Object> source = searchHit.getSource();
                event2Order = (Integer) source.get(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD);
            }

            Assert.assertTrue("Event 1 order was not less than event 2 order", event1Order < event2Order);
        }
    }

    private String getCorrelationId(){
        return UUID.randomUUID().toString();
    }

    private SearchHit getAuditEvent(String correlationId) throws ConfigurationException {
        try (TransportClient transportClient
                     = ElasticAuditTransportClientFactory.getTransportClient(ES_HOSTNAME_AND_PORT, ES_CLUSTERNAME)) {
            //The default queryType is https://www.elastic.co/blog/understanding-query-then-fetch-vs-dfs-query-then-fetch
            SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch("*" + ElasticAuditConstants.Index.SUFFIX)
                    .setTypes(ElasticAuditConstants.Index.TYPE)
                    .setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setFetchSource(true)
                    .setQuery(QueryBuilders.matchQuery(ElasticAuditConstants.FixedFieldName.CORRELATION_ID_FIELD, correlationId));

            SearchHits searchHits = searchRequestBuilder.get().getHits();
            for (int attempts = 0; attempts < 5; attempts++) {
                if (searchHits.getTotalHits() > 0) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                searchHits = searchRequestBuilder.get().getHits();
            }

            Assert.assertEquals("Expected search result not found", 1, searchHits.getTotalHits());

            return searchHits.getHits()[0];
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

    private static void deleteIndex(TransportClient client, String indexId)
    {
        ElasticAuditRetryOperation retryDelete = new ElasticAuditRetryOperation();
        while (retryDelete.shouldRetry()) {
            try {
                boolean didElasticAckDelete = client.admin().indices().delete(
                        new DeleteIndexRequest(indexId.toLowerCase())).get().isAcknowledged();

                if (didElasticAckDelete) {
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

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
