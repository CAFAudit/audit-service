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

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionHelper;
import com.hpe.caf.auditing.AuditEventBuilder;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.UUID;
import java.util.Date;
import java.util.TimeZone;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ElasticAuditIT
{

    private static final String APPLICATION_ID = "aTestApplication";
    private static final String TENANT_ID = "tTestTenant";
    private static String USER_ID;
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

    private static String ES_HOSTNAME;
    private static int ES_PORT;
    private static String ES_CLUSTERNAME;

    @BeforeClass
    public static void setup() throws Exception
    {
        // Test the Auditing library in elasticsearchdirect mode
        System.setProperty("AUDIT_LIB_MODE", "elasticsearchdirect");

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
        final AuditConnection auditConnection = AuditConnectionHelper.getElasticAuditConnection(esHostAndPort, ES_CLUSTERNAME);
    }

    @Test(expected = Exception.class)
    public void testIncorrectESPort() throws Exception
    {
        //  This tests the usage of an unexpected port number for the ES config.
        //  An exception is expected to be thrown.

        final String esHostAndPort = ES_HOSTNAME + ":10000";

        try (
            AuditConnection auditConnection = AuditConnectionHelper.getElasticAuditConnection(esHostAndPort, ES_CLUSTERNAME);
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
            AuditConnection auditConnection = AuditConnectionHelper.getElasticAuditConnection(esHostAndPort, ES_CLUSTERNAME);
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

    @Test(expected = UncheckedExecutionException.class)
    public void testStringLengthRestrictionTenantId() throws Exception
    {
        //  This tests the usage of too many characters supplied as the tenant identifier. The tenant identifier is
        //  part of the ES index name and therefore must not exceed 255. An UncheckedExecutionException is expected to
        //  be thrown.

        final String esHostAndPort = ES_HOSTNAME + ":" + ES_PORT;
        final String tenantIdContainingOver255Chars = "ATenantIndexNameWithOverOneHundredCharacters" +
                "ATenantIndexNameWithOverOneHundredCharactersATenantIndexNameWithOverOneHundredCharacters" +
                "ATenantIndexNameWithOverOneHundredCharactersATenantIndexNameWithOverOneHundredCharacters" +
                "ATenantIndexNameWithOverOneHundredCharactersATenantIndexNameWithOverOneHundredCharacters";

        try (
                AuditConnection auditConnection = AuditConnectionHelper.getElasticAuditConnection(esHostAndPort,
                        ES_CLUSTERNAME);
                com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {
            //  Index a sample audit event message into Elasticsearch.
            AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(APPLICATION_ID);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            auditEventBuilder.setTenant(tenantIdContainingOver255Chars);
            auditEventBuilder.setUser(USER_ID);

            auditEventBuilder.send();
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
            AuditConnection auditConnection = AuditConnectionHelper.getElasticAuditConnection(esHostAndPort, ES_CLUSTERNAME);
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
                = ElasticAuditTransportClientFactory.getTransportClient(esHostAndPort, ES_CLUSTERNAME)) {

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
                verifyCustomFieldResult(hits, CUSTOM_DOC_STRING_PARAM_FIELD, docStringParamValue, "string");
                verifyCustomFieldResult(hits, CUSTOM_DOC_INT_PARAM_FIELD, docIntParamValue, "int");
                verifyCustomFieldResult(hits, CUSTOM_DOC_SHORT_PARAM_FIELD, docShortParamValue, "short");
                verifyCustomFieldResult(hits, CUSTOM_DOC_LONG_PARAM_FIELD, docLongParamValue, "long");
                verifyCustomFieldResult(hits, CUSTOM_DOC_FLOAT_PARAM_FIELD, docFloatParamValue, "float");
                verifyCustomFieldResult(hits, CUSTOM_DOC_DOUBLE_PARAM_FIELD, docDoubleParamValue, "double");
                verifyCustomFieldResult(hits, CUSTOM_DOC_BOOLEAN_PARAM_FIELD, docBooleanParamValue, "boolean");
                verifyCustomFieldResult(hits, CUSTOM_DOC_DATE_PARAM_FIELD, docDateParamValue, "date");

                //  Delete test document after verification is complete.
                deleteDocument(transportClient, ES_INDEX, docId);
            }
        }
    }

    private void verifyTypeMappings(TransportClient transportClient) {
        String expectedTypeMappings = "{\"cafAuditEvent\":{\"dynamic_templates\":[{\"CAFAuditKeyword\":" +
                "{\"match\":\"*_CAKyw\",\"mapping\":{\"type\":\"keyword\"}}},{\"CAFAuditText\":" +
                "{\"match\":\"*_CATxt\",\"mapping\":{\"type\":\"text\"}}},{\"CAFAuditLong\":" +
                "{\"match\":\"*_CALng\",\"mapping\":{\"type\":\"long\"}}},{\"CAFAuditInteger\":" +
                "{\"match\":\"*_CAInt\",\"mapping\":{\"type\":\"integer\"}}},{\"CAFAuditShort\":" +
                "{\"match\":\"*_CAShort\",\"mapping\":{\"type\":\"short\"}}},{\"CAFAuditDouble\":" +
                "{\"match\":\"*_CADbl\",\"mapping\":{\"type\":\"double\"}}},{\"CAFAuditFloat\":" +
                "{\"match\":\"*_CAFlt\",\"mapping\":{\"type\":\"float\"}}},{\"CAFAuditDate\":" +
                "{\"match\":\"*_CADte\",\"mapping\":{\"type\":\"date\"}}},{\"CAFAuditBoolean\":" +
                "{\"match\":\"*_CABln\",\"mapping\":{\"type\":\"boolean\"}}}],\"properties\":" +
                "{\"applicationId\":{\"type\":\"keyword\"},\"correlationId\":{\"type\":\"keyword\"}," +
                "\"docBooleanParam_CABln\":{\"type\":\"boolean\"},\"docDateParam_CADte\":{\"type\":\"date\"}," +
                "\"docDoubleParam_CADbl\":{\"type\":\"double\"},\"docFloatParam_CAFlt\":{\"type\":\"float\"}," +
                "\"docIntParam_CAInt\":{\"type\":\"integer\"},\"docLongParam_CALng\":{\"type\":\"long\"}," +
                "\"docShortParam_CAShort\":{\"type\":\"short\"},\"docStringParam_CAKyw\":{\"type\":\"keyword\"}," +
                "\"eventCategoryId\":{\"type\":\"keyword\"},\"eventOrder\":{\"type\":\"long\"},\"eventTime\":" +
                "{\"type\":\"date\"},\"eventTimeSource\":{\"type\":\"keyword\"},\"eventTypeId\":" +
                "{\"type\":\"keyword\"},\"processId\":{\"type\":\"keyword\"},\"threadId\":{\"type\":\"long\"}," +
                "\"userId\":{\"type\":\"keyword\"}}}}";

        ClusterStateResponse clusterStateResponse =
                transportClient.admin().cluster().prepareState().execute().actionGet();

        // Get the CAF Audit Event type mapping for the tenant index
        CompressedXContent indexMapping = clusterStateResponse.getState().getMetaData()
                .index((TENANT_ID + ElasticAuditConstants.Index.SUFFIX).toLowerCase())
                .getMappings()
                .get(ElasticAuditConstants.Index.TYPE)
                .source();

        Assert.assertEquals("Expected type mappings and actual type mappings should match", expectedTypeMappings,
                indexMapping.toString());
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
                AuditConnection auditConnection = AuditConnectionHelper.getElasticAuditConnection(esHostAndPort,
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
                tenantIndexIds[0] = tenant1Id + ElasticAuditConstants.Index.SUFFIX;
                tenantIndexIds[1] = tenant2Id + ElasticAuditConstants.Index.SUFFIX;

                ElasticAuditRetryOperation retrySearch = new ElasticAuditRetryOperation();
                SearchHit[] tenantIndicesHits;
                while (retrySearch.shouldRetry()) {
                    tenantIndicesHits = searchDocumentInIndices(transportClient,
                            tenantIndexIds,
                            ElasticAuditConstants.FixedFieldName.APPLICATION_ID_FIELD,
                            testApplicationId);

                    int numberOfTenantIndexHits = tenantIndicesHits.length;

                    //  If there are less than two hits returned, retry.
                    //  Else if there are more than two hits returned, fail the test
                    if (numberOfTenantIndexHits < 2) {
                        try {
                            retrySearch.retryNeeded();
                            continue;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else if (numberOfTenantIndexHits > 2) {
                        deleteIndices(transportClient, tenantIndexIds);
                        Assert.fail("Expecting only two hits to be returned from Audit, however "
                                + numberOfTenantIndexHits + " hits were returned from Elastic");
                    }

                    //  Expecting two hits.
                    Assert.assertTrue("Expected two hits, one for each tenant index, to be returned from Elastic",
                            numberOfTenantIndexHits == 2);
                    break;
                }

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

        ElasticAuditRetryOperation retryDelete = new ElasticAuditRetryOperation();
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

        ElasticAuditRetryOperation retrySearch = new ElasticAuditRetryOperation();
        SearchHit[] hits = null;
        while (retrySearch.shouldRetry()) {
            hits = client.prepareSearch(indices)
                    .setTypes(ElasticAuditConstants.Index.TYPE)
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
        ElasticAuditRetryOperation retrySearch = new ElasticAuditRetryOperation();
        SearchHit[] hits = null;
        while (retrySearch.shouldRetry()) {
            hits = client.prepareSearch(indexId.toLowerCase())
                    .setTypes(ElasticAuditConstants.Index.TYPE)
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
            Assert.assertEquals(expectedValue.toString(), actualFieldValue.toString());
        } else {
            Assert.assertTrue(datesAreEqual((Date) expectedValue, actualFieldValue.toString()));
        }
    }

    private static void verifyCustomFieldResult(SearchHit[] results, String field, Object expectedValue, String type)
        throws ParseException
    {
        //  Determine entry key to look for based on type supplied.
        switch (type.toLowerCase()) {
            case "string":
                field = field + ElasticAuditConstants.CustomFieldSuffix.KEYWORD_SUFFIX;
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

}
