package com.hpe.testapplication.auditing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import com.hpe.caf.api.BootstrapConfiguration;
import com.hpe.caf.api.Cipher;
import com.hpe.caf.api.CipherProvider;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.ConfigurationSourceProvider;
import com.hpe.caf.api.ManagedConfigurationSource;
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

        //  Identify number of messages to be sent as part of this test.
        int messageCount = messages.getNumberOfMessages();
        LOG.debug("Number of messages to be sent is {}...",messageCount);

        //  Identify message content to be sent.
        List<AuditEventMessage> messageArrayList = new ArrayList<AuditEventMessage>();
        messageArrayList = messages.getMessages();

        //  Truncate database table before we start test.
        DBUtil dbUtil = new DBUtil(config);
        dbUtil.truncateTable();

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

                LOG.debug("Processing method parameters for method invocation...");
                for (AuditEventMessageParam param : messageParamsArrayList) {

                    //  Parameter type and value needs converted to class type and java object type respectively for method invocation.
                    String type = param.getType();
                    classTypes.add(convertToJavaClass(type, TestApplication.class.getClassLoader()));

                    switch (type) {
                        case "short":
                            objectTypes.add(Short.parseShort(param.getValue().toString()));
                            break;
                        case "int":
                            objectTypes.add(Integer.parseInt(param.getValue().toString()));
                            break;
                        case "long":
                            objectTypes.add(Long.parseLong(param.getValue().toString()));
                            break;
                        case "float":
                            objectTypes.add(Float.parseFloat(param.getValue().toString()));
                            break;
                        case "double":
                            objectTypes.add(Double.parseDouble(param.getValue().toString()));
                            break;
                        case "boolean":
                            objectTypes.add(Boolean.parseBoolean(param.getValue().toString()));
                            break;
                        case "Date":
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            objectTypes.add(df.parse(param.getValue().toString()));
                            break;
                        default:
                            objectTypes.add(param.getValue());
                    }
                }

                //  Invoke the target method for this message.
                try {
                    Class[] paramTypes = classTypes.toArray(new Class[classTypes.size()]);
                    Object[] params = objectTypes.toArray(new Object[objectTypes.size()]);

                    auditLogClass = Class.forName("com.hpe.testapplication.auditing.AuditLog");
                    method = auditLogClass.getDeclaredMethod(methodName, paramTypes);

                    LOG.debug("Invoking method {}...",methodName);
                    method.invoke(auditLogInstance, params);

                } catch (ClassNotFoundException e) {
                    LOG.error("Exception caught during method invocation for method {}",methodName);
                    throw new Exception(e);
                } catch (IllegalAccessException e) {
                    LOG.error("Exception caught during method invocation for method {}",methodName);
                    throw new Exception(e);
                } catch (NoSuchMethodException e) {
                    LOG.error("Exception caught during method invocation for method {}",methodName);
                    throw new Exception(e);
                } catch (Exception e) {
                    LOG.error("Exception caught during method invocation for method {}",methodName);
                    throw new Exception(e);
                }
            }
        }

        //  Pause to allow messages to be sent through to Kafka and onto Vertica.
        Thread.sleep(10000);

        //  Get row count of target table and verify all messages have been sent through as expected.
        int rowCount = dbUtil.getTableRowCount();

        LOG.debug("Verifying all messages have been sent to Kafka and Vertica...");
        if (rowCount == messageCount) {
            LOG.debug("Writing database rows to disk...");
            dbUtil.writeTableRowsToDisk();

            LOG.debug("Getting database rows as a list...");
            List<HashMap<String,Object>> resultSet = dbUtil.getTableRowsAsList();

        } else {
            LOG.error("Test failure as one or more messages have not been received");
            throw new Exception("Test failure as one or more messages have not been received!");
        }

        LOG.debug("Done.");
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
