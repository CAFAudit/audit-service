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
package com.hpe.caf.services.audit.api;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.AuditIndexingHint;
import com.hpe.caf.auditing.elastic.ElasticAuditConstants;
import com.hpe.caf.auditing.elastic.ElasticAuditRetryOperation;
import com.hpe.caf.auditing.elastic.ElasticAuditTransportClientFactory;
import com.hpe.caf.auditing.webserviceclient.WebServiceClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class WebserviceClientAuditIT {

    private static final Logger LOG = LogManager.getLogger(WebserviceClientAuditIT.class.getName());

    private static final String APPLICATION_ID = "aTestApplication";
    private static final String TENANT_ID = "tTestTenant";
    private static final String USER_ID = "aTestUser@testcompany.com";
    private static final String EVENT_CATEGORY_ID = "evDocument";
    private static final String EVENT_TYPE_ID = "etView";
    private static final String CORRELATION_ID = "cTestCorrelation";

    private static final String ES_INDEX = TENANT_ID + ElasticAuditConstants.Index.SUFFIX;

    private static final String CUSTOM_DOC_STRING_PARAM_FIELD = "docStringParam";
    private static final String CUSTOM_DOC_INT_PARAM_FIELD = "docIntParam";
    private static final String CUSTOM_DOC_SHORT_PARAM_FIELD = "docShortParam";
    private static final String CUSTOM_DOC_LONG_PARAM_FIELD = "docLongParam";
    private static final String CUSTOM_DOC_BOOLEAN_PARAM_FIELD = "docBooleanParam";
    private static final String CUSTOM_DOC_FLOAT_PARAM_FIELD = "docFloatParam";
    private static final String CUSTOM_DOC_DOUBLE_PARAM_FIELD = "docDoubleParam";
    private static final String CUSTOM_DOC_DATE_PARAM_FIELD = "docDateParam";

    private static String WS_HOSTNAME;
    private static String WS_ENDPOINT;
    private static int WS_PORT;

    private static String ES_HOSTNAME;
    private static int ES_PORT;
    private static String ES_HOSTNAME_AND_PORT;
    private static String ES_CLUSTERNAME;

    /**
     * Class that enables overriding of environment variables without effecting the environment variables set on the
     * host
     */
    static class TestEnvironmentVariablesOverrider {
        @SuppressWarnings("unchecked")
        public static void configureEnvironmentVariable(String name, String value) throws Exception {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.put(name, value);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
                    .getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.put(name, value);
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        // Test the Auditing library in webservice mode
        System.setProperty("AUDIT_LIB_MODE", "webservice");

        TestEnvironmentVariablesOverrider.configureEnvironmentVariable("no_proxy", "");
        TestEnvironmentVariablesOverrider.configureEnvironmentVariable("http_proxy", "");
        TestEnvironmentVariablesOverrider.configureEnvironmentVariable("https_proxy", "");

        WS_HOSTNAME = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
        WS_PORT = Integer.parseInt(System.getProperty("webservice.adminport", System.getenv("webservice.adminport")));
        WS_ENDPOINT = String.format("http://%s:%s/caf-audit-service/v1", WS_HOSTNAME, WS_PORT);

        ES_HOSTNAME = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
        ES_PORT = Integer.parseInt(System.getProperty("elasticsearch.transport.port", System.getenv("elasticsearch.transport.port")));
        ES_CLUSTERNAME = System.getProperty("CAF_ELASTIC_CLUSTER_NAME", System.getenv("CAF_ELASTIC_CLUSTER_NAME"));
        ES_HOSTNAME_AND_PORT = String.format("%s:%s", ES_HOSTNAME, ES_PORT);
    }

    @AfterMethod
    public void cleanUp() throws ConfigurationException {
        TransportClient transportClient
                     = ElasticAuditTransportClientFactory.getTransportClient(ES_HOSTNAME_AND_PORT, ES_CLUSTERNAME);
        try {
            deleteIndex(transportClient, ES_INDEX);
        } catch (RuntimeException rte) {
            LOG.warn("Unable to delete tenant index. It may not exist.");
        }
    }

    @Test
    public void testWebserviceClient() throws Exception {

        AuditConnection auditConnection = AuditConnectionHelper.getWebServiceAuditConnection(WS_ENDPOINT);
        AuditChannel auditChannel = auditConnection.createChannel();

        // Create new Audit Event Builder
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
        auditEventBuilder.addEventParameter(CUSTOM_DOC_STRING_PARAM_FIELD, null, docStringParamValue,
                AuditIndexingHint.FULLTEXT);
        auditEventBuilder.addEventParameter(CUSTOM_DOC_STRING_PARAM_FIELD, null, docStringParamValue);
        int docIntParamValue = rand.nextInt();
        auditEventBuilder.addEventParameter(CUSTOM_DOC_INT_PARAM_FIELD, null, docIntParamValue);
        short docShortParamValue = (short) rand.nextInt(Short.MAX_VALUE + 1);
        auditEventBuilder.addEventParameter(CUSTOM_DOC_SHORT_PARAM_FIELD, null, docShortParamValue);
        long docLongParamValue = rand.nextLong();
        auditEventBuilder.addEventParameter(CUSTOM_DOC_LONG_PARAM_FIELD, null, docLongParamValue);
        float docFloatParamValue = rand.nextFloat();
        auditEventBuilder.addEventParameter(CUSTOM_DOC_FLOAT_PARAM_FIELD, null, docFloatParamValue);
        double docDoubleParamValue = rand.nextDouble();
        auditEventBuilder.addEventParameter(CUSTOM_DOC_DOUBLE_PARAM_FIELD, null, docDoubleParamValue);
        boolean docBooleanParamValue = rand.nextBoolean();
        auditEventBuilder.addEventParameter(CUSTOM_DOC_BOOLEAN_PARAM_FIELD, null, docBooleanParamValue);
        Date docDateParamValue = new Date();
        auditEventBuilder.addEventParameter(CUSTOM_DOC_DATE_PARAM_FIELD, null, docDateParamValue);

        //  Send audit event message to Elasticsearch.
        auditEventBuilder.send();

        //  Verify the type mappings have been set for the index. Then search for the audit event message in
        //  Elasticsearch and verify field data matches input.
        try (TransportClient transportClient
                     = ElasticAuditTransportClientFactory.getTransportClient(ES_HOSTNAME_AND_PORT, ES_CLUSTERNAME)) {

            verifyTypeMappings(transportClient);

            SearchHit[] hits = new SearchHit[0];
            hits = searchDocumentInIndex(transportClient,
                    ES_INDEX,
                    ElasticAuditConstants.FixedFieldName.USER_ID_FIELD,
                    USER_ID);

            //  Expecting a single hit.
            Assert.assertTrue(hits.length == 1);

            //  Make a note of the document identifier as we will use this to clean
            //  up afterwards.
            final String docId = hits[0].getId();

            //  Verify fixed field data results.
            verifyFixedFieldResult(hits, ElasticAuditConstants.FixedFieldName.APPLICATION_ID_FIELD, APPLICATION_ID, "string");
            verifyFixedFieldResult(hits, ElasticAuditConstants.FixedFieldName.EVENT_CATEGORY_ID_FIELD, EVENT_CATEGORY_ID, "string");
            verifyFixedFieldResult(hits, ElasticAuditConstants.FixedFieldName.EVENT_TYPE_ID_FIELD, EVENT_TYPE_ID, "string");
            verifyFixedFieldResult(hits, ElasticAuditConstants.FixedFieldName.USER_ID_FIELD, USER_ID, "string");
            verifyFixedFieldResult(hits, ElasticAuditConstants.FixedFieldName.CORRELATION_ID_FIELD, CORRELATION_ID, "string");

            //  Verify fixed field data results.
            verifyCustomFieldResult(hits, CUSTOM_DOC_STRING_PARAM_FIELD, docStringParamValue, "string",
                    AuditIndexingHint.FULLTEXT);
            verifyCustomFieldResult(hits, CUSTOM_DOC_STRING_PARAM_FIELD, docStringParamValue, "string",
                    AuditIndexingHint.KEYWORD);
            verifyCustomFieldResult(hits, CUSTOM_DOC_INT_PARAM_FIELD, docIntParamValue, "int", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_SHORT_PARAM_FIELD, docShortParamValue, "short", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_LONG_PARAM_FIELD, docLongParamValue, "long", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_FLOAT_PARAM_FIELD, docFloatParamValue, "float", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_DOUBLE_PARAM_FIELD, docDoubleParamValue, "double", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_BOOLEAN_PARAM_FIELD, docBooleanParamValue, "boolean", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_DATE_PARAM_FIELD, docDateParamValue, "date", null);

            //  Delete test document after verification is complete.
            deleteDocument(transportClient, ES_INDEX, docId);
        }
    }

    @Test
    public void eventOrderTest() throws Exception{
        int event1Order;
        int event2Order;

        try (
                AuditConnection auditConnection =
                        AuditConnectionHelper.getWebServiceAuditConnection(WS_ENDPOINT);
                com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {
            {
                Date date = new Date();
                String correlationId = UUID.randomUUID().toString();
                AuditLog.auditTestEvent1(auditChannel, TENANT_ID, "user1", correlationId,
                        "stringType1", "stringType2", "stringType3", "stringType4",
                        Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true,
                        date);

                SearchHit searchHit = getAuditEvent(correlationId);
                Map<String, Object> source = searchHit.getSource();
                event1Order = (Integer) source.get(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD);
            }

            {
                Date date = new Date();
                String correlationId = UUID.randomUUID().toString();
                AuditLog.auditTestEvent1(auditChannel, TENANT_ID, "user1", correlationId,
                        "stringType1", "stringType2", "stringType3", "stringType4",
                        Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true,
                        date);

                SearchHit searchHit = getAuditEvent(correlationId);
                Map<String, Object> source = searchHit.getSource();
                event2Order = (Integer) source.get(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD);
            }

            Assert.assertTrue(event1Order < event2Order, "Event 1 order was not less than event 2 order");
        }
    }

    @Test(expectedExceptions = WebServiceClientException.class)
    public void testWebserviceClientBadAuditEvent() throws Exception {

        AuditConnection auditConnection = AuditConnectionHelper.getWebServiceAuditConnection(WS_ENDPOINT);
        AuditChannel auditChannel = auditConnection.createChannel();

        // Create new Audit Event Builder
        AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();
        //  Set up fixed field data for the sample audit event message.
        auditEventBuilder.setApplication(APPLICATION_ID);
        auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
        auditEventBuilder.setCorrelationId(CORRELATION_ID);
        auditEventBuilder.setTenant(TENANT_ID);
        auditEventBuilder.setUser(USER_ID);

        //  Send audit event message to Elasticsearch.
        auditEventBuilder.send();
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

            org.junit.Assert.assertEquals("Expected search result not found", 1, searchHits.getTotalHits());

            return searchHits.getHits()[0];
        }
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

    private static SearchHit[] searchDocumentInIndex(TransportClient client, String indexId, String field, String value)
    {
        ElasticAuditRetryOperation retrySearch = new ElasticAuditRetryOperation();
        SearchHit[] hits = null;
        while (retrySearch.shouldRetry()) {
            hits = client.prepareSearch(indexId.toLowerCase())
                    .setTypes(ElasticAuditConstants.Index.TYPE)
                    .setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setQuery(QueryBuilders.matchQuery(field, value))
                    .setFrom(0).setSize(10)
                    .execute()
                    .actionGet().getHits().getHits();

            if (hits.length > 0) {
                break;
            }

            //  No search hits just yet. Retry.
            try {
                retrySearch.retryNeeded();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return hits;
    }

    private static void deleteDocument(TransportClient client, String indexId, String documentId)
    {
        ElasticAuditRetryOperation retryDelete = new ElasticAuditRetryOperation();
        while (retryDelete.shouldRetry()) {
            // Delete document by id.
            DeleteResponse response = client
                    .prepareDelete()
                    .setIndex(indexId.toLowerCase())
                    .setType(ElasticAuditConstants.Index.TYPE)
                    .setId(documentId)
                    .execute()
                    .actionGet();

            if (response.status() == RestStatus.OK) {
                break;
            }

            // Retry deletion status is not OK.
            try {
                retryDelete.retryNeeded();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void verifyTypeMappings(TransportClient transportClient) {
        String expectedTypeMappings = "{\"cafAuditEvent\":{\"dynamic_templates\":[" +
                "{\"CAFAuditKeyword\":{\"match\":\"*_CAKyw\",\"mapping\":{\"type\":\"keyword\"}}}," +
                "{\"CAFAuditText\":{\"match\":\"*_CATxt\",\"mapping\":{\"type\":\"text\"}}}," +
                "{\"CAFAuditLong\":{\"match\":\"*_CALng\",\"mapping\":{\"type\":\"long\"}}}," +
                "{\"CAFAuditInteger\":{\"match\":\"*_CAInt\",\"mapping\":{\"type\":\"integer\"}}}," +
                "{\"CAFAuditShort\":{\"match\":\"*_CAShort\",\"mapping\":{\"type\":\"short\"}}}," +
                "{\"CAFAuditDouble\":{\"match\":\"*_CADbl\",\"mapping\":{\"type\":\"double\"}}}," +
                "{\"CAFAuditFloat\":{\"match\":\"*_CAFlt\",\"mapping\":{\"type\":\"float\"}}}," +
                "{\"CAFAuditDate\":{\"match\":\"*_CADte\",\"mapping\":{\"type\":\"date\"}}}," +
                "{\"CAFAuditBoolean\":{\"match\":\"*_CABln\",\"mapping\":{\"type\":\"boolean\"}}}]," +
                "\"properties\":{\"applicationId\":{\"type\":\"keyword\"},\"correlationId\":{\"type\":\"keyword\"}," +
                "\"docBooleanParam_CABln\":{\"type\":\"boolean\"},\"docDateParam_CADte\":{\"type\":\"date\"}," +
                "\"docDoubleParam_CADbl\":{\"type\":\"double\"},\"docFloatParam_CAFlt\":{\"type\":\"float\"}," +
                "\"docIntParam_CAInt\":{\"type\":\"integer\"},\"docLongParam_CALng\":{\"type\":\"long\"}," +
                "\"docShortParam_CAShort\":{\"type\":\"short\"},\"docStringParam_CAKyw\":{\"type\":\"keyword\"}," +
                "\"docStringParam_CATxt\":{\"type\":\"text\"},\"eventCategoryId\":{\"type\":\"keyword\"}," +
                "\"eventOrder\":{\"type\":\"long\"},\"eventTime\":{\"type\":\"date\"}," +
                "\"eventTimeSource\":{\"type\":\"keyword\"},\"eventTypeId\":{\"type\":\"keyword\"}," +
                "\"processId\":{\"type\":\"keyword\"},\"threadId\":{\"type\":\"long\"}," +
                "\"userId\":{\"type\":\"keyword\"}}}}";

        ClusterStateResponse clusterStateResponse =
                transportClient.admin().cluster().prepareState().execute().actionGet();

        // Get the CAF Audit Event type mapping for the tenant index
        CompressedXContent indexMapping = clusterStateResponse.getState().getMetaData()
                .index((TENANT_ID + ElasticAuditConstants.Index.SUFFIX).toLowerCase())
                .getMappings()
                .get(ElasticAuditConstants.Index.TYPE)
                .source();

        Assert.assertEquals(indexMapping.toString(), expectedTypeMappings,
                "Expected type mappings and actual type mappings should match");
    }

    private static void verifyFixedFieldResult(SearchHit[] results, String field, Object expectedValue, String type)
            throws ParseException
    {
        Map<String, Object> result = results[0].getSource();

        Object actualFieldValue = null;

        //  Identify matching field in search results.
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            if (entry.getKey().equals(field)) {
                actualFieldValue = entry.getValue();
                break;
            }
        }

        //  Assert result is not null and matches expected value.
        Assert.assertNotNull(actualFieldValue);
        if (!type.toLowerCase().equals("date")) {
            Assert.assertEquals(actualFieldValue.toString(), expectedValue.toString());
        } else {
            Assert.assertTrue(datesAreEqual((Date) expectedValue, actualFieldValue.toString()));
        }
    }

    private static void verifyCustomFieldResult(SearchHit[] results, String field, Object expectedValue, String type,
                                                AuditIndexingHint indexingHint) throws ParseException
    {
        //  Determine entry key to look for based on type supplied.
        switch (type.toLowerCase()) {
            case "string":
                if (indexingHint != null) {
                    if (indexingHint == AuditIndexingHint.KEYWORD) {
                        field = field + ElasticAuditConstants.CustomFieldSuffix.KEYWORD_SUFFIX;
                    } else {
                        field = field + ElasticAuditConstants.CustomFieldSuffix.TEXT_SUFFIX;
                    }
                } else {
                    throw new RuntimeException("An indexing hint has not been specified.");
                }
                break;
            case "short":
                field = field + ElasticAuditConstants.CustomFieldSuffix.SHORT_SUFFIX;
                break;
            case "int":
                field = field + ElasticAuditConstants.CustomFieldSuffix.INT_SUFFIX;
                break;
            case "long":
                field = field + ElasticAuditConstants.CustomFieldSuffix.LONG_SUFFIX;
                break;
            case "float":
                field = field + ElasticAuditConstants.CustomFieldSuffix.FLOAT_SUFFIX;
                break;
            case "double":
                field = field + ElasticAuditConstants.CustomFieldSuffix.DOUBLE_SUFFIX;
                break;
            case "boolean":
                field = field + ElasticAuditConstants.CustomFieldSuffix.BOOLEAN_SUFFIX;
                break;
            case "date":
                field = field + ElasticAuditConstants.CustomFieldSuffix.DATE_SUFFIX;
                break;
        }

        Map<String, Object> result = results[0].getSource();

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
            Assert.assertEquals(actualFieldValue.toString(), expectedValue.toString());
        } else {
            Assert.assertTrue(datesAreEqual((Date) expectedValue, actualFieldValue.toString()));
        }
    }

    private static boolean datesAreEqual(Date expectedDate, String actualDateString) throws ParseException
    {
        //  Convert expected date to similar format used in Elasticsearch search results
        //  (i.e. default ISODateTimeFormat.dateOptionalTimeParser).
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String expectedDateSting = df.format(expectedDate);

        return expectedDateSting.equals(actualDateString);
    }
}
