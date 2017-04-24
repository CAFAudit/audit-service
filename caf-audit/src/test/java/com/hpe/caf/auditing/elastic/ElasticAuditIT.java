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
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
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
                RetryElasticsearchOperation retrySearch = new RetryElasticsearchOperation();
                while (retrySearch.shouldRetry()) {
                    hits = searchDocument(transportClient, USER_ID_FIELD.concat(KEYWORD_SUFFIX), USER_ID);

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
                deleteDocument(transportClient, docId);
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

            String tenant1Id = TENANT_ID + 1;

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(APPLICATION_ID);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            auditEventBuilder.setTenant(tenant1Id);
            auditEventBuilder.setUser(USER_ID);

            //  Send audit event message to Elasticsearch.
            auditEventBuilder.send();

            String tenant2Id = TENANT_ID + 2;

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(APPLICATION_ID);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            auditEventBuilder.setTenant(tenant2Id);
            auditEventBuilder.setUser(USER_ID);

            //  Send audit event message to Elasticsearch.
            auditEventBuilder.send();

            //  Search for the audit event message in Elasticsearch and verify
            //  field data matches input.
            try (TransportClient transportClient
                         = ElasticAuditTransportClientFactory.getTransportClient(esHostAndPort, ES_CLUSTERNAME)) {

                SearchHit[] hits = new SearchHit[0];
                RetryElasticsearchOperation retrySearch = new RetryElasticsearchOperation();
                hits = getSearchHitsForTenantIndex(tenant1Id, transportClient, hits, retrySearch);

                //  Expecting a single hit.
                Assert.assertTrue(hits.length == 1);

                //  Make a note of the document identifier as we will use this to clean
                //  up afterwards.
                final String tenant1DocId = hits[0].getId();

                retrySearch = new RetryElasticsearchOperation();
                hits = getSearchHitsForTenantIndex(tenant2Id, transportClient, hits, retrySearch);

                //  Expecting a single hit.
                Assert.assertTrue(hits.length == 1);

                //  Make a note of the document identifier as we will use this to clean
                //  up afterwards.
                final String tenant2DocId = hits[0].getId();

                //  Delete test documents after verification is complete.
                deleteTenantDocument(transportClient, tenant1Id, tenant1DocId);
                deleteTenantDocument(transportClient, tenant2Id, tenant2DocId);
            }
        }
    }

    private SearchHit[] getSearchHitsForTenantIndex(String tenantId, TransportClient transportClient, SearchHit[] hits, RetryElasticsearchOperation retrySearch) {
        while (retrySearch.shouldRetry()) {
            hits = searchDocumentInIndex(transportClient, tenantId, USER_ID_FIELD.concat(KEYWORD_SUFFIX), USER_ID);

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

    @Test
    public void testAuditEventsSearchingFieldValueIsNotCaseSensitive() throws Exception
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

            //  Send audit event message to Elasticsearch.
            auditEventBuilder.send();

            //  Search for the audit event message in Elasticsearch and verify
            //  field data matches input.
            try (TransportClient transportClient
                = ElasticAuditTransportClientFactory.getTransportClient(esHostAndPort, ES_CLUSTERNAME)) {

                SearchHit[] hits = new SearchHit[0];
                RetryElasticsearchOperation retrySearch = new RetryElasticsearchOperation();
                while (retrySearch.shouldRetry()) {
                    // Upper case the userId value to search for to prove case sensitivity is not required for matching
                    hits = searchDocument(transportClient, USER_ID_FIELD.concat(KEYWORD_SUFFIX), USER_ID.toUpperCase());

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

                //  Expecting a single hit.
                Assert.assertTrue(hits.length == 1);

                //  Delete test document after verification is complete.
                deleteDocument(transportClient, hits[0].getId());
            }
        }
    }

    @Test
    public void testAuditEventsSearchingFieldValueIsCharacterSensitive() throws Exception
    {
        //  This test indexes the message into ES. It then searches ES for the newly indexed document with field and
        // value that contains unsupported characters, it verifies that the field value indexed into ES is not
        // searchable because it contains unsupported characters. Afterwards the document is removed from ES.

        final String esHostAndPort = ES_HOSTNAME + ":" + ES_PORT;

        try (
                AuditConnection auditConnection = AuditConnectionHelper.getAuditConnection(esHostAndPort, ES_CLUSTERNAME);
                com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {

            //  Index a sample audit event message into Elasticsearch.
            AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

            String appIdWithUnsupportedCharacters = "^:¬`¦!\"£$%&*()_+-=/\\|{}:@~<>?[];'#,./\\" + APPLICATION_ID;

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(appIdWithUnsupportedCharacters);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            auditEventBuilder.setTenant(TENANT_ID);
            auditEventBuilder.setUser(USER_ID);

            //  Send audit event message to Elasticsearch.
            auditEventBuilder.send();

            //  Search for the audit event message in Elasticsearch and verify
            //  field data matches input.
            try (TransportClient transportClient
                         = ElasticAuditTransportClientFactory.getTransportClient(esHostAndPort, ES_CLUSTERNAME)) {

                SearchHit[] hits = new SearchHit[0];
                RetryElasticsearchOperation retrySearch = new RetryElasticsearchOperation();
                while (retrySearch.shouldRetry()) {
                    hits = searchDocument(transportClient, APP_ID_FIELD.concat(KEYWORD_SUFFIX),
                            appIdWithUnsupportedCharacters);

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

                //  Expecting a single hit.
                Assert.assertTrue(hits.length == 1);

                //  Delete test document after verification is complete.
                deleteDocument(transportClient, hits[0].getId());
            }
        }
    }

    private static SearchHit[] searchDocumentInIndex(TransportClient client, String index, String field, String value)
    {
        SearchResponse response = client.prepareSearch((ES_INDEX_PREFIX + index).toLowerCase())
                .setTypes(ES_TYPE)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchQuery(field, value.toLowerCase()))
                .setFrom(0).setSize(10)
                .execute()
                .actionGet();

        return response.getHits().getHits();
    }

    private static SearchHit[] searchDocument(TransportClient client, String field, String value)
    {
        SearchResponse response = client.prepareSearch(ES_INDEX.toLowerCase())
            .setTypes(ES_TYPE)
            .setSearchType(SearchType.QUERY_THEN_FETCH)
            .setQuery(QueryBuilders.matchQuery(field, value.toLowerCase()))
            .setFrom(0).setSize(10)
            .execute()
            .actionGet();

        return response.getHits().getHits();
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

    private static void deleteDocument(TransportClient client, String documentId)
    {

        RetryElasticsearchOperation retryDelete = new RetryElasticsearchOperation();
        while (retryDelete.shouldRetry()) {
            // Delete document by id.
            DeleteResponse response = client
                .prepareDelete()
                .setIndex(ES_INDEX.toLowerCase())
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

    private static void deleteTenantDocument(TransportClient client, String tenantId, String documentId)
    {

        RetryElasticsearchOperation retryDelete = new RetryElasticsearchOperation();
        while (retryDelete.shouldRetry()) {
            // Delete document by id.
            DeleteResponse response = client
                    .prepareDelete()
                    .setIndex((ES_INDEX_PREFIX + tenantId).toLowerCase())
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

}
