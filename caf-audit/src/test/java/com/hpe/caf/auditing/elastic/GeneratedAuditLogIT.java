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

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.services.audit.api.AuditLog;
import com.hpe.caf.util.processidentifier.ProcessIdentifier;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class GeneratedAuditLogIT {

    //TODO Would have preferred to reference these instead of duplicate them
    private static final String INDEX_PREFIX = "audit_tenant_";

    private static final String ELASTIC_TYPE = "cafAuditEvent";

    private static final String KEYWORD_SUFFIX = "_AKyw";
    private static final String SHORT_SUFFIX = "_ASrt";
    private static final String INT_SUFFIX = "_AInt";
    private static final String LONG_SUFFIX = "_ALng";
    private static final String FLOAT_SUFFIX = "_AFlt";
    private static final String DOUBLE_SUFFIX = "_ADbl";
    private static final String BOOLEAN_SUFFIX = "_ABln";
    private static final String DATE_SUFFIX = "_ADte";

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

    private static final Logger LOG = LoggerFactory.getLogger(GeneratedAuditLogIT.class);

    private final AuditChannel auditChannel;
    private final TransportClient transportClient;

    /*
        These tests can also be executed individually if you are running Elastic Search externally
     */
    public GeneratedAuditLogIT() throws Exception {

        //TODO Sorry I could not get my properties set correctly so reinstate this block and remove duplicated block below

//        this.elasticHostname = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
//        this.elasticPort = Integer.parseInt(System.getProperty("elasticsearch.http.port", System.getenv("elasticsearch.transport.port")));
//        this.elasticClusterName = System.getProperty("es.cluster.name", System.getenv("es.cluster.name"));

        String elasticHostname = System.getProperty("docker.host.address", "localhost");
        int elasticPort = Integer.parseInt(System.getProperty("elasticsearch.transport.port", "9300"));
        String elasticClusterName = System.getProperty("es.cluster.name", "elasticsearch");

        String elasticHostnameAndPort = String.format("%s:%s", elasticHostname, elasticPort);

        auditChannel = AuditConnectionHelper.getAuditConnection(elasticHostnameAndPort, elasticClusterName)
                .createChannel();

        transportClient = ElasticAuditTransportClientFactory.getTransportClient(elasticHostnameAndPort, elasticClusterName);
    }

    @BeforeClass
    public static void setup() throws Exception {
    }


    @Test
    public void auditSimpleEventTest() throws Exception {
        Date date = new Date();
        String correlationId = getCorrelationId();
        AuditLog.auditTestEvent1(auditChannel, "tenant1", "user1", correlationId, "stringType1",
                Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true, date );

        SearchHit searchHit = getAuditEvent(correlationId);
        Map<String, Object> source = searchHit.getSource();

        assertField(ProcessIdentifier.getProcessId().toString(), PROCESS_ID_FIELD, source);

        //TODO This will need to be corrected once we have index creation specifying the correct data type
//        assertField(Thread.currentThread().getId(), THREAD_ID_FIELD, source);

        //Event order is tested in eventOrderTest()

        Object eventTimeField = source.get(EVENT_TIME_FIELD.concat(DATE_SUFFIX));
        DateTime eventDateTime = new DateTime(eventTimeField);
        Assert.assertTrue(eventDateTime.isAfter(new DateTime(date)));

        assertField(InetAddress.getLocalHost().getHostName(), EVENT_TIME_SOURCE_FIELD, source);
        assertField("TestAuditEvents", APP_ID_FIELD, source);
        assertField("TestCategory1", EVENT_CATEGORY_ID_FIELD, source);
        assertField("TestEvent1", EVENT_TYPE_ID_FIELD, source);

        Assert.assertEquals(INDEX_PREFIX + "tenant1", searchHit.getIndex());
        assertField("user1", USER_ID_FIELD, source);
        assertField(correlationId, CORRELATION_ID_FIELD, source);
        assertField(Short.MAX_VALUE, "ShortType", source);
        assertField(Integer.MAX_VALUE, "IntType", source);
        assertField(Long.MAX_VALUE, "LongType", source);
        assertField(Float.MAX_VALUE, "FloatType", source);
        assertField(Double.MAX_VALUE, "DoubleType", source);
        assertField(true, "BooleanType", source);
        assertField(date, "DateType", source);
    }

    @Test
    public void eventOrderTest() throws Exception{
        long event1Order;
        long event2Order;

        {
            Date date = new Date();
            String correlationId = getCorrelationId();
            AuditLog.auditTestEvent1(auditChannel, "tenant1", "user1", correlationId, "stringType1",
                    Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true, date);

            SearchHit searchHit = getAuditEvent(correlationId);
            Map<String, Object> source = searchHit.getSource();
            event1Order = (Long)source.get(EVENT_ORDER_FIELD.concat(LONG_SUFFIX));
        }

        {
            Date date = new Date();
            String correlationId = getCorrelationId();
            AuditLog.auditTestEvent1(auditChannel, "tenant1", "user1", correlationId, "stringType1",
                    Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true, date);

            SearchHit searchHit = getAuditEvent(correlationId);
            Map<String, Object> source = searchHit.getSource();
            event2Order = (Long)source.get(EVENT_ORDER_FIELD.concat(LONG_SUFFIX));
        }

        Assert.assertTrue("Event 1 order was not less than event 2 order", event1Order<event2Order);

    }

    private String getCorrelationId(){
        return UUID.randomUUID().toString();
    }

    private SearchHit getAuditEvent(String correlationId){
        //The default queryType is https://www.elastic.co/blog/understanding-query-then-fetch-vs-dfs-query-then-fetch
        SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch(INDEX_PREFIX + "*")
                .setTypes(ELASTIC_TYPE)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setFetchSource(true)
                .setQuery(QueryBuilders.matchQuery(CORRELATION_ID_FIELD.concat(KEYWORD_SUFFIX), correlationId));

        SearchHits searchHits = searchRequestBuilder.get().getHits();
        for(int attempts=0; attempts<5; attempts++){
            if(searchHits.getTotalHits()>0){break;}
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

    private void assertField(String expected, String fieldName, Map<String, Object> source){

        String fullFieldName = fieldName.concat(KEYWORD_SUFFIX);
        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        Assert.assertEquals(String.class, sourceField.getClass());
        String value = (String) sourceField;
        Assert.assertEquals(expected, value);
    }

    private void assertField(Short expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(SHORT_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        //TODO This is likely because of Elastic Search autom mapping
        Assert.assertEquals(Integer.class, sourceField.getClass());
        Short value = ((Integer)(sourceField)).shortValue();
        Assert.assertEquals(expected, value);
    }

    private void assertField(Integer expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(INT_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        Assert.assertEquals(Integer.class, sourceField.getClass());
        Integer value = (Integer)sourceField;
        Assert.assertEquals(expected, value);
    }

    private void assertField(Long expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(LONG_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        Assert.assertEquals(Long.class, sourceField.getClass());
        Long value = (Long)sourceField;
        Assert.assertEquals(expected, value);
    }

    private void assertField(Float expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(FLOAT_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        //TODO This is likely because of Elastic Search autom mapping
        Assert.assertEquals(Double.class, sourceField.getClass());
        Float value = ((Double)sourceField).floatValue();
        Assert.assertEquals(expected, value);
    }

    private void assertField(Double expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(DOUBLE_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        Assert.assertEquals(Double.class, sourceField.getClass());
        Double value = (Double)sourceField;
        Assert.assertEquals(expected, value);
    }

    private void assertField(Boolean expected, String fieldName, Map<String, Object> source){
        String fullFieldName = fieldName.concat(BOOLEAN_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        Assert.assertEquals(Boolean.class, sourceField.getClass());
        Boolean value = (Boolean)sourceField;
        Assert.assertEquals(expected, value);
    }

    private void assertField(Date expected, String fieldName, Map<String, Object> source) throws ParseException {
        String fullFieldName = fieldName.concat(DATE_SUFFIX);

        Assert.assertTrue(String.format("Field %s not returned", fullFieldName), source.containsKey(fullFieldName));
        Object sourceField = source.get(fullFieldName);

        //TODO This is likely because of Elastic Search autom mapping, not sure on this one.
        Assert.assertEquals(String.class, sourceField.getClass());

        String sourceTime = (String)sourceField;
        DateTime dateTime = new DateTime(sourceTime);

        Assert.assertEquals(new DateTime(expected), dateTime);
    }

}
