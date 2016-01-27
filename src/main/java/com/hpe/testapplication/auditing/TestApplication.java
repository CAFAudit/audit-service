package com.hpe.testapplication.auditing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import com.hpe.caf.api.*;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.cipher.NullCipherProvider;
import com.hpe.caf.config.system.SystemBootstrapConfiguration;
import com.hpe.caf.util.ModuleLoader;
import com.hpe.caf.naming.ServicePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Exception;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestApplication {

    private static final String REQUIRED_INPUTS_FILE_NAME = "caf-audit-qa.yaml";

    private static final List<HashMap<String,Object>> expectedResultSet = new ArrayList<>();

    private static final Logger LOG = LoggerFactory.getLogger(TestApplication.class);

    public static void main(String[] args) throws Exception {

        BootstrapConfiguration bootstrap = new SystemBootstrapConfiguration();
        Cipher cipher = ModuleLoader.getService(CipherProvider.class, NullCipherProvider.class).getCipher(bootstrap);
        ServicePath path = bootstrap.getServicePath();
        Codec codec = ModuleLoader.getService(Codec.class);
        ManagedConfigurationSource config = ModuleLoader.getService(ConfigurationSourceProvider.class).getConfigurationSource(bootstrap, cipher, path, codec);

        //  De-serialize YAML file contents comprising test data.
        LOG.debug("De-serializing YAML test data...");
        ObjectMapper mapper = new YAMLMapper();
        AuditEventMessages messages = mapper.readValue(Files.readAllBytes(Paths.get(REQUIRED_INPUTS_FILE_NAME)), AuditEventMessages.class);

        //  Truncate database table before we start test.
        DBUtil dbUtil = new DBUtil(config);
        dbUtil.truncateTable();

        //  Identify number of audit event messages to be sent as part of this test.
        int messageCount = messages.getNumberOfMessages();
        LOG.debug("Number of messages to be sent is {}...",messageCount);

        //  Get list of audit event messages to be sent.
        List<AuditEventMessage> messageArrayList;
        messageArrayList = messages.getMessages();

        //  Send the audit event messages;
        sendAuditEventMessages(config, messageArrayList);

        //  Pause to allow messages to be sent through to Kafka and onto Vertica.
        Thread.sleep(10000);

        //  Write resultset to disk.
        LOG.debug("Writing database rows to disk...");
        dbUtil.writeTableRowsToDisk();

        //  Get resultset as a list.
        LOG.debug("Getting database rows as a list...");
        List<HashMap<String,Object>> actualResultSet;
        actualResultSet = dbUtil.getTableRowsAsList();

        //  Verify the data returned from the database matches that expected.
        LOG.debug("Verifying all messages and contents have been sent as expected...");
        boolean matches = doesDatabaseMatch(expectedResultSet, actualResultSet);
        if(!matches){
            LOG.error("Test failure - expected data does not match data returned from Vertica!");
            throw new Exception("Test failure - expected data does not match data returned from Vertica!");
        }

        LOG.debug("Done.");
    }

    private static void sendAuditEventMessages(final ConfigurationSource config, List<AuditEventMessage> testEventMessages) throws Exception {
        try (
                AuditConnection connection = AuditConnectionFactory.createConnection(config);
                AuditChannel channel = connection.createChannel()
        ) {
            Class<?> auditLog;
            Class[] paramTypes = new Class[0];

            //  Prepare the auditing infrastructure.
            LOG.debug("Preparing the auditing infrastructure...");
            AuditLog.declareApplication(channel);

            //  Iterate through each test event message and send to Kafka.
            LOG.debug("Processing message test data...");
            for (AuditEventMessage testEventMessage : testEventMessages) {
                ArrayList<Parameter> auditLogParams = new ArrayList<>();
                List<AuditEventMessageParam> testMessageParams;
                ArrayList<Object> testMethodArgs = new ArrayList<>();

                //  For each test event message, build a hashmap of expected result data.
                HashMap<String, Object> testEventMessageMap = new HashMap<>();

                //  Identify AuditLog method to be invoked for this test event message.
                LOG.debug("Identifying method to be invoked...");
                String methodName = testEventMessage.getAuditLogMethod();

                //  Use reflection to get parameters and parameter types for the method to be invoked.
                auditLog = Class.forName("com.hpe.testapplication.auditing.AuditLog");
                Method[] methods = auditLog.getMethods();
                for (Method method : methods) {
                    //  Match on the specified method name.
                    if (method.getName().equals(methodName)) {
                        //  Get a list of parameters.
                        Parameter[] params = method.getParameters();

                        for (Parameter parameter : params) {
                            if (!parameter.isNamePresent()) {
                                throw new IllegalArgumentException("Parameter names are not present!");
                            }
                            auditLogParams.add(parameter);
                        }

                        //  Get list of parameter types.
                        paramTypes = method.getParameterTypes();
                    }
                }

                //  Include AuditChannel in test method args.
                testMethodArgs.add(channel);

                //  Identify parameter test data to be passed to the AuditLog method.
                testMessageParams = testEventMessage.getAuditLogMethodParams();

                LOG.debug("Processing method parameters for method invocation...");
                for (AuditEventMessageParam param : testMessageParams) {

                    // For each test data event parameter, build up arraylist and hashmap of test data values
                    // for further processing.
                    for (Parameter alcParam : auditLogParams) {
                        if (alcParam.getName().equals(param.getName())) {
                            Class<?> type = alcParam.getType();

                            if (type.isAssignableFrom(String.class)) {
                                testMethodArgs.add(param.getValue());
                                testEventMessageMap.put(param.getName(), param.getValue());
                            } else if (type.isAssignableFrom(short.class)) {
                                testMethodArgs.add(Short.parseShort(param.getValue().toString()));
                                testEventMessageMap.put(param.getName(), Long.parseLong(param.getValue().toString()));
                            } else if (type.isAssignableFrom(int.class)) {
                                testMethodArgs.add(Integer.parseInt(param.getValue().toString()));
                                testEventMessageMap.put(param.getName(), Long.parseLong(param.getValue().toString()));
                            } else if (type.isAssignableFrom(long.class)) {
                                testMethodArgs.add(Long.parseLong(param.getValue().toString()));
                                testEventMessageMap.put(param.getName(), Long.parseLong(param.getValue().toString()));
                            } else if (type.isAssignableFrom(float.class)) {
                                testMethodArgs.add(Float.parseFloat(param.getValue().toString()));
                                testEventMessageMap.put(param.getName(), Double.parseDouble(param.getValue().toString()));
                            } else if (type.isAssignableFrom(double.class)) {
                                testMethodArgs.add(Double.parseDouble(param.getValue().toString()));
                                testEventMessageMap.put(param.getName(), Double.parseDouble(param.getValue().toString()));
                            } else if (type.isAssignableFrom(boolean.class)) {
                                testMethodArgs.add(Boolean.parseBoolean(param.getValue().toString()));
                                testEventMessageMap.put(param.getName(), Boolean.parseBoolean(param.getValue().toString()));
                            } else if (type.isAssignableFrom(Date.class)) {
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                testMethodArgs.add(df.parse(param.getValue().toString()));
                                testEventMessageMap.put(param.getName(), df.parse(param.getValue().toString()));
                            }
                        }
                    }
                }

                //  Add each event message map to list of expected result sets.
                expectedResultSet.add(testEventMessageMap);

                //  Invoke the target method for this test event message.
                try {
                    Object[] params = testMethodArgs.toArray(new Object[testMethodArgs.size()]);
                    Method alcMethod = auditLog.getMethod(methodName, paramTypes);
                    alcMethod.invoke(null, params);

                } catch (Exception e) {
                    LOG.error("Exception caught during method invocation for method {}",methodName);
                    throw new Exception(e);
                }
            }
        }
    }

    private static boolean doesDatabaseMatch(List<HashMap<String,Object>> expected, List<HashMap<String,Object>> actual) throws Exception {

        boolean databaseDataMatches = false;

        //  Check sizes first.
        if (expected.size() != actual.size()) {
            LOG.error("Failed to send one or more messages...");
        }
        else {
            // Now iterate through key/values pairs in expected data and try and match up
            // with data returned from the database.
            int hashMapIndex = 0;
            for (HashMap<String, Object> entry : expected) {
                Set<String> keys = entry.keySet();
                for (String key : keys) {
                    Object value = entry.get(key);

                    // Search for key/value pair in actual hashmap and compare
                    // with expected value.
                    HashMap<String, Object> actualHashMap = actual.get(hashMapIndex);
                    Object actualvalue = actualHashMap.get(key);
                    if (!Objects.equals(actualvalue,value)) {

                        //  Database returns timestamp objects and not java Date.
                        //  Convert and re-check before assuming dates are different.
                        if (actualvalue instanceof Timestamp) {
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            Date actualDate = df.parse(actualvalue.toString());
                            if (!Objects.equals(actualDate,value)) {
                                //  Date values still don't match.
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                }

                hashMapIndex++;
            }

            //  Reached this far, so data stored in the database matches test data
            //  in the YAML file.
            databaseDataMatches = true;
        }

        return databaseDataMatches;
    }
}
