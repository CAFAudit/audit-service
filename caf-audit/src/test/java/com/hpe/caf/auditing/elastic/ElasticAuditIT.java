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
import com.hpe.caf.auditing.AuditEventBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class ElasticAuditIT
{

    private static final String APPLICATION_ID = "aTestApplication";
    private static final String TENANT_ID = "tTestTenant";
    private static String USER_ID;
    private static final String EVENT_CATEGORY_ID = "evDocument";
    private static final String EVENT_TYPE_ID = "etView";
    private static final String CORRELATION_ID = "cTestCorrelation";

    private static final String ES_INDEX_PREFIX = "audit_tenant_";
    private static final String ES_INDEX = ES_INDEX_PREFIX + TENANT_ID;
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

    static class RetryElasticsearchOperation
    {
        public static final int DEFAULT_RETRIES = 5;
        public static final long DEFAULT_WAIT_TIME_MS = 1000;

        private int numberOfRetries;
        private int numberOfTriesLeft;
        private long timeToWait;

        public RetryElasticsearchOperation()
        {
            this(DEFAULT_RETRIES, DEFAULT_WAIT_TIME_MS);
        }

        public RetryElasticsearchOperation(int numberOfRetries, long timeToWait)
        {
            this.numberOfRetries = numberOfRetries;
            numberOfTriesLeft = numberOfRetries;
            this.timeToWait = timeToWait;
        }

        public boolean shouldRetry()
        {
            return numberOfTriesLeft > 0;
        }

        public void retryNeeded() throws Exception
        {
            numberOfTriesLeft--;
            if (!shouldRetry()) {
                throw new Exception("Retry Failed: Total " + numberOfRetries
                    + " attempts made at interval " + getTimeToWait()
                    + "ms");
            }
            waitUntilNextTry();
        }

        public long getTimeToWait()
        {
            return timeToWait;
        }

        private void waitUntilNextTry()
        {
            try {
                Thread.sleep(getTimeToWait());
            } catch (InterruptedException ignored) {
            }
        }
    }

    @BeforeClass
    public static void setup() throws Exception
    {
        ES_HOSTNAME = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
        ES_PORT = Integer.parseInt(System.getProperty("es.port", System.getenv("es.port")));
        ES_CLUSTERNAME = System.getProperty("es.cluster.name", System.getenv("es.cluster.name"));
        USER_ID = UUID.randomUUID().toString();
    }

    @Test(expected = ConfigurationException.class)
    public void testUnknownESHost() throws Exception
    {
        //  This tests the usage of an unknown host.
        //  An exception is expected to be thrown.

        final String esHostAndPort = "unknown:" + ES_PORT;
        final AuditConnection auditConnection = AuditConnectionHelper.getAuditConnection(esHostAndPort, ES_CLUSTERNAME);
    }

    @Test(expected = Exception.class)
    public void testIncorrectESPort() throws Exception
    {
        //  This tests the usage of an unexpected port number for the ES config.
        //  An exception is expected to be thrown.

        final String esHostAndPort = ES_HOSTNAME + ":10000";

        try (
            AuditConnection auditConnection = AuditConnectionHelper.getAuditConnection(esHostAndPort, ES_CLUSTERNAME);
            com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {
            //  Index a sample audit event message into Elasticsearch.
            AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(APPLICATION_ID);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            auditEventBuilder.setTenant(TENANT_ID);
            auditEventBuilder.setUser(USER_ID);

            //  No need to set up custom data as we expect the call to index the document to fail because
            //  of the unexpected port used.
            auditEventBuilder.send();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTenantId() throws Exception
    {
        //  This tests the usage of invalid characters (i.e. commas) in
        //  the tenant identifier. The tenant identifier is part of the ES
        //  index name and therefore must not contain commas.
        //  An IllegalArgumentException is expected to be thrown.

        final String esHostAndPort = ES_HOSTNAME + ":" + ES_PORT;
        final String invalidTenantIdContainingCommas = "t,test,tenant";

        try (
            AuditConnection auditConnection = AuditConnectionHelper.getAuditConnection(esHostAndPort, ES_CLUSTERNAME);
            com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {
            //  Index a sample audit event message into Elasticsearch.
            AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(APPLICATION_ID);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            auditEventBuilder.setTenant(invalidTenantIdContainingCommas);
        }
    }

    @Test
    public void testESIndexing() throws Exception
    {
        //  This tests the successful indexing of a sample audit event
        //  message into ES. It firstly indexes the message into ES. It then
        //  searches ES for the newly indexed document and verifies the field
        //  data indexed into ES. Afterwards the document is removed from ES.

        final String esHostAndPort = ES_HOSTNAME + ":" + ES_PORT;

        try (
            AuditConnection auditConnection = AuditConnectionHelper.getAuditConnection(esHostAndPort, ES_CLUSTERNAME);
            com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {

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
            auditEventBuilder.addEventParameter(CUSTOM_DOC_STRING_PARAM_FIELD, CUSTOM_DOC_STRING_PARAM_FIELD, docStringParamValue);
            int docIntParamValue = rand.nextInt();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_INT_PARAM_FIELD, CUSTOM_DOC_INT_PARAM_FIELD, docIntParamValue);
            short docShortParamValue = (short) rand.nextInt(Short.MAX_VALUE + 1);
            auditEventBuilder.addEventParameter(CUSTOM_DOC_SHORT_PARAM_FIELD, CUSTOM_DOC_SHORT_PARAM_FIELD, docShortParamValue);
            long docLongParamValue = rand.nextLong();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_LONG_PARAM_FIELD, CUSTOM_DOC_LONG_PARAM_FIELD, docLongParamValue);
            float docFloatParamValue = rand.nextFloat();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_FLOAT_PARAM_FIELD, CUSTOM_DOC_FLOAT_PARAM_FIELD, docFloatParamValue);
            double docDoubleParamValue = rand.nextDouble();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_DOUBLE_PARAM_FIELD, CUSTOM_DOC_DOUBLE_PARAM_FIELD, docDoubleParamValue);
            boolean docBooleanParamValue = rand.nextBoolean();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_BOOLEAN_PARAM_FIELD, CUSTOM_DOC_BOOLEAN_PARAM_FIELD, docBooleanParamValue);
            Date docDateParamValue = new Date();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_DATE_PARAM_FIELD, CUSTOM_DOC_DATE_PARAM_FIELD, docDateParamValue);

            //  Send audit event message to Elasticsearch.
            auditEventBuilder.send();

            //  Search for the audit event message in Elasticsearch and verify
            //  field data matches input.
            try (TransportClient transportClient
                = ElasticAuditTransportClientFactory.getTransportClient(esHostAndPort, ES_CLUSTERNAME)) {

                SearchHit[] hits = new SearchHit[0];
                hits = searchDocumentInIndex(transportClient, ES_INDEX, USER_ID_FIELD.concat(KEYWORD_SUFFIX), USER_ID);

                //  Expecting a single hit.
                Assert.assertTrue(hits.length == 1);

                //  Make a note of the document identifier as we will use this to clean
                //  up afterwards.
                final String docId = hits[0].getId();

                //  Verify fixed field data results.
                verifyFieldResult(hits, APP_ID_FIELD, APPLICATION_ID, "string");
                verifyFieldResult(hits, EVENT_CATEGORY_ID_FIELD, EVENT_CATEGORY_ID, "string");
                verifyFieldResult(hits, EVENT_TYPE_ID_FIELD, EVENT_TYPE_ID, "string");
                verifyFieldResult(hits, USER_ID_FIELD, USER_ID, "string");
                verifyFieldResult(hits, CORRELATION_ID_FIELD, CORRELATION_ID, "string");

                //  Verify fixed field data results.
                verifyFieldResult(hits, CUSTOM_DOC_STRING_PARAM_FIELD, docStringParamValue, "string");
                verifyFieldResult(hits, CUSTOM_DOC_INT_PARAM_FIELD, docIntParamValue, "int");
                verifyFieldResult(hits, CUSTOM_DOC_SHORT_PARAM_FIELD, docShortParamValue, "short");
                verifyFieldResult(hits, CUSTOM_DOC_LONG_PARAM_FIELD, docLongParamValue, "long");
                verifyFieldResult(hits, CUSTOM_DOC_FLOAT_PARAM_FIELD, docFloatParamValue, "float");
                verifyFieldResult(hits, CUSTOM_DOC_DOUBLE_PARAM_FIELD, docDoubleParamValue, "double");
                verifyFieldResult(hits, CUSTOM_DOC_BOOLEAN_PARAM_FIELD, docBooleanParamValue, "boolean");
                verifyFieldResult(hits, CUSTOM_DOC_DATE_PARAM_FIELD, docDateParamValue, "date");

                //  Delete test document after verification is complete.
                deleteDocument(transportClient, ES_INDEX, docId);
            }
        }
    }

    @Test
    public void testMultiTenantESIndexing() throws Exception
    {
        //  This tests the successful indexing of sample audit event messages for two different tenant's into ES.
        //  It indexes each tenant audit event message into ES. It then searches ES for each newly indexed tenant
        //  document and verifies the field data for each tenant document indexed into ES . Afterwards each document is
        //  removed from ES.

        final String esHostAndPort = ES_HOSTNAME + ":" + ES_PORT;

        try (
                AuditConnection auditConnection = AuditConnectionHelper.getAuditConnection(esHostAndPort,
                        ES_CLUSTERNAME);
                com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {

            //  Index a sample audit event message into Elasticsearch.
            AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

            String testApplicationId = UUID.randomUUID().toString();

            String tenant1Id = TENANT_ID + 1;

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(testApplicationId);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            auditEventBuilder.setTenant(tenant1Id);
            auditEventBuilder.setUser(USER_ID);

            //  Send audit event message to Elasticsearch.
            auditEventBuilder.send();

            String tenant2Id = TENANT_ID + 2;

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(testApplicationId);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            auditEventBuilder.setTenant(tenant2Id);
            auditEventBuilder.setUser(USER_ID);

            //  Send audit event message to Elasticsearch.
            auditEventBuilder.send();

            // Search across all indices for the applicationId field in Elasticsearch and verify that the expected
            // number of hits are returned.
            try (TransportClient transportClient
                         = ElasticAuditTransportClientFactory.getTransportClient(esHostAndPort, ES_CLUSTERNAME)) {

                String[] tenantIndexIds = new String[2];
                tenantIndexIds[0] = ES_INDEX_PREFIX + tenant1Id;
                tenantIndexIds[1] = ES_INDEX_PREFIX + tenant2Id;

                SearchHit[] tenantIndicesHits = searchDocumentInIndices(transportClient, tenantIndexIds,
                        APP_ID_FIELD.concat(KEYWORD_SUFFIX), testApplicationId);

                //  Expecting a two hits.
                Assert.assertTrue(tenantIndicesHits.length == 2);

                //  Delete test indexes after verification is complete.
                deleteIndices(transportClient, tenantIndexIds);
            }
        }
    }

    private void deleteIndices(TransportClient client, String[] indices) {
        // Lowercase each string in the array of indices
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indices[i].toLowerCase();
        }

        RetryElasticsearchOperation retryDelete = new RetryElasticsearchOperation();
        while (retryDelete.shouldRetry()) {
            try {
                boolean didElasticAckDelete = client.admin().indices().delete(
                        new DeleteIndexRequest(indices)).get().isAcknowledged();

                if (didElasticAckDelete) {
                    // If Elastic acknowledged our delete wait a second to allow it time to delete the indices
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

    private static SearchHit[] searchDocumentInIndices(TransportClient client, String[] indices, String field,
                                                       String value)
    {
        // Lowercase each string in the array of indices
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indices[i].toLowerCase();
        }

        RetryElasticsearchOperation retrySearch = new RetryElasticsearchOperation();
        SearchHit[] hits = null;
        while (retrySearch.shouldRetry()) {
            hits = client.prepareSearch(indices)
                    .setTypes(ES_TYPE)
                    .setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setQuery(QueryBuilders.matchQuery(field, value.toLowerCase()))
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

    private static SearchHit[] searchDocumentInIndex(TransportClient client, String indexId, String field, String value)
    {
        RetryElasticsearchOperation retrySearch = new RetryElasticsearchOperation();
        SearchHit[] hits = null;
        while (retrySearch.shouldRetry()) {
            hits = client.prepareSearch(indexId.toLowerCase())
                    .setTypes(ES_TYPE)
                    .setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setQuery(QueryBuilders.matchQuery(field, value.toLowerCase()))
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

    private static void verifyFieldResult(SearchHit[] results, String field, Object expectedValue, String type)
        throws ParseException
    {
        //  Determine entry key to look for based on type supplied.
        switch (type.toLowerCase()) {
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
            Assert.assertEquals(expectedValue.toString(), actualFieldValue.toString());
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

    private static void deleteDocument(TransportClient client, String indexId, String documentId)
    {
        RetryElasticsearchOperation retryDelete = new RetryElasticsearchOperation();
        while (retryDelete.shouldRetry()) {
            // Delete document by id.
            DeleteResponse response = client
                    .prepareDelete()
                    .setIndex(indexId.toLowerCase())
                    .setType(ES_TYPE)
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

    private static void deleteIndex(TransportClient client, String indexId)
    {
        RetryElasticsearchOperation retryDelete = new RetryElasticsearchOperation();
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
