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
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.auditing.AuditEventBuilder;
import org.elasticsearch.action.bulk.byscroll.BulkByScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ElasticAuditIT {

    private static final String APPLICATION_ID = "aTestApplication";
    private static final String TENANT_ID = "tTestTenant";
    private static final String USER_ID = "uTestUser2";
    private static final String EVENT_CATEGORY_ID = "evDocument";
    private static final String EVENT_TYPE_ID = "etView";
    private static final String CORRELATION_ID = "cTestCorrelation";

    private static final String ES_INDEX = "audit_tenant_" + TENANT_ID;
    private static final String ES_TYPE = "cafAuditEvent";

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

    private static final String CUSTOM_DOC_STRING_PARAM_FIELD = "docStringParam";
    private static final String CUSTOM_DOC_INT_PARAM_FIELD = "docIntParam";
    private static final String CUSTOM_DOC_SHORT_PARAM_FIELD = "docShortParam";
    private static final String CUSTOM_DOC_LONG_PARAM_FIELD = "docLongParam";
    private static final String CUSTOM_DOC_BOOLEAN_PARAM_FIELD = "docBooleanParam";
    private static final String CUSTOM_DOC_FLOAT_PARAM_FIELD = "docFloatParam";
    private static final String CUSTOM_DOC_DOUBLE_PARAM_FIELD = "docDoubleParam";
    private static final String CUSTOM_DOC_DATE_PARAM_FIELD = "docDateParam";

    private static String ES_HOSTNAME;
    private static int ES_PORT;
    private static String ES_CLUSTERNAME;

    @BeforeClass
    public static void setup() throws Exception {
        ES_HOSTNAME = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
        ES_PORT = Integer.parseInt(System.getProperty("es.port", System.getenv("es.port")));
        ES_CLUSTERNAME = System.getProperty("es.cluster.name", System.getenv("es.cluster.name"));
    }

    @Test(expected = ConfigurationException.class)
    public void testESHost_ConfigException() throws Exception {

        final String esUnknownHostName = "unknown";

        try (
                AuditConnection auditConnection = AuditConnectionFactory.createConnection(new ConfigurationSource() {

                    @Override
                    public <T> T getConfiguration(Class<T> aClass) throws ConfigurationException {
                        List<String> hostNames = new ArrayList<>();
                        hostNames.add(esUnknownHostName);

                        ElasticAuditConfiguration elasticAuditConfiguration = new ElasticAuditConfiguration();
                        elasticAuditConfiguration.setHostnames(hostNames);
                        elasticAuditConfiguration.setPort(ES_PORT);
                        elasticAuditConfiguration.setClusterName(ES_CLUSTERNAME);
                        return (T) elasticAuditConfiguration;
                    }
                })
        ) {
            // Do nothing as exception is expected to be thrown.
        }
    }

    @Test(expected = ConfigurationException.class)
    public void testESPort_ConfigException() throws Exception {

        final int esUnexpectedPort = 9100;

        try (
                AuditConnection auditConnection = AuditConnectionFactory.createConnection(new ConfigurationSource() {

                    @Override
                    public <T> T getConfiguration(Class<T> aClass) throws ConfigurationException {
                        List<String> hostNames = new ArrayList<>();
                        hostNames.add(ES_HOSTNAME);

                        ElasticAuditConfiguration elasticAuditConfiguration = new ElasticAuditConfiguration();
                        elasticAuditConfiguration.setHostnames(hostNames);
                        elasticAuditConfiguration.setPort(esUnexpectedPort);
                        elasticAuditConfiguration.setClusterName(ES_CLUSTERNAME);
                        return (T) elasticAuditConfiguration;
                    }
                });
                com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()
        ) {
            //  Index a sample audit event message into Elasticsearch.
            AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(APPLICATION_ID);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            auditEventBuilder.setTenant(TENANT_ID);
            auditEventBuilder.setUser(USER_ID);

            //  No need to set up custom data as we expect the call to index the document to fail because
            //  of unexpected port.
            auditEventBuilder.send();
        }
    }

    @Test
    public void testESIndexing() throws Exception {
        final List<String> esHostNames = new ArrayList<>();
        esHostNames.add(ES_HOSTNAME);

        try (
                AuditConnection auditConnection = AuditConnectionFactory.createConnection(new ConfigurationSource() {

                    @Override
                    public <T> T getConfiguration(Class<T> aClass) throws ConfigurationException {
                        ElasticAuditConfiguration elasticAuditConfiguration = new ElasticAuditConfiguration();
                        elasticAuditConfiguration.setHostnames(esHostNames);
                        elasticAuditConfiguration.setPort(ES_PORT);
                        elasticAuditConfiguration.setClusterName(ES_CLUSTERNAME);
                        return (T) elasticAuditConfiguration;
                    }
                });
                com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()
        ) {

            //  Index a sample audit event message into Elasticsearch.
            AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(APPLICATION_ID);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            auditEventBuilder.setTenant(TENANT_ID);
            auditEventBuilder.setUser(USER_ID);

            //  Set up random test values for custom field data.
            Random rand = new Random();

            String docStringParamValue = "testStringParam";
            auditEventBuilder.addEventParameter(CUSTOM_DOC_STRING_PARAM_FIELD,CUSTOM_DOC_STRING_PARAM_FIELD, docStringParamValue);
            int docIntParamValue = rand.nextInt();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_INT_PARAM_FIELD,CUSTOM_DOC_INT_PARAM_FIELD, docIntParamValue);
            short docShortParamValue = (short) rand.nextInt(Short.MAX_VALUE + 1);
            auditEventBuilder.addEventParameter(CUSTOM_DOC_SHORT_PARAM_FIELD,CUSTOM_DOC_SHORT_PARAM_FIELD,docShortParamValue);
            long docLongParamValue = rand.nextLong();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_LONG_PARAM_FIELD,CUSTOM_DOC_LONG_PARAM_FIELD, docLongParamValue);
            float docFloatParamValue = rand.nextFloat();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_FLOAT_PARAM_FIELD,CUSTOM_DOC_FLOAT_PARAM_FIELD, docFloatParamValue);
            double docDoubleParamValue = rand.nextDouble();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_DOUBLE_PARAM_FIELD,CUSTOM_DOC_DOUBLE_PARAM_FIELD, docDoubleParamValue);
            boolean docBooleanParamValue = rand.nextBoolean();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_BOOLEAN_PARAM_FIELD,CUSTOM_DOC_BOOLEAN_PARAM_FIELD, docBooleanParamValue);
            Date docDateParamValue = new Date();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_DATE_PARAM_FIELD,CUSTOM_DOC_DATE_PARAM_FIELD, docDateParamValue);

            //  Send audit event message to Elasticsearch.
            auditEventBuilder.send();
            Thread.sleep(1000);

            //  Search Elasticsearch for the newly indexed document (by UserId).
            try ( TransportClient transportClient = ElasticAuditTransportClientFactory.getTransportClient(esHostNames, ES_PORT, ES_CLUSTERNAME)) {
                SearchHit[] hits = searchDocument(transportClient, ES_INDEX, ES_TYPE, USER_ID_FIELD, USER_ID);

                //  Expecting a single hit.
                Assert.assertTrue(hits.length == 1);

                //  Verify fixed field data results.
                verifyFixedFieldResult(hits, APP_ID_FIELD, APPLICATION_ID);
                verifyFixedFieldResult(hits, EVENT_CATEGORY_ID_FIELD, EVENT_CATEGORY_ID);
                verifyFixedFieldResult(hits, EVENT_TYPE_ID_FIELD, EVENT_TYPE_ID);
                verifyFixedFieldResult(hits, USER_ID_FIELD, USER_ID);
                verifyFixedFieldResult(hits, CORRELATION_ID_FIELD, CORRELATION_ID);

                //  Verify fixed field data results.
                verifyCustomFieldResult(hits, CUSTOM_DOC_STRING_PARAM_FIELD, docStringParamValue, "string");
                verifyCustomFieldResult(hits, CUSTOM_DOC_INT_PARAM_FIELD, docIntParamValue, "int");
                verifyCustomFieldResult(hits, CUSTOM_DOC_SHORT_PARAM_FIELD, docShortParamValue, "short");
                verifyCustomFieldResult(hits, CUSTOM_DOC_LONG_PARAM_FIELD, docLongParamValue, "long");
                verifyCustomFieldResult(hits, CUSTOM_DOC_FLOAT_PARAM_FIELD, docFloatParamValue, "float");
                verifyCustomFieldResult(hits, CUSTOM_DOC_DOUBLE_PARAM_FIELD, docDoubleParamValue, "double");
                verifyCustomFieldResult(hits, CUSTOM_DOC_BOOLEAN_PARAM_FIELD, docBooleanParamValue, "boolean");
                verifyCustomFieldResult(hits, CUSTOM_DOC_DATE_PARAM_FIELD, docDateParamValue, "date");

                //  Delete test document after verification is complete.
                deleteDocument(transportClient, ES_INDEX, USER_ID_FIELD, USER_ID);
            }
        }
    }

    private static SearchHit[] searchDocument(TransportClient client, String index, String type, String field, String value){
        SearchResponse response = client.prepareSearch(index.toLowerCase())
                .setTypes(type)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery(field, value.toLowerCase()))
                .setFrom(0).setSize(60).setExplain(true)
                .execute()
                .actionGet();

        return response.getHits().getHits();
    }

    private static void verifyFixedFieldResult(SearchHit[] results, String field, String expectedValue){
        Map<String,Object> result = results[0].getSource();

        Object actualFieldValue = null;

        //  Identify matching field in search results.
        for (Map.Entry<String, Object> entry : result.entrySet()) {

            //  Allow for type suffixes appended to field name in ES.
            if (entry.getKey().startsWith(field)) {
                actualFieldValue = entry.getValue();
                break;
            }
        }

        //  Assert result is not null and matches expected value.
        Assert.assertNotNull(actualFieldValue);
        Assert.assertEquals(expectedValue, actualFieldValue);
    }

    private static void verifyCustomFieldResult(SearchHit[] results, String field, Object expectedValue, String type) throws ParseException {
        //  Determine entry key to look for based on type supplied.
        switch(type.toLowerCase()) {
            case "string":
                field = field + KEYWORD_SUFFIX;
                break;
            case "short":
                field = field + SHORT_SUFFIX;
                break;
            case "int":
                field = field + INT_SUFFIX;
                break;
            case "long":
                field = field + LONG_SUFFIX;
                break;
            case "float":
                field = field + FLOAT_SUFFIX;
                break;
            case "double":
                field = field + DOUBLE_SUFFIX;
                break;
            case "boolean":
                field = field + BOOLEAN_SUFFIX;
                break;
            case "date":
                field = field + DATE_SUFFIX;
                break;
        }

        Map<String,Object> result = results[0].getSource();

        Object actualFieldValue = null;

        //  Identify matching field in search results.
        for (Map.Entry<String, Object> entry : result.entrySet()) {

            //  Allow for type suffixes appended to field name in ES.
            if (entry.getKey().equals(field)) {
                actualFieldValue = entry.getValue();
                break;
            }
        }

        //  Assert result is not null and matches expected value.
        Assert.assertNotNull(actualFieldValue);
        if (!type.toLowerCase().equals("date")) {
            Assert.assertEquals(expectedValue.toString(), actualFieldValue.toString());
        } else {
            Assert.assertTrue(datesAreEqual((Date)expectedValue, actualFieldValue.toString()));
        }
    }

    private static boolean datesAreEqual(Date expectedDate, String actualDateString) throws ParseException {
        //  Convert expected date to similar format used in Elasticsearch search results (default ISODateTimeFormat.dateOptionalTimeParser).
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String expectedDateSting = df.format(expectedDate);

        return expectedDateSting.equals(actualDateString);
    }

    private static void deleteDocument(TransportClient client, String index, String field, String value){
        //   Delete documents based on the provided field and value.
        BulkByScrollResponse response =
                DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                        .filter(QueryBuilders.matchQuery(field, value))
                        .source(index.toLowerCase())
                        .get();

        //  Only expecting a single document to be deleted.
        Assert.assertTrue(1 == response.getDeleted());
    }
}
