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

import com.fasterxml.jackson.core.JsonFactory;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.AuditIndexingHint;
import com.hpe.caf.auditing.AuditConnectionFactory;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.io.StringWriter;
import java.net.ConnectException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.jackson.JacksonJsonpGenerator;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch._types.SearchType;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexResponse;
import org.opensearch.client.opensearch.indices.GetMappingRequest;
import org.opensearch.client.opensearch.indices.GetMappingResponse;
import org.opensearch.client.transport.OpenSearchTransport;

public class ElasticAuditIT
{

    private static final String APPLICATION_ID = "aTestApplication";
    private static String TENANT_ID;
    private static String USER_ID;
    private static final String EVENT_CATEGORY_ID = "evDocument";
    private static final String EVENT_TYPE_ID = "etView";
    private static final String CORRELATION_ID = "cTestCorrelation";

    private static String ES_INDEX;

    private static final String CUSTOM_DOC_STRING_PARAM_FIELD = "docStringParam";
    private static final String CUSTOM_DOC_STRING_KEYWORD_PARAM_FIELD = "docKeywordParam";
    private static final String CUSTOM_DOC_STRING_TEXT_PARAM_FIELD = "docTextParam";
    private static final String CUSTOM_DOC_INT_PARAM_FIELD = "docIntParam";
    private static final String CUSTOM_DOC_SHORT_PARAM_FIELD = "docShortParam";
    private static final String CUSTOM_DOC_LONG_PARAM_FIELD = "docLongParam";
    private static final String CUSTOM_DOC_BOOLEAN_PARAM_FIELD = "docBooleanParam";
    private static final String CUSTOM_DOC_FLOAT_PARAM_FIELD = "docFloatParam";
    private static final String CUSTOM_DOC_DOUBLE_PARAM_FIELD = "docDoubleParam";
    private static final String CUSTOM_DOC_DATE_PARAM_FIELD = "docDateParam";

    private static String CAF_ELASTIC_PROTOCOL;
    private static String ES_HOSTNAME;
    private static int ES_PORT;
    private static String CAF_ELASTIC_USERNAME;
    private static String CAF_ELASTIC_PASSWORD;

    @BeforeAll
    public static void setup() throws Exception
    {
        // Test the Auditing library in elasticsearch mode
        System.setProperty("CAF_AUDIT_MODE", "elasticsearch");

        CAF_ELASTIC_PROTOCOL = System.getProperty("CAF_ELASTIC_PROTOCOL", System.getenv("CAF_ELASTIC_PROTOCOL"));
        CAF_ELASTIC_USERNAME = System.getProperty("CAF_ELASTIC_USERNAME", System.getenv("CAF_ELASTIC_USERNAME"));
        CAF_ELASTIC_PASSWORD = System.getProperty("CAF_ELASTIC_PASSWORD", System.getenv("CAF_ELASTIC_PASSWORD"));
        ES_HOSTNAME = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
        ES_PORT = Integer.parseInt(System.getProperty("es.port", System.getenv("es.port")));
        USER_ID = UUID.randomUUID().toString();
    }

    @BeforeEach
    public void randomiseTenantId() {
        TENANT_ID = UUID.randomUUID().toString().replace("-", "");
        ES_INDEX = TENANT_ID + ElasticAuditConstants.Index.SUFFIX;
    }

    @Test
    public void testIncorrectESPort() throws Exception
    {
        //  This tests the usage of an unexpected port number for the ES config.
        //  An exception is expected to be thrown.

        final String esHostAndPort = ES_HOSTNAME + ":10000";
        System.setProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES, esHostAndPort);
        try (
            AuditConnection auditConnection = AuditConnectionFactory.createConnection();
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
        } catch (final ConnectException ex) {
            assertNotNull(ex);
        }
    }

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void testInvalidTenantId() throws Exception
    {
        //  This tests the usage of invalid characters (i.e. commas) in
        //  the tenant identifier. The tenant identifier is part of the ES
        //  index name and therefore must not contain commas.
        //  An IllegalArgumentException is expected to be thrown.

        final String esHostAndPort = ES_HOSTNAME + ":" + ES_PORT;
        final String invalidTenantIdContainingCommas = "t,test,tenant";
        System.setProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES, esHostAndPort);
        try (
            AuditConnection auditConnection = AuditConnectionFactory.createConnection();
            com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {
            //  Index a sample audit event message into Elasticsearch.
            AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(APPLICATION_ID);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            Assertions.assertThrows(IllegalArgumentException.class, () ->
                    auditEventBuilder.setTenant(invalidTenantIdContainingCommas));
        }
    }

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
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

        System.setProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES, esHostAndPort);
        try (final AuditConnection auditConnection = AuditConnectionFactory.createConnection();
                com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {
            //  Index a sample audit event message into Elasticsearch.
            AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

            //  Set up fixed field data for the sample audit event message.
            auditEventBuilder.setApplication(APPLICATION_ID);
            auditEventBuilder.setEventType(EVENT_CATEGORY_ID, EVENT_TYPE_ID);
            auditEventBuilder.setCorrelationId(CORRELATION_ID);
            auditEventBuilder.setTenant(tenantIdContainingOver255Chars);
            auditEventBuilder.setUser(USER_ID);

            Assertions.assertThrows(OpenSearchException.class, auditEventBuilder::send);
        }
    }

    @Test
    public void testESIndexingWithConfigurationPassedAsSystemProps() throws Exception
    {
        // Set the system props required to configure the ElasticAuditConnection
        final String esHostAndPort = ES_HOSTNAME + ":" + ES_PORT;
        System.setProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES, esHostAndPort);

        try (final AuditConnection auditConnection = AuditConnectionFactory.createConnection();
             com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {

            // Send Audit Event to Elasticsearch
            TestAuditEvent testAuditEvent = new TestAuditEvent(auditChannel).invoke();
            // Verify that the Audit Event was stored in Elasticsearch
            verifyAuditEvent(esHostAndPort, testAuditEvent);
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
        System.setProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES, esHostAndPort);
        try (
            AuditConnection auditConnection = AuditConnectionFactory.createConnection();
            com.hpe.caf.auditing.AuditChannel auditChannel = auditConnection.createChannel()) {

            // Send Audit Event to Elasticsearch
            TestAuditEvent testAuditEvent = new TestAuditEvent(auditChannel).invoke();
            // Verify that the Audit Event was stored in Elasticsearch
            verifyAuditEvent(esHostAndPort, testAuditEvent);
        }
    }

    private void verifyAuditEvent(String esHostAndPort, TestAuditEvent testAuditEvent) throws Exception
    {
        //  Verify the type mappings have been set for the index. Then search for the audit event message in
        //  Elasticsearch and verify field data matches input.
        try (final OpenSearchTransport openSearchTransport
            = OpenSearchTransportFactory.getOpenSearchTransport(
                CAF_ELASTIC_PROTOCOL,
                esHostAndPort,
                CAF_ELASTIC_USERNAME,
                CAF_ELASTIC_PASSWORD)) {
            final OpenSearchClient openSearchClient = new OpenSearchClient(openSearchTransport);
            verifyTypeMappings(openSearchClient);

            final List<Hit<JsonData>> hits = searchDocumentInIndex(openSearchClient,
                                         ES_INDEX,
                                         ElasticAuditConstants.FixedFieldName.USER_ID_FIELD,
                                         USER_ID);

            //  Expecting a single hit.
            assertEquals(1, hits.size());

            //  Make a note of the document identifier as we will use this to clean
            //  up afterwards.
            final String docId = hits.get(0).id();

            //  Verify fixed field data results.
            verifyFixedFieldResult(hits, ElasticAuditConstants.FixedFieldName.APPLICATION_ID_FIELD, APPLICATION_ID, "string");
            verifyFixedFieldResult(hits, ElasticAuditConstants.FixedFieldName.EVENT_CATEGORY_ID_FIELD, EVENT_CATEGORY_ID, "string");
            verifyFixedFieldResult(hits, ElasticAuditConstants.FixedFieldName.EVENT_TYPE_ID_FIELD, EVENT_TYPE_ID, "string");
            verifyFixedFieldResult(hits, ElasticAuditConstants.FixedFieldName.USER_ID_FIELD, USER_ID, "string");
            verifyFixedFieldResult(hits, ElasticAuditConstants.FixedFieldName.CORRELATION_ID_FIELD, CORRELATION_ID, "string");

            //  Verify fixed field data results.
            verifyCustomFieldResult(hits, CUSTOM_DOC_STRING_PARAM_FIELD, testAuditEvent.getStringParamValue(), "string", AuditIndexingHint.KEYWORD);
            verifyCustomFieldResult(hits, CUSTOM_DOC_INT_PARAM_FIELD, testAuditEvent.getIntParamValue(), "int", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_SHORT_PARAM_FIELD, testAuditEvent.getShortParamValue(), "short", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_LONG_PARAM_FIELD, testAuditEvent.getLongParamValue(), "long", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_FLOAT_PARAM_FIELD, testAuditEvent.getFloatParamValue(), "float", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_DOUBLE_PARAM_FIELD, testAuditEvent.getDoubleParamValue(), "double", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_BOOLEAN_PARAM_FIELD, testAuditEvent.getBooleanParamValue(), "boolean", null);
            verifyCustomFieldResult(hits, CUSTOM_DOC_DATE_PARAM_FIELD, testAuditEvent.getDateParamValue(), "date", null);

            //  Delete test document after verification is complete.
            deleteDocument(openSearchClient, ES_INDEX, docId);
        }
    }

    private void verifyTypeMappings(final OpenSearchClient openSearchClient) {
        String expectedTypeMappings = "{\"dynamic_templates\":" +
                "[{\"CAFAuditKeyword\":{\"mapping\":{\"type\":\"keyword\"},\"match\":\"*_CAKyw\"}}," +
                "{\"CAFAuditText\":{\"mapping\":{\"type\":\"text\"},\"match\":\"*_CATxt\"}}," +
                "{\"CAFAuditLong\":{\"mapping\":{\"type\":\"long\"},\"match\":\"*_CALng\"}}," +
                "{\"CAFAuditInteger\":{\"mapping\":{\"type\":\"integer\"},\"match\":\"*_CAInt\"}}," +
                "{\"CAFAuditShort\":{\"mapping\":{\"type\":\"short\"},\"match\":\"*_CAShort\"}}," +
                "{\"CAFAuditDouble\":{\"mapping\":{\"type\":\"double\"},\"match\":\"*_CADbl\"}}," +
                "{\"CAFAuditFloat\":{\"mapping\":{\"type\":\"float\"},\"match\":\"*_CAFlt\"}}," +
                "{\"CAFAuditDate\":{\"mapping\":{\"type\":\"date\"},\"match\":\"*_CADte\"}}," +
                "{\"CAFAuditBoolean\":{\"mapping\":{\"type\":\"boolean\"},\"match\":\"*_CABln\"}}]," +
                "\"properties\":{\"docDoubleParam_CADbl\":{\"type\":\"double\"},\"docBooleanParam_CABln\":" +
                "{\"type\":\"boolean\"},\"docDateParam_CADte\":{\"type\":\"date\"},\"docShortParam_CAShort\":" +
                "{\"type\":\"short\"},\"eventTimeSource\":{\"type\":\"keyword\"},\"docLongParam_CALng\":" +
                "{\"type\":\"long\"},\"userId\":{\"type\":\"keyword\"},\"docIntParam_CAInt\":{\"type\":\"integer\"}," +
                "\"threadId\":{\"type\":\"long\"},\"docFloatParam_CAFlt\":{\"type\":\"float\"}," +
                "\"eventTypeId\":{\"type\":\"keyword\"},\"processId\":{\"type\":\"keyword\"},\"eventTime\":" +
                "{\"type\":\"date\"},\"correlationId\":{\"type\":\"keyword\"},\"docStringParam_CAKyw\":" +
                "{\"type\":\"keyword\"},\"applicationId\":{\"type\":\"keyword\"},\"eventOrder\":{\"type\":\"long\"}," +
                "\"eventCategoryId\":{\"type\":\"keyword\"}}}";

        final String index = (TENANT_ID + ElasticAuditConstants.Index.SUFFIX).toLowerCase();
        
        final StringWriter writer = new StringWriter();
        try (final JacksonJsonpGenerator generator = new JacksonJsonpGenerator(new JsonFactory().createGenerator(writer))) {
            final GetMappingResponse getMappingsResponse = openSearchClient.indices()
                .getMapping(new GetMappingRequest.Builder().index(index).build());
            // Get the CAF Audit Event type mapping for the tenant index
            getMappingsResponse.result().get(index).mappings().serialize(generator, new JacksonJsonpMapper());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(expectedTypeMappings, writer.toString(), "Expected type mappings and actual type mappings should match");
    }

    @Test
    public void testMultiTenantESIndexing() throws Exception
    {
        //  This tests the successful indexing of sample audit event messages for two different tenant's into ES.
        //  It indexes each tenant audit event message into ES. It then searches ES for each newly indexed tenant
        //  document and verifies the field data for each tenant document indexed into ES . Afterwards each document is
        //  removed from ES.

        final String esHostAndPort = ES_HOSTNAME + ":" + ES_PORT;

        System.setProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES, esHostAndPort);
        try (final AuditConnection auditConnection = AuditConnectionFactory.createConnection();
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
            try (OpenSearchTransport openSearchTransport
                = OpenSearchTransportFactory.getOpenSearchTransport(
                    CAF_ELASTIC_PROTOCOL,
                    esHostAndPort,
                    CAF_ELASTIC_USERNAME,
                    CAF_ELASTIC_PASSWORD)) {

                final OpenSearchClient openSearchClient = new OpenSearchClient(openSearchTransport);
                final List<String> tenantIndexIds = new ArrayList<>();
                tenantIndexIds.add(tenant1Id + ElasticAuditConstants.Index.SUFFIX);
                tenantIndexIds.add(tenant2Id + ElasticAuditConstants.Index.SUFFIX);

                ElasticAuditRetryOperation retrySearch = new ElasticAuditRetryOperation();
                List<Hit<JsonData>> tenantIndicesHits;
                while (retrySearch.shouldRetry()) {
                    tenantIndicesHits = searchDocumentInIndices(openSearchClient,
                                                                tenantIndexIds,
                                                                ElasticAuditConstants.FixedFieldName.APPLICATION_ID_FIELD,
                                                                testApplicationId);

                    int numberOfTenantIndexHits = tenantIndicesHits.size();

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
                        deleteIndices(openSearchClient, tenantIndexIds);
                        fail("Expecting only two hits to be returned from Audit, however "
                            + numberOfTenantIndexHits + " hits were returned from Elastic");
                    }

                    //  Expecting two hits.
                    assertEquals(2, numberOfTenantIndexHits,
                            "Expected two hits, one for each tenant index, to be returned from Elastic");
                    break;
                }

                //  Delete test indexes after verification is complete.
                deleteIndices(openSearchClient, tenantIndexIds);
            }
        }
    }

    private void deleteIndices(OpenSearchClient client, List<String> indices) {
        // Lowercase each string in the array of indices
        indices.forEach(x -> x.toLowerCase());

        ElasticAuditRetryOperation retryDelete = new ElasticAuditRetryOperation();
        while (retryDelete.shouldRetry()) {
            try {

                final DeleteIndexResponse deleteResponse =
                        client.indices().delete(new DeleteIndexRequest.Builder().index(indices).build());

                if (deleteResponse.acknowledged()) {
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

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static List<Hit<JsonData>> searchDocumentInIndices(OpenSearchClient client, List<String> indices, String field,
                                                       String value)
    {
        // Lowercase each string in the array of indices
        indices.forEach(x -> x.toLowerCase());

        ElasticAuditRetryOperation retrySearch = new ElasticAuditRetryOperation();
        List<Hit<JsonData>> hits = null;
        while (retrySearch.shouldRetry()) {

            try {
               hits = client.search(new SearchRequest.Builder()
                    .index(indices)
                    .searchType(SearchType.QueryThenFetch)
                    .query(x -> x.match(m -> m.field(field).query(FieldValue.of(value.toLowerCase()))))
                    .from(0)
                    .size(10)
                    .build(), JsonData.class)
                    .hits().hits();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (!hits.isEmpty()) {
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

    private static List<Hit<JsonData>> searchDocumentInIndex(OpenSearchClient client, String indexId, String field, String value)
    {
        ElasticAuditRetryOperation retrySearch = new ElasticAuditRetryOperation();
        List<Hit<JsonData>> hits = null;
        while (retrySearch.shouldRetry()) {

            try {
                hits = client.search(new SearchRequest.Builder()
                    .index(indexId)
                    .searchType(SearchType.QueryThenFetch)
                    .query(x -> x.match(m -> m.field(field).query(FieldValue.of(value.toLowerCase()))))
                    .from(0)
                    .size(10)
                    .build(), JsonData.class)
                    .hits().hits();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (!hits.isEmpty()) {
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

    private static void verifyFixedFieldResult(List<Hit<JsonData>> results, String field, Object expectedValue, String type)
            throws ParseException
    {
        JsonObject result = results.get(0).source().toJson().asJsonObject();

        String actualFieldValue = null;

        //  Identify matching field in search results.
        actualFieldValue = result.getString(field);

        //  Assert result is not null and matches expected value.
        assertNotNull(actualFieldValue);
        if (!type.toLowerCase().equals("date")) {
            assertEquals(expectedValue.toString(), actualFieldValue);
        } else {
            assertTrue(datesAreEqual((Date) expectedValue, actualFieldValue));
        }
    }

    private static void verifyCustomFieldResult(List<Hit<JsonData>> results, String field, Object expectedValue, String type, AuditIndexingHint indexingHint)
        throws ParseException
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

        JsonObject result = results.get(0).source().toJson().asJsonObject();

        String actualFieldValue = null;

        //  Identify matching field in search results.
        actualFieldValue = result.get(field).toString().replace("\"", "");
        assertNotNull(actualFieldValue);

        //  Assert result is not null and matches expected value.
        if (!type.toLowerCase().equals("date")) {
            assertEquals(expectedValue.toString(), actualFieldValue);
        } else {
            assertTrue(datesAreEqual((Date) expectedValue, actualFieldValue));
        }
    }

    private static boolean datesAreEqual(Date expectedDate, String actualDateString) throws ParseException
    {
        //  Convert to date, by default in opensearch dates are stored as a long number representing milliseconds-since-the-epoch
        Date actualDate = new Date(Long.parseLong(actualDateString));
        return expectedDate.equals(actualDate);
    }

    private static void deleteDocument(OpenSearchClient client, String indexId, String documentId)
    {
        ElasticAuditRetryOperation retryDelete = new ElasticAuditRetryOperation();
        while (retryDelete.shouldRetry()) {
            try {
                final DeleteResponse deleteResponse = client.delete(d -> d.index(indexId.toLowerCase()).id(documentId));
                if (deleteResponse.result().equals(Result.Deleted)) {
                    break;
                }
            } catch (IOException ex) {
                try {
                    // Retry deletion status is not OK.
                    retryDelete.retryNeeded();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * TestAuditEvent class for sending a test audit event with the AuditEventBuilder's event parameters methods.
     * On call of the invoke() method, the AuditChannel passed to this class creates an AuditEventBuilder which is then
     * used to build and send an audit event. Type values for each audit event parameter are saved and after the audit
     * event is sent the TestAuditEvent returns itself for validation of expected to actual values.
     */
    private class TestAuditEvent {
        private AuditChannel auditChannel;
        private String stringParamValue;
        private int intParamValue;
        private short shortParamValue;
        private long longParamValue;
        private float floatParamValue;
        private double doubleParamValue;
        private boolean booleanParamValue;
        private Date dateParamValue;

        public TestAuditEvent(AuditChannel auditChannel) {
            this.auditChannel = auditChannel;
        }

        public String getStringParamValue() {
            return stringParamValue;
        }

        public int getIntParamValue() {
            return intParamValue;
        }

        public short getShortParamValue() {
            return shortParamValue;
        }

        public long getLongParamValue() {
            return longParamValue;
        }

        public float getFloatParamValue() {
            return floatParamValue;
        }

        public double getDoubleParamValue() {
            return doubleParamValue;
        }

        public boolean getBooleanParamValue() {
            return booleanParamValue;
        }

        public Date getDateParamValue() {
            return dateParamValue;
        }

        public TestAuditEvent invoke() throws Exception {
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

            stringParamValue = "testStringParam";
            auditEventBuilder.addEventParameter(CUSTOM_DOC_STRING_PARAM_FIELD, null, stringParamValue);
            intParamValue = rand.nextInt();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_INT_PARAM_FIELD, null, intParamValue);
            shortParamValue = (short) rand.nextInt(Short.MAX_VALUE + 1);
            auditEventBuilder.addEventParameter(CUSTOM_DOC_SHORT_PARAM_FIELD, null, shortParamValue);
            longParamValue = rand.nextLong();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_LONG_PARAM_FIELD, null, longParamValue);
            floatParamValue = rand.nextFloat();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_FLOAT_PARAM_FIELD, null, floatParamValue);
            doubleParamValue = rand.nextDouble();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_DOUBLE_PARAM_FIELD, null, doubleParamValue);
            booleanParamValue = rand.nextBoolean();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_BOOLEAN_PARAM_FIELD, null, booleanParamValue);
            dateParamValue = new Date();
            auditEventBuilder.addEventParameter(CUSTOM_DOC_DATE_PARAM_FIELD, null, dateParamValue);

            //  Send audit event message to Elasticsearch.
            auditEventBuilder.send();
            return this;
        }
    }
}