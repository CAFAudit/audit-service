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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.hpe.caf.services.audit.api.AuditLog;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class ElasticAuditTestCaseValidationIT
{

    private static String ES_HOSTNAME;
    private static int ES_PORT;
    private static String ES_CLUSTERNAME;

    private static String ES_HOST_AND_PORT;

    private static final String TENANT_ID = "1";

    private static final String ES_INDEX = "audit_tenant_" + TENANT_ID;

    private static final String KEYWORD_SUFFIX = "_AKyw";
    private static final String SHORT_SUFFIX = "_ASrt";
    private static final String INT_SUFFIX = "_AInt";
    private static final String LONG_SUFFIX = "_ALng";
    private static final String FLOAT_SUFFIX = "_AFlt";
    private static final String DOUBLE_SUFFIX = "_ADbl";
    private static final String BOOLEAN_SUFFIX = "_ABln";
    private static final String DATE_SUFFIX = "_ADte";

    //The audit events XML file in the test case directory must be the same events XML file used in the caf-audit-maven-plugin, see pom.xml property auditXMLConfigFile.
    private static final String testCaseDirectory = "./test-case";

    private static final Logger LOG = LoggerFactory.getLogger(ElasticAuditTestCaseValidationIT.class);

    @BeforeClass
    public static void setup() throws Exception
    {
        ES_HOSTNAME = System.getProperty("docker.host.address", System.getenv("docker.host.address"));
        ES_PORT = Integer.parseInt(System.getProperty("es.port", System.getenv("es.port")));
        ES_CLUSTERNAME = System.getProperty("es.cluster.name", System.getenv("es.cluster.name"));

        ES_HOST_AND_PORT = ES_HOSTNAME + ":" + ES_PORT;
    }

    private static void deleteTenantIndex(TransportClient transportClient) throws ExecutionException, InterruptedException
    {
        Thread.sleep(1000);
        transportClient.admin().indices().delete(new DeleteIndexRequest(ES_INDEX)).get();
        Thread.sleep(1000);
    }

    // After each test method delete the tenant index
    @AfterMethod
    private static void deleteTenantIndex() throws Exception
    {
        Thread.sleep(1000);
        try (TransportClient transportClient
            = ElasticAuditTransportClientFactory.getTransportClient(ES_HOST_AND_PORT, ES_CLUSTERNAME)) {
            transportClient.admin().indices().delete(new DeleteIndexRequest(ES_INDEX)).get();
        }
        Thread.sleep(1000);
    }

    @Test
    public void auditTest() throws Exception
    {
        LOG.info("*** Beginning Audit tests ***");

        AuditTestCase testCase = getTestCase();
        Iterator<Path> testCaseYamls = testCase.getYaml().iterator();
        while (testCaseYamls.hasNext()) {
            Path eventsYaml = testCaseYamls.next();
            String eventsYamlFileName = eventsYaml.getFileName().toString();
            LOG.info("Verifying testCase yaml file: " + eventsYamlFileName);
            AuditEventMessages messagesToSend = getTestMessages(eventsYaml);
            LinkedList<Map<String, Object>> expectedResultSet = sendAuditEventMessages(messagesToSend.getMessages(), TENANT_ID);

            // Allow time to sleep for events to make it into Elastic
            Thread.sleep(1000);

            try (TransportClient transportClient
                = ElasticAuditTransportClientFactory.getTransportClient(ES_HOST_AND_PORT, ES_CLUSTERNAME)) {

                // Search the Tenant Index for all Audit Events
                SearchResponse allAuditEventsForTenantIndexSearchResponse = transportClient.prepareSearch(ES_INDEX)
                    .setTypes("cafAuditEvent")
                    .setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setQuery(QueryBuilders.matchAllQuery())
                    .setFrom(0).setSize(60)
                    .setExplain(true)
                    .get();

                final SearchHits allActualTenantAuditEvents = allAuditEventsForTenantIndexSearchResponse.getHits();

                doesDatabaseMatch(eventsYamlFileName, expectedResultSet, allActualTenantAuditEvents);

                // If there is another test events yaml to test, delete the tenant index, otherwise let AfterMethod() take
                // care of it
                if (testCaseYamls.hasNext()) {
                    deleteTenantIndex(transportClient);
                }
            }
        }
        LOG.info("*** Completed Audit tests ***");
    }

    private void doesDatabaseMatch(String eventsYamlFileName, LinkedList<Map<String, Object>> expectedResultSet, SearchHits allActualTenantAuditEvents)
    {

        if (expectedResultSet.size() != allActualTenantAuditEvents.totalHits()) {
            LOG.error("Failed to send one or more messages...");
        } else {

            // Copy the returned events into a LinkedList before matching with expected
            Iterator<SearchHit> actualResultSetIter = allActualTenantAuditEvents.iterator();
            LinkedList<Map<String, Object>> actualResultSetLinkedList = new LinkedList<>();
            while (actualResultSetIter.hasNext()) {
                actualResultSetLinkedList.add(actualResultSetIter.next().getSource());
            }
            Iterator<Map<String, Object>> actualAuditEventsIter = actualResultSetLinkedList.iterator();

            // Iterate through and match each actual audit event returned with its expected audit event
            while (actualAuditEventsIter.hasNext()) {
                Map<String, Object> actualAuditEvent = actualAuditEventsIter.next();
                Iterator<Map<String, Object>> expectedAuditEventsIter = expectedResultSet.iterator();
                while (expectedAuditEventsIter.hasNext()) {
                    Map<String, Object> expectedAuditEvent = expectedAuditEventsIter.next();
                    boolean customFieldsComparisonResult = verifyFields(actualAuditEvent, expectedAuditEvent);
                    // If the fields did not match and there are still more expected fields to match against
                    // continue to the next expected fields for matching.
                    if (!customFieldsComparisonResult && expectedAuditEventsIter.hasNext()) {
                        LOG.debug("Actual set of Custom Fields returned did not match with Expected set of "
                            + "Custom Fields. Attempting to match with the next set of Expected Custom Fields.");
                        continue;
                    }
                    // If actual and expected fields matched, remove them from their lists and break out of
                    // the loop
                    if (customFieldsComparisonResult) {
                        LOG.info("Actual Custom Fields matched with Expected Custom Fields.");
                        actualAuditEventsIter.remove();
                        expectedAuditEventsIter.remove();
                        break;
                    }
                }
            }
            Assert.assertFalse(actualResultSetLinkedList.size() > 0, "All Actual Fields Should Match For Test "
                               + "File: " + eventsYamlFileName);
            Assert.assertFalse(expectedResultSet.size() > 0, "All Expected Fields Should Match For Test "
                               + "File: " + eventsYamlFileName);
        }
    }

    private static boolean verifyFields(Map<String, Object> actualResult, Map<String, Object> expectedResult)
    {
        System.out.println();
        boolean comparisonResult = false;
        for (Map.Entry<String, Object> expectedField : expectedResult.entrySet()) {
            // Ignore tenantId as this is not stored in the DB.
            if (!expectedField.getKey().equals("tenantId")) {
                try {
                    Object expectedFieldValue = expectedField.getValue();
                    comparisonResult = verifyFieldResult(actualResult, expectedField.getKey(), expectedFieldValue,
                                                         expectedFieldValue.getClass().getSimpleName());
                    if (!comparisonResult) {
                        break;
                    }
                } catch (ParseException e) {
                    // Occurred because could not parse date
                    e.printStackTrace();
                }
            }
        }
        return comparisonResult;
    }

    private static boolean verifyFieldResult(Map<String, Object> result, String field, Object expectedValue, String type) throws ParseException
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
            case "integer":
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

        Object actualFieldValue = null;

        //  Identify matching field in search results.
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            if (entry.getKey().equals(field)) {
                // TODO: CAF-2613 Start of if block to remove
                if (type.equals("Long")) {
                    actualFieldValue = Long.parseLong(String.valueOf(entry.getValue()));
                } else if (type.equals("Short")) {
                    actualFieldValue = Short.parseShort(String.valueOf(entry.getValue()));
                } else {
                    actualFieldValue = entry.getValue();
                }
                // TODO: CAF-2613 End of if block to remove
                //actualFieldValue = entry.getValue();    // TODO: CAF-2613 Uncomment
                break;
            }
        }

        if (actualFieldValue == null) {
            LOG.debug("Map of actual results did not contain expected field: " + field);
            return false;
        }

        LOG.info("Comparing expected key value: " + field + "=" + expectedValue + " with actual key value: "
            + field + "=" + actualFieldValue);
        final String actualFieldValueStr = actualFieldValue.toString();
        if (!type.toLowerCase().equals("date")) {
            final String expectedValueStr = expectedValue.toString();
            if (!Objects.equals(expectedValueStr, actualFieldValueStr)) {
                LOG.warn("Expected Object: " + expectedValue + " mismatched against Actual Object: "
                    + actualFieldValue);
                LOG.warn("Expected Object toString: " + expectedValueStr
                    + " mismatched against Actual Object toString: " + actualFieldValueStr);
                return false;
            }
        } else {
            Date expectedDateValue = (Date) expectedValue;
            if (!datesAreEqual(expectedDateValue, actualFieldValueStr)) {
                LOG.warn("Expected Time Value: " + expectedDateValue.getTime() + " mismatched against Actual Time "
                    + "Value: " + ((Date) actualFieldValue).getTime());
                return false;
            }
        }
        return true;
    }

    private static boolean datesAreEqual(Date expectedDate, String actualDateString) throws ParseException
    {
        //  Convert expected date to similar format used in Elasticsearch search results (default
        // ISODateTimeFormat.dateOptionalTimeParser).
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String expectedDateSting = df.format(expectedDate);

        return expectedDateSting.equals(actualDateString);
    }

    private AuditTestCase getTestCase() throws Exception
    {
        AuditTestCase testCase = new AuditTestCase(Paths.get(testCaseDirectory));
        return testCase;
    }

    private AuditEventMessages getTestMessages(Path eventsYaml) throws Exception
    {
        LOG.info("De-serializing YAML test data {} ...", eventsYaml.toString());
        ObjectMapper mapper = new YAMLMapper();
        AuditEventMessages messages = mapper.readValue(Files.readAllBytes(eventsYaml), AuditEventMessages.class);
        int messageCount = messages.getNumberOfMessages();
        LOG.info("Number of messages to be sent is {}.", messageCount);
        return messages;
    }

    private static LinkedList<Map<String, Object>> sendAuditEventMessages(List<AuditEventMessage> testEventMessages, String tenantId)
        throws Exception
    {
        LinkedList<Map<String, Object>> expectedResultSet = new LinkedList<>();
        final String esHostAndPort = ES_HOSTNAME + ":" + ES_PORT;
        LOG.info("Obtaining Elastic connection and channel...");

        com.hpe.caf.auditing.AuditChannel channel
            = AuditConnectionHelper.getAuditConnection(esHostAndPort, ES_CLUSTERNAME).createChannel();

        Class<?> auditLog;
        Class[] paramTypes = new Class[0];

        LOG.info("Preparing the Kafka auditing infrastructure...");
        AuditLog.declareApplication(channel);

        //  Iterate through each test event message and send to Kafka.
        LOG.info("Processing message test data - sending each test event message Kafka...");
        for (AuditEventMessage testEventMessage : testEventMessages) {
            ArrayList<Parameter> auditLogParams = new ArrayList<>();
            List<AuditEventMessageParam> testMessageParams;
            ArrayList<Object> testMethodArgs = new ArrayList<>();

            //  For each test event message, build a hashmap of expected result data.
            HashMap<String, Object> testEventMessageMap = new HashMap<>();

            LOG.info("Identifying method to be invoked for test message...");
            String methodName = testEventMessage.getAuditLogMethod();

            LOG.info("Getting parameters and parameter types for the method to be invoked for test message...");
            //  Use reflection to get parameters and parameter types.
            auditLog = Class.forName("com.hpe.caf.services.audit.api.AuditLog");
            Method[] methods = auditLog.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    Parameter[] params = method.getParameters();
                    for (Parameter parameter : params) {
                        if (!parameter.isNamePresent()) {
                            throw new IllegalArgumentException("Parameter names are not present!");
                        }
                        auditLogParams.add(parameter);
                    }
                    paramTypes = method.getParameterTypes();
                }
            }

            //  Include AuditChannel in test method args.
            testMethodArgs.add(channel);

            //  Identify parameter test data to be passed to the AuditLog method.
            testMessageParams = testEventMessage.getAuditLogMethodParams();

            LOG.info("Processing method parameters for method invocation...");
            for (AuditEventMessageParam param : testMessageParams) {

                // For each test data event parameter, build up arraylist and hashmap of test data values for further processing.
                for (Parameter alcParam : auditLogParams) {
                    if (alcParam.getName().equals(param.getName())) {
                        Class<?> type = alcParam.getType();
                        if (type.isAssignableFrom(String.class)) {
                            testMethodArgs.add(tenantId);
                            testEventMessageMap.put(param.getName(), tenantId);
                        } else if (type.isAssignableFrom(short.class)) {
                            testMethodArgs.add(Short.parseShort(param.getValue().toString()));
                            testEventMessageMap.put(param.getName(), Short.parseShort(param.getValue().toString()));
                        } else if (type.isAssignableFrom(int.class)) {
                            testMethodArgs.add(Integer.parseInt(param.getValue().toString()));
                            testEventMessageMap.put(param.getName(), Integer.parseInt(param.getValue().toString()));
                        } else if (type.isAssignableFrom(long.class)) {
                            testMethodArgs.add(Long.parseLong(param.getValue().toString()));
                            testEventMessageMap.put(param.getName(), Long.parseLong(param.getValue().toString()));
                        } else if (type.isAssignableFrom(float.class)) {
                            testMethodArgs.add(Float.parseFloat(param.getValue().toString()));
                            testEventMessageMap.put(param.getName(), Float.parseFloat(param.getValue().toString()));
                        } else if (type.isAssignableFrom(double.class)) {
                            testMethodArgs.add(Double.parseDouble(param.getValue().toString()));
                            testEventMessageMap.put(param.getName(), Double.parseDouble(param.getValue().toString()));
                        } else if (type.isAssignableFrom(boolean.class)) {
                            testMethodArgs.add(Boolean.parseBoolean(param.getValue().toString()));
                            testEventMessageMap.put(param.getName(), Boolean.parseBoolean(param.getValue().toString()));
                        } else if (type.isAssignableFrom(Date.class)) {
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX");
                            String sourceTime = param.getValue().toString();
                            // Attempt to parse date that may have a timezone specified
                            Date parsedDate = df.parse(sourceTime, new ParsePosition(0));
                            if (parsedDate == null) {
                                df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                                // This will throw a ParseException if a date cannot be parsed from the sourceTime
                                parsedDate = df.parse(sourceTime);
                            }
                            testMethodArgs.add(parsedDate);
                            testEventMessageMap.put(param.getName(), parsedDate);
                        }
                    }
                }
            }

            LOG.info("Adding event message map to the list of expected result sets...");
            expectedResultSet.add(testEventMessageMap);

            LOG.info("Invoking the target method for test event message...");
            try {
                Object[] params = testMethodArgs.toArray(new Object[testMethodArgs.size()]);
                Method alcMethod = auditLog.getMethod(methodName, paramTypes);
                // alcMethod.invoke(null, params);

                //thread to send messages to multiple partitions.
                SendMessageRunnable sendMessageRunnable = new SendMessageRunnable(alcMethod, params);
                Thread t = new Thread(sendMessageRunnable);
                t.start();
                t.join(); //wait for thread
            } catch (Exception e) {
                LOG.error("Exception caught during method invocation for method {}", methodName);
                throw new Exception(e);
            }
        }

        LOG.info("Completed sending test event messages to Kafka.");
        return expectedResultSet;
    }

    private static class SendMessageRunnable implements Runnable
    {

        Object[] params;
        Method method;

        public SendMessageRunnable(Method method, Object[] params)
        {
            this.params = params;
            this.method = method;
        }

        @Override
        public void run()
        {
            try {
                method.invoke(null, params);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("Exception caught during method invocation for method {}", method);
                throw new AssertionError(e);
            }
        }
    }

    class AuditTestCase
    {
        private Collection<Path> yaml;
        private Path xml;

        public AuditTestCase(Path testCaseDirectory) throws Exception
        {
            yaml = new Vector<>();
            try (DirectoryStream<Path> directoryContents = Files.newDirectoryStream(testCaseDirectory)) {
                for (Path path : directoryContents) {
                    if (Files.isRegularFile(path)) {
                        if (path.getFileSystem().getPathMatcher("glob:*.xml").matches(path.getFileName())) {
                            if (xml == null) {
                                this.xml = path;
                            }
                        } else if (path.getFileSystem().getPathMatcher("glob:*.yaml").matches(path.getFileName())) {
                            this.yaml.add(path);
                        }
                    }
                }
            }
            if (xml == null) {
                throw new Exception("No events XML file was found in the test case directory. Expected to find an audit events file with the extension .xml");
            }
            if (yaml.isEmpty()) {
                throw new Exception("No test case YAML files were found in the test case directory. Expected to find at least one file with the extension .yaml");
            }
        }

        public Path getXml()
        {
            return xml;
        }

        public Collection<Path> getYaml()
        {
            return yaml;
        }
    }

}
