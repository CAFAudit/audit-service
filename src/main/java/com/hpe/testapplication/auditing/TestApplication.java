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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestApplication {

    private static final String REQUIRED_INPUTS_FILE_NAME = "caf-audit-qa.yaml";
    private static final Map CLASS_NAME_TYPE_MAP = new HashMap();

    static {
        CLASS_NAME_TYPE_MAP.put("String", String.class);
        CLASS_NAME_TYPE_MAP.put("boolean", Boolean.TYPE);
        CLASS_NAME_TYPE_MAP.put("byte", Byte.TYPE);
        CLASS_NAME_TYPE_MAP.put("short", Short.TYPE);
        CLASS_NAME_TYPE_MAP.put("int", Integer.TYPE);
        CLASS_NAME_TYPE_MAP.put("long", Long.TYPE);
        CLASS_NAME_TYPE_MAP.put("float", Float.TYPE);
        CLASS_NAME_TYPE_MAP.put("double", Double.TYPE);
        CLASS_NAME_TYPE_MAP.put("Date", Date.class);
    }

    private static List<HashMap<String,Object>> expectedResultSet = new ArrayList<HashMap<String,Object>>();

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
        List<AuditEventMessage> messageArrayList = new ArrayList<AuditEventMessage>();
        messageArrayList = messages.getMessages();

        //  Send the audit event messages;
        sendAuditEventMessages(config, messageArrayList);

        //  Pause to allow messages to be sent through to Kafka and onto Vertica.
        Thread.sleep(10000);

        LOG.debug("Writing database rows to disk...");
        dbUtil.writeTableRowsToDisk();

        LOG.debug("Getting database rows as a list...");
        List<HashMap<String,Object>> actualResultSet = new ArrayList<HashMap<String,Object>>();
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

    private static void sendAuditEventMessages(final ConfigurationSource config, List<AuditEventMessage> messageArrayList) throws Exception {
        try (
                AuditConnection connection = AuditConnectionFactory.createConnection(config);
                AuditChannel channel = connection.createChannel();
        ) {
            //  Prepare the auditing infrastructure.
            LOG.debug("Preparing the auditing infrastructure...");
            AuditLog.declareApplication(channel);

            //  Parse and send each message to kafka.
            Class<?> auditLogClass = null;
            Object auditLogInstance = null;
            Method method = null;
            LOG.debug("Processing message test data...");
            for (AuditEventMessage message : messageArrayList) {

                //  Identify AuditLog method to be invoked.
                LOG.debug("Identifying method to be invoked...");
                String methodName = message.getAuditLogMethod();

                //  Identify parameters to be passed to the AuditLog method.
                List<AuditEventMessageParam> messageParamsArrayList = new ArrayList<AuditEventMessageParam>();
                messageParamsArrayList = message.getAuditLogMethodParams();

                //  Process each parameter and build up object arrays for method invocation.
                ArrayList<Class> classTypes = new ArrayList<Class>();
                ArrayList<Object> objectTypes = new ArrayList<Object>();

                //  Include AuditChannel in parameter setup.
                classTypes.add(AuditChannel.class);
                objectTypes.add(channel);

                //  For each message, build a hashmap of test data so we can compare against retrieved data
                //  from the database.
                HashMap<String, Object> messageMap = new HashMap<String, Object>();

                LOG.debug("Processing method parameters for method invocation...");
                for (AuditEventMessageParam param : messageParamsArrayList) {

                    //  Parameter type and value needs converted to class type and java object type respectively for method invocation.
                    String type = param.getType();
                    classTypes.add(convertToJavaClass(type, TestApplication.class.getClassLoader()));

                    switch (type) {
                        case "short":
                            objectTypes.add(Short.parseShort(param.getValue().toString()));

                            //  Add test data name and values pairs to hashmap. Converting
                            //  to Long here intentionally to compare with database later.
                            messageMap.put(param.getName(),Long.parseLong(param.getValue().toString()));
                            break;
                        case "int":
                            objectTypes.add(Integer.parseInt(param.getValue().toString()));

                            //  Add test data name and values pairs to hashmap. Converting
                            //  to Long here intentionally to compare with database later.
                            messageMap.put(param.getName(),Long.parseLong(param.getValue().toString()));
                            break;
                        case "long":
                            objectTypes.add(Long.parseLong(param.getValue().toString()));

                            //  Add test data name and values pairs to hashmap.
                            messageMap.put(param.getName(),Long.parseLong(param.getValue().toString()));
                            break;
                        case "float":
                            objectTypes.add(Float.parseFloat(param.getValue().toString()));

                            //  Add test data name and values pairs to hashmap. Converting
                            //  to Double here intentionally to compare with database later.
                            messageMap.put(param.getName(),Double.parseDouble(param.getValue().toString()));
                            break;
                        case "double":
                            objectTypes.add(Double.parseDouble(param.getValue().toString()));

                            //  Add test data name and values pairs to hashmap.
                            messageMap.put(param.getName(),Double.parseDouble(param.getValue().toString()));
                            break;
                        case "boolean":
                            objectTypes.add(Boolean.parseBoolean(param.getValue().toString()));

                            //  Add test data name and values pairs to hashmap.
                            messageMap.put(param.getName(),Boolean.parseBoolean(param.getValue().toString()));
                            break;
                        case "Date":
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            objectTypes.add(df.parse(param.getValue().toString()));

                            //  Add test data name and values pairs to hashmap.
                            messageMap.put(param.getName(), df.parse(param.getValue().toString()));
                            break;
                        default:
                            objectTypes.add(param.getValue());

                            //  Add test data name and values pairs to hashmap.
                            messageMap.put(param.getName(),param.getValue());
                    }
                }

                //  Add each message map to list of expected result sets.
                expectedResultSet.add(messageMap);

                //  Invoke the target method for this message.
                try {
                    Class[] paramTypes = classTypes.toArray(new Class[classTypes.size()]);
                    Object[] params = objectTypes.toArray(new Object[objectTypes.size()]);

                    auditLogClass = Class.forName("com.hpe.testapplication.auditing.AuditLog");
                    method = auditLogClass.getDeclaredMethod(methodName, paramTypes);

                    LOG.debug("Invoking method {}...",methodName);
                    method.invoke(auditLogInstance, params);

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
                                databaseDataMatches = false;
                                return databaseDataMatches;
                            }
                        } else {
                            databaseDataMatches = false;
                            return databaseDataMatches;
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

    private final static Class convertToJavaClass(String name, ClassLoader cl)
            throws ClassNotFoundException {

        Class c = (Class) CLASS_NAME_TYPE_MAP.get(name);

        if (c == null) {
            try {
                c = cl.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                throw new ClassNotFoundException("Parameter class not found: " + name);
            }
        }

        return c;
    }
}
