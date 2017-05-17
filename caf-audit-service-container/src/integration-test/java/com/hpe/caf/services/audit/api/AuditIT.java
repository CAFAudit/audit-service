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

import com.hpe.caf.auditing.elastic.ElasticAuditConstants;
import com.hpe.caf.auditing.elastic.ElasticAuditRetryOperation;
import com.hpe.caf.auditing.elastic.ElasticAuditTransportClientFactory;
import com.hpe.caf.services.audit.client.ApiException;
import com.hpe.caf.services.audit.client.api.AuditEventsApi;
import com.hpe.caf.services.audit.client.model.EventParam;
import com.hpe.caf.services.audit.client.model.NewAuditEvent;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

public class AuditIT {
    private static String AUDIT_WEBSERVICE_HTTP_BASE_PATH;
    private static String AUDIT_WEBSERVICE_HTTPS_BASE_PATH;
    private static String CAF_ELASTIC_HOST_AND_PORT;
    private static String CAF_ELASTIC_CLUSTER_NAME;

    private static AuditEventsApi auditEventsApi;

    @BeforeClass
    public static void setup() throws Exception {
        //  Read environment variable settings
        AUDIT_WEBSERVICE_HTTP_BASE_PATH = System.getenv("webserviceurl");
        AUDIT_WEBSERVICE_HTTPS_BASE_PATH = System.getenv("webserviceurlhttps");

        CAF_ELASTIC_HOST_AND_PORT = System.getenv("CAF_ELASTIC_HOST_AND_PORT");
        CAF_ELASTIC_CLUSTER_NAME = System.getenv("CAF_ELASTIC_CLUSTER_NAME");

        auditEventsApi = new AuditEventsApi();
    }

    @Test
    public void testAuditEventsPost() throws Exception {
        auditEventsApi.getApiClient().setBasePath(AUDIT_WEBSERVICE_HTTP_BASE_PATH);
        postNewAuditEvent();
    }

    @Test
    public void testAuditEventsPost_Https() throws Exception {
        disableSSLVerification();
        auditEventsApi.getApiClient().setBasePath(AUDIT_WEBSERVICE_HTTPS_BASE_PATH);
        postNewAuditEvent();
    }

    @Test
    public void testAuditEventsPost_FixedFieldNotProvided() {
        //  Create new audit event message with at least one fixed field missing.
        NewAuditEvent auditEventMessage = createNewAuditEventExcludeFixedField();
        try {
            auditEventsApi.auditeventsPost(auditEventMessage);
        } catch (ApiException ae) {
            Assert.assertEquals(ae.getMessage(),"The application identifier has not been specified");
        }
    }

    @Test
    public void testAuditEventsPost_CustomsFieldsNotProvided() {
        //  Create new audit event message with no custom fields specified.
        NewAuditEvent auditEventMessage = createNewAuditEventExcludeCustomFields();
        try {
            auditEventsApi.auditeventsPost(auditEventMessage);
        } catch (ApiException ae) {
            Assert.assertEquals(ae.getMessage(),"Custom audit event fields have not been specified");
        }
    }

    /**
     * Disable Certificate Validation in Java SSL Connections.
     */
    private static void disableSSLVerification() {
        // Set up a trust-all cert manager implementation
        TrustManager[] trustAllCertsManager = new TrustManager[]{
                new X509TrustManager()
                {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        System.out.println("Trust All TrustManager getAcceptedIssuers() called");
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType)
                    {
                        System.out.println("Trust All TrustManager checkClientTrusted() called");
                    }

                    @Override
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType)
                    {
                        System.out.println("Trust All TrustManager CheckServerTrusted() called");
                    }
                }
        };

        SSLContext sc;
        try {
            // Install the all-trusting trust manager.
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCertsManager, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier.
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Install the all-trusting host verifier.
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a new audit event message to Elasticsearch.
     */
    private void postNewAuditEvent() throws Exception {
        //  Create new audit event message and index into Elasticsearch.
        final NewAuditEvent auditEventMessage = createNewAuditEvent();
        auditEventsApi.auditeventsPost(auditEventMessage);

        //  Search for the audit event message in Elasticsearch and verify
        //  hit has been returned.
        try (TransportClient transportClient
                     = ElasticAuditTransportClientFactory.getTransportClient(CAF_ELASTIC_HOST_AND_PORT, CAF_ELASTIC_CLUSTER_NAME)) {

            final String esIndex = auditEventMessage.getTenantId().toLowerCase().concat("_audit");
            SearchHit[] hits = new SearchHit[0];
            hits = searchAuditEventMessage(transportClient,
                    esIndex,
                    "userId",
                    auditEventMessage.getUserId());

            //  Expecting a single hit.
            Assert.assertTrue(hits.length == 1);

            //  Make a note of the document identifier as we will use this to clean
            //  up afterwards.
            final String docId = hits[0].getId();

            //  Verify search results match the expected audit event message data.
            final Map<String, Object> hitSource = hits[0].getSource();
            verifySearchResults(auditEventMessage, hitSource);

            //  Delete test document after verification is complete.
            deleteAuditEventMessage(transportClient, esIndex, docId);
        }
    }

    /**
     * Returns a new audit event object.
     */
    private NewAuditEvent createNewAuditEvent() {
        return getNewAuditEvent(false, false);
    }

    /**
     * Returns a new audit event object with a fixed field removed.
     */
    private NewAuditEvent createNewAuditEventExcludeFixedField() {
        return getNewAuditEvent(true, false);
    }

    /**
     * Returns a new audit event object with no custom fields defined.
     */
    private NewAuditEvent createNewAuditEventExcludeCustomFields() {
        return getNewAuditEvent(false, true);
    }

    /**
     * Creates a new audit event object. Depending on the supplied boolean inputs, it returns an instance
     * comprising fixed and custom fields. It can be configured to exclude a single fixed field and all
     * custom fields.
     */
    private NewAuditEvent getNewAuditEvent(final boolean excludeFixed, final boolean excludeCustom) {

        //  Declare fixed field metadata values for test purposes.
        final String APPLICATION_ID = "application-test";
        final String PROCESS_ID = UUID.randomUUID().toString();
        final long THREAD_ID = 1;
        final long EVENT_ORDER = 1;
        final String EVENT_TIME = Instant.now().toString();
        final String EVENT_TIME_SOURCE = "event-time-source-test";
        final String USER_ID = UUID.randomUUID().toString();
        final String TENANT_ID = UUID.randomUUID().toString();
        final String CORRELATION_ID = "correlation-test";
        final String EVENT_TYPE_ID = "event-type-test";
        final String EVENT_CATEGORY_ID = "event-catageory-test";

        //  Create a new audit event object.
        NewAuditEvent ae = new NewAuditEvent();

        //  Add fixed metadata field values.
        //  Do not include the application identifier if a fixed field is to be excluded.
        if (!excludeFixed) {
            ae.setApplicationId(APPLICATION_ID);
        }

        ae.setProcessId(PROCESS_ID);
        ae.setThreadId(THREAD_ID);
        ae.setEventOrder(EVENT_ORDER);
        ae.setEventTime(EVENT_TIME);
        ae.setEventTimeSource(EVENT_TIME_SOURCE);
        ae.setUserId(USER_ID);
        ae.setTenantId(TENANT_ID);
        ae.setCorrelationId(CORRELATION_ID);
        ae.setEventTypeId(EVENT_TYPE_ID);
        ae.setEventCategoryId(EVENT_CATEGORY_ID);

        //  Add custom metadata field values.
        //  Do not include custom event parameters if custom fields are to be excluded.
        if (!excludeCustom) {
            List<EventParam> eventParamsList = new ArrayList<EventParam>();

            //  Set up random test values for custom field data.
            final Random rand = new Random();

            final EventParam stringEventParam = createEventParam("stringParam", null, EventParam.ParamTypeEnum.STRING, "stringParam-test-value");
            eventParamsList.add(stringEventParam);

            final EventParam intEventParam = createEventParam("intParam", null, EventParam.ParamTypeEnum.INT, String.valueOf(rand.nextInt()));
            eventParamsList.add(intEventParam);
            final EventParam shortEventParam = createEventParam("shortParam", null, EventParam.ParamTypeEnum.SHORT, String.valueOf((short) rand.nextInt(Short.MAX_VALUE + 1)));
            eventParamsList.add(shortEventParam);
            final EventParam longEventParam = createEventParam("longParam", null, EventParam.ParamTypeEnum.LONG, String.valueOf(rand.nextLong()));
            eventParamsList.add(longEventParam);
            final EventParam floatEventParam = createEventParam("floatParam", null, EventParam.ParamTypeEnum.FLOAT, String.valueOf(rand.nextFloat()));
            eventParamsList.add(floatEventParam);
            final EventParam doubleEventParam = createEventParam("doubleParam", null, EventParam.ParamTypeEnum.DOUBLE, String.valueOf(rand.nextDouble()));
            eventParamsList.add(doubleEventParam);
            final EventParam booleanEventParam = createEventParam("booleanParam", null, EventParam.ParamTypeEnum.BOOLEAN, String.valueOf(rand.nextBoolean()));
            eventParamsList.add(booleanEventParam);
            final EventParam dateEventParam = createEventParam("dateParam", null, EventParam.ParamTypeEnum.DATE, Instant.now().toString());
            eventParamsList.add(dateEventParam);

            ae.setEventParams(eventParamsList);
        }

        return ae;
    }

    /**
     * Search Elasticsearch by the specified field and value.
     */
    private static SearchHit[] searchAuditEventMessage(final TransportClient client, final String indexId, final String field, final String value)
    {
        final ElasticAuditRetryOperation retrySearch = new ElasticAuditRetryOperation();
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

    /**
     * Delete the specified document in Elasticsearch.
     */
    private static void deleteAuditEventMessage(final TransportClient client, final String indexId, final String documentId)
    {
        final ElasticAuditRetryOperation retryDelete = new ElasticAuditRetryOperation();
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

    /**
     * Verifies the search results match the expected audit event message data that was indexed into Elasticsearch.
     */
    private void verifySearchResults(final NewAuditEvent expected, final Map<String, Object> actual) throws Exception {

        //  Verify fixed field results.
        assertField(ElasticAuditConstants.FixedFieldName.APPLICATION_ID_FIELD, false, expected.getApplicationId(), actual);
        assertField(ElasticAuditConstants.FixedFieldName.PROCESS_ID_FIELD, false, expected.getProcessId(), actual);
        assertField(ElasticAuditConstants.FixedFieldName.THREAD_ID_FIELD, false, expected.getThreadId(), actual);
        assertField(ElasticAuditConstants.FixedFieldName.EVENT_ORDER_FIELD, false, expected.getEventOrder(), actual);
        assertField(ElasticAuditConstants.FixedFieldName.EVENT_TIME_FIELD, false, expected.getEventTime(), actual);
        assertField(ElasticAuditConstants.FixedFieldName.EVENT_TIME_SOURCE_FIELD, false, expected.getEventTimeSource(), actual);
        assertField(ElasticAuditConstants.FixedFieldName.USER_ID_FIELD, false, expected.getUserId(), actual);
        assertField(ElasticAuditConstants.FixedFieldName.CORRELATION_ID_FIELD, false, expected.getCorrelationId(), actual);
        assertField(ElasticAuditConstants.FixedFieldName.EVENT_TYPE_ID_FIELD, false, expected.getEventTypeId(), actual);
        assertField(ElasticAuditConstants.FixedFieldName.EVENT_CATEGORY_ID_FIELD, false, expected.getEventCategoryId(), actual);

        //  Verify custom field results.
        for (EventParam ep : expected.getEventParams()) {
            //  Identify parameter type.
            final EventParam.ParamTypeEnum epParamType = ep.getParamType();

            switch(epParamType) {
                case STRING:
                    assertField(ep.getParamName(), true, ep.getParamValue(), actual);
                    break;
                case SHORT:
                    assertField(ep.getParamName(), true, Short.parseShort(ep.getParamValue()), actual);
                    break;
                case INT:
                    assertField(ep.getParamName(), true, Integer.parseInt(ep.getParamValue()), actual);
                    break;
                case LONG:
                    assertField(ep.getParamName(), true, Long.parseLong(ep.getParamValue()), actual);
                    break;
                case FLOAT:
                    assertField(ep.getParamName(), true, Float.parseFloat(ep.getParamValue()), actual);
                    break;
                case DOUBLE:
                    assertField(ep.getParamName(), true, Double.parseDouble(ep.getParamValue()), actual);
                    break;
                case BOOLEAN:
                    assertField(ep.getParamName(), true, Boolean.parseBoolean(ep.getParamValue()), actual);
                    break;
                case DATE:
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    assertField(ep.getParamName(), true, df.parse(ep.getParamValue()), actual);
                    break;

                default:
                    // Unexpected audit event parameter type.
                    throw new IllegalArgumentException("Unexpected parameter type: " + epParamType.toString());
            }
        }
    }

    private void assertField(String fieldName, final boolean isCustom, final String expectedValue, final Map<String, Object> searchResult){

        if (isCustom) {
            fieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.KEYWORD_SUFFIX);
        }
        Assert.assertTrue(searchResult.containsKey(fieldName), String.format("Field %s not found", fieldName));
        Object sourceField = searchResult.get(fieldName);

        Assert.assertEquals(String.class, sourceField.getClass());
        String value = (String) sourceField;
        Assert.assertEquals(expectedValue, value);
    }

    private void assertField(String fieldName, final boolean isCustom, final Short expectedValue, final Map<String, Object> searchResult){
        if (isCustom) {
            fieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.SHORT_SUFFIX);
        }

        Assert.assertTrue(searchResult.containsKey(fieldName), String.format("Field %s not found", fieldName));
        Object sourceField = searchResult.get(fieldName);

        Assert.assertEquals(Integer.class, sourceField.getClass());
        Short value = ((Integer)(sourceField)).shortValue();
        Assert.assertEquals(expectedValue, value);
    }

    private void assertField(String fieldName, final boolean isCustom, final Integer expectedValue, final Map<String, Object> searchResult){
        if (isCustom) {
            fieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.INT_SUFFIX);
        }

        Assert.assertTrue(searchResult.containsKey(fieldName), String.format("Field %s not found", fieldName));
        Object sourceField = searchResult.get(fieldName);

        Assert.assertEquals(Integer.class, sourceField.getClass());
        Integer value = (Integer)sourceField;
        Assert.assertEquals(expectedValue, value);
    }

    private void assertField(String fieldName, final boolean isCustom, final Long expectedValue, final Map<String, Object> searchResult){
        if (isCustom) {
            fieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.LONG_SUFFIX);
        }

        Assert.assertTrue(searchResult.containsKey(fieldName), String.format("Field %s not found", fieldName));
        Object sourceField = searchResult.get(fieldName);

        Long value = Long.parseLong(sourceField.toString());
        Assert.assertEquals(expectedValue, value);
    }

    private void assertField(String fieldName, final boolean isCustom, final Float expectedValue, final Map<String, Object> searchResult){
        if (isCustom) {
            fieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.FLOAT_SUFFIX);
        }

        Assert.assertTrue(searchResult.containsKey(fieldName), String.format("Field %s not found", fieldName));
        Object sourceField = searchResult.get(fieldName);

        Assert.assertEquals(Double.class, sourceField.getClass());
        Float value = ((Double)sourceField).floatValue();
        Assert.assertEquals(expectedValue, value);
    }

    private void assertField(String fieldName, final boolean isCustom, final Double expectedValue, final Map<String, Object> searchResult){
        if (isCustom) {
            fieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.DOUBLE_SUFFIX);
        }

        Assert.assertTrue(searchResult.containsKey(fieldName), String.format("Field %s not found", fieldName));
        Object sourceField = searchResult.get(fieldName);

        Assert.assertEquals(Double.class, sourceField.getClass());
        Double value = (Double)sourceField;
        Assert.assertEquals(expectedValue, value);
    }

    private void assertField(String fieldName, final boolean isCustom, final Boolean expectedValue, final Map<String, Object> searchResult){
        if (isCustom) {
            fieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.BOOLEAN_SUFFIX);
        }

        Assert.assertTrue(searchResult.containsKey(fieldName), String.format("Field %s not found", fieldName));
        Object sourceField = searchResult.get(fieldName);

        Assert.assertEquals(Boolean.class, sourceField.getClass());
        Boolean value = (Boolean)sourceField;
        Assert.assertEquals(expectedValue, value);
    }

    private void assertField(String fieldName, final boolean isCustom, final Date expectedValue, final Map<String, Object> searchResult) throws ParseException {
        if (isCustom) {
            fieldName = fieldName.concat(ElasticAuditConstants.CustomFieldSuffix.DATE_SUFFIX);
        }

        Assert.assertTrue(searchResult.containsKey(fieldName), String.format("Field %s not found", fieldName));
        Object sourceField = searchResult.get(fieldName);

        Assert.assertEquals(String.class, sourceField.getClass());

        String sourceTime = (String)sourceField;
        DateTime dateTime = new DateTime(sourceTime);

        Assert.assertEquals(new DateTime(expectedValue), dateTime);
    }

    //  Returns a new event parameter object.
    private EventParam createEventParam(final String name, final String columnName, final EventParam.ParamTypeEnum type, final String value) {
        EventParam ep = new EventParam();

        ep.setParamName(name);
        ep.setParamColumnName(columnName);
        ep.setParamType(type);
        ep.setParamValue(value);

        return ep;
    }

}
