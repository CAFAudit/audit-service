/*
 * Copyright 2015-2022 Micro Focus or one of its affiliates.
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

import com.fasterxml.jackson.core.JsonFactory;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.AuditIndexingHint;
import com.hpe.caf.auditing.elastic.ElasticAuditConstants;
import com.hpe.caf.auditing.elastic.ElasticAuditRestHighLevelClientFactory;
import com.hpe.caf.auditing.elastic.ElasticAuditRetryOperation;
import com.hpe.caf.auditing.exception.AuditConfigurationException;
import com.hpe.caf.auditing.exception.AuditingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static com.github.stefanbirkner.systemlambda.SystemLambda.*;
import jakarta.json.JsonObject;
import java.io.StringWriter;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.jackson.JacksonJsonpGenerator;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch._types.SearchType;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexResponse;
import org.opensearch.client.opensearch.indices.GetMappingRequest;
import org.opensearch.client.opensearch.indices.GetMappingResponse;
import org.opensearch.client.transport.OpenSearchTransport;

public class WebserviceClientAuditIT {

    private static final Logger LOG = LoggerFactory.getLogger(WebserviceClientAuditIT.class.getName());

    private static final String APPLICATION_ID = "aTestApplication";
    private static String TENANT_ID;
    private static final String USER_ID = "aTestUser@testcompany.com";
    private static final String EVENT_CATEGORY_ID = "evDocument";
    private static final String EVENT_TYPE_ID = "etView";
    private static final String CORRELATION_ID = "cTestCorrelation";

    private static String ES_INDEX;

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

    private static String CAF_ELASTIC_PROTOCOL;
    private static String ES_HOSTNAME;
    private static int ES_PORT;
    private static String ES_HOSTNAME_AND_PORT;
    private static String CAF_ELASTIC_USERNAME;
    private static String CAF_ELASTIC_PASSWORD;

    @BeforeClass
    public static void setup() throws Exception {
        // Test the Auditing library in webservice mode
        System.setProperty("CAF_AUDIT_MODE", "webservice");

        WS_HOSTNAME = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
        WS_PORT = Integer.parseInt(System.getProperty("webservice.adminport", System.getenv("webservice.adminport")));
        WS_ENDPOINT = String.format("http://%s:%s/caf-audit-service/v1", WS_HOSTNAME, WS_PORT);

        CAF_ELASTIC_PROTOCOL = System.getProperty("CAF_ELASTIC_PROTOCOL", System.getenv("CAF_ELASTIC_PROTOCOL"));
        CAF_ELASTIC_USERNAME = System.getProperty("CAF_ELASTIC_USERNAME", System.getenv("CAF_ELASTIC_USERNAME"));
        CAF_ELASTIC_PASSWORD = System.getProperty("CAF_ELASTIC_PASSWORD", System.getenv("CAF_ELASTIC_PASSWORD"));
        ES_HOSTNAME = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
        ES_PORT = Integer.parseInt(System.getProperty("elasticsearch.http.port", System.getenv("elasticsearch.http.port")));
        ES_HOSTNAME_AND_PORT = String.format("%s:%s", ES_HOSTNAME, ES_PORT);
    }

    @BeforeMethod
    public void randomiseTenantId() {
        TENANT_ID = UUID.randomUUID().toString().replace("-", "");
        ES_INDEX = TENANT_ID + ElasticAuditConstants.Index.SUFFIX;
    }

    @AfterMethod
    public void cleanUp() throws AuditConfigurationException {
        OpenSearchTransport transport
                     = ElasticAuditRestHighLevelClientFactory.getOpenSearchTransport(
                         CAF_ELASTIC_PROTOCOL,
                         ES_HOSTNAME_AND_PORT,
                         CAF_ELASTIC_USERNAME,
                         CAF_ELASTIC_PASSWORD);
        try {
            deleteIndex(new OpenSearchClient(transport), ES_INDEX);
        } catch (RuntimeException rte) {
            LOG.warn("Unable to delete tenant index. It may not exist.");
        }
    }

    @Test
    public void testWebserviceClient() throws Exception {
        withEnvironmentVariable("no_proxy", "")
            .and("http_proxy", "")
            .and("https_proxy", "")
            .execute(() -> {
                System.setProperty("CAF_AUDIT_WEBSERVICE_ENDPOINT_URL", WS_ENDPOINT);
                AuditConnection auditConnection = AuditConnectionFactory.createConnection();
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
                try (OpenSearchTransport transport
                    = ElasticAuditRestHighLevelClientFactory.getOpenSearchTransport(
                        CAF_ELASTIC_PROTOCOL,
                        ES_HOSTNAME_AND_PORT,
                        CAF_ELASTIC_USERNAME,
                        CAF_ELASTIC_PASSWORD)) {

                    final OpenSearchClient client = new OpenSearchClient(transport);
                    verifyTypeMappings(client);

                    List<Hit<JsonData>> hits = searchDocumentInIndex(client,
                                                 ES_INDEX,
                                                 ElasticAuditConstants.FixedFieldName.USER_ID_FIELD,
                                                 USER_ID);

                    //  Expecting a single hit.
                    Assert.assertTrue(hits.size() == 1);

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
                    deleteDocument(client, ES_INDEX, docId);
                }
            });
    }

    @Test
    public void eventOrderTest() throws Exception{
        withEnvironmentVariable("no_proxy", "")
            .and("http_proxy", "")
            .and("https_proxy", "")
            .execute(() -> {
                int event1Order;
                int event2Order;
                System.setProperty("CAF_AUDIT_WEBSERVICE_ENDPOINT_URL", WS_ENDPOINT);
                try (
                    final AuditConnection auditConnection = AuditConnectionFactory.createConnection();
                    final AuditChannel auditChannel = auditConnection.createChannel()) {
                    {
                        Date date = new Date();
                        String correlationId = UUID.randomUUID().toString();
                        AuditLog.auditTestEvent1(auditChannel, TENANT_ID, "user1", correlationId,
                                                 "stringType1", "stringType2", "stringType3", "stringType4",
                                                 Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true,
                                                 date);

                        Hit<JsonData> searchHit = getAuditEvent(correlationId);
                        JsonObject source = searchHit.source().toJson().asJsonObject();
                        event1Order = source.getInt(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD);
                    }

                    {
                        Date date = new Date();
                        String correlationId = UUID.randomUUID().toString();
                        AuditLog.auditTestEvent1(auditChannel, TENANT_ID, "user1", correlationId,
                                                 "stringType1", "stringType2", "stringType3", "stringType4",
                                                 Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true,
                                                 date);

                        Hit<JsonData> searchHit = getAuditEvent(correlationId);
                        JsonObject source = searchHit.source().toJson().asJsonObject();
                        event2Order = source.getInt(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD);
                    }

                    Assert.assertTrue(event1Order < event2Order, "Event 1 order was not less than event 2 order");
                }
            });
    }

    @Test(expectedExceptions = AuditingException.class)
    public void testWebserviceClientBadAuditEvent() throws Exception {
        withEnvironmentVariable("no_proxy", "")
            .and("http_proxy", "")
            .and("https_proxy", "")
            .execute(() -> {
                System.setProperty("CAF_AUDIT_WEBSERVICE_ENDPOINT_URL", WS_ENDPOINT);
                AuditConnection auditConnection = AuditConnectionFactory.createConnection();
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
            });
    }

    private Hit<JsonData> getAuditEvent(String correlationId) throws AuditConfigurationException {
        try (OpenSearchTransport transport
                     = ElasticAuditRestHighLevelClientFactory.getOpenSearchTransport(
                         CAF_ELASTIC_PROTOCOL,
                         ES_HOSTNAME_AND_PORT,
                         CAF_ELASTIC_USERNAME,
                         CAF_ELASTIC_PASSWORD)) {
            final OpenSearchClient openSearchClient = new OpenSearchClient(transport);
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

            Assert.assertEquals(hits.total().value(), 1);

            return hits.hits().get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteIndex(OpenSearchClient client, String indexId)
    {
        ElasticAuditRetryOperation retryDelete = new ElasticAuditRetryOperation();
        while (retryDelete.shouldRetry()) {
            try {

                try {
                    final DeleteIndexResponse deleteResponse =
                        client.indices().delete(new DeleteIndexRequest.Builder().index(indexId).build());

                    if (deleteResponse.acknowledged()) {
                        // If Elastic acknowledged our delete wait a second to allow it time to delete the index
                        Thread.sleep(1000);
                        break;
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Retry deletion if Elastic did not acknowledge the delete request.
                try {
                    retryDelete.retryNeeded();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
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

    private static void deleteDocument(OpenSearchClient client, String indexId, String documentId)
    {
        ElasticAuditRetryOperation retryDelete = new ElasticAuditRetryOperation();
        while (retryDelete.shouldRetry()) {
            // Delete document by id.
            final DeleteResponse response;
            try {
                final DeleteResponse deleteResponse = client.delete(d -> d.index(indexId.toLowerCase()).id(documentId));
                if (deleteResponse.result().equals(Result.Deleted)) {
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Retry deletion status is not OK.
            try {
                retryDelete.retryNeeded();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void verifyTypeMappings(OpenSearchClient client) {
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
                "\"properties\":{\"docDoubleParam_CADbl\":{\"type\":\"double\"}," +
                "\"docBooleanParam_CABln\":{\"type\":\"boolean\"}," +
                "\"docDateParam_CADte\":{\"type\":\"date\"}," +
                "\"docShortParam_CAShort\":{\"type\":\"short\"}," +
                "\"eventTimeSource\":{\"type\":\"keyword\"}," +
                "\"docLongParam_CALng\":{\"type\":\"long\"},\"userId\":{\"type\":\"keyword\"}," +
                "\"docIntParam_CAInt\":{\"type\":\"integer\"}," +
                "\"threadId\":{\"type\":\"long\"}," +
                "\"docFloatParam_CAFlt\":{\"type\":\"float\"}," +
                "\"eventTypeId\":{\"type\":\"keyword\"}," +
                "\"processId\":{\"type\":\"keyword\"}," +
                "\"eventTime\":{\"type\":\"date\"}," +
                "\"correlationId\":{\"type\":\"keyword\"}," +
                "\"docStringParam_CAKyw\":{\"type\":\"keyword\"}," +
                "\"applicationId\":{\"type\":\"keyword\"}," +
                "\"docStringParam_CATxt\":{\"type\":\"text\"}," +
                "\"eventOrder\":{\"type\":\"long\"}," +
                "\"eventCategoryId\":{\"type\":\"keyword\"}}}";

        final String index = (TENANT_ID + ElasticAuditConstants.Index.SUFFIX).toLowerCase();
        final StringWriter writer = new StringWriter();
        try (final JacksonJsonpGenerator generator = new JacksonJsonpGenerator(new JsonFactory().createGenerator(writer))) {
            final GetMappingResponse getMappingsResponse = client.indices()
                .getMapping(new GetMappingRequest.Builder().index(index).build());
            // Get the CAF Audit Event type mapping for the tenant index
            getMappingsResponse.result().get(index).mappings().serialize(generator, new JacksonJsonpMapper());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assert.assertEquals(writer.toString(), expectedTypeMappings,
                "Expected type mappings and actual type mappings should match");
    }

    private static void verifyFixedFieldResult(List<Hit<JsonData>>results, String field, Object expectedValue, String type)
            throws ParseException
    {
        JsonObject result = results.get(0).source().toJson().asJsonObject();

        String actualFieldValue = null;

        //  Identify matching field in search results.
        actualFieldValue = result.get(field).toString().replace("\"", "");
        Assert.assertNotNull(actualFieldValue);

        //  Assert result is not null and matches expected value.
        if (!type.toLowerCase().equals("date")) {
            Assert.assertEquals(actualFieldValue, expectedValue.toString());
        } else {
            assertDatesAreEqual((Date) expectedValue, actualFieldValue);
        }
    }

    private static void verifyCustomFieldResult(List<Hit<JsonData>> results, String field, Object expectedValue, String type,
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

        JsonObject result = results.get(0).source().toJson().asJsonObject();

        String actualFieldValue = null;

        //  Identify matching field in search results.
        actualFieldValue = result.get(field).toString().replace("\"", "");
        Assert.assertNotNull(actualFieldValue);

        //  Assert result is not null and matches expected value.
        if (!type.toLowerCase().equals("date")) {
            Assert.assertEquals(actualFieldValue, expectedValue.toString());
        } else {
            assertDatesAreEqual((Date) expectedValue, actualFieldValue);
        }
    }

    private static void assertDatesAreEqual(Date expectedDate, String actualDateString) throws ParseException
    {
        //  Convert to date, by default in opensearch dates are stored as a long number representing milliseconds-since-the-epoch
        Date actualDate = new Date(Long.parseLong(actualDateString));
        Assert.assertEquals(expectedDate, actualDate);
    }
}
