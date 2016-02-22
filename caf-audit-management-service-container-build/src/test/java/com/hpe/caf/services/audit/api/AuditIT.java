package com.hpe.caf.services.audit.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.hpe.caf.api.*;
import com.hpe.caf.api.Cipher;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.cipher.NullCipherProvider;
import com.hpe.caf.config.system.SystemBootstrapConfiguration;
import com.hpe.caf.naming.ServicePath;
import com.hpe.caf.services.audit.client.ApiException;
import com.hpe.caf.services.audit.client.api.DefaultApi;
import com.hpe.caf.util.ModuleLoader;
import com.jcraft.jsch.*;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class AuditIT {

    private static final String VERTICA_HOST = "192.168.56.30";
    private static final String VERTICA_HOST_USERNAME = "dbadmin";
    private static final String VERTICA_HOST_PASSWORD = "password";

    private static final String AUDIT_MANAGEMENT_WEBSERVICE_BASE_PATH = "http://127.0.0.1:25080/caf-audit-management/v1";

    private static final String CAF_AUDIT_DATABASE_NAME = "CAFAudit";
    private static final String AUDIT_IT_DATABASE_NAME = "AuditIT";
    private static final String AUDIT_IT_DATABASE_PORT = "5433";

    //Must be the same events XML file used in the caf-audit-plugin, see pom.xml property auditXMLConfigFile.
    private static final String auditXMLConfigFile = "./test-case/events.xml";

    private static final String auditYAMLFile = "./test-case/events.yaml";

    private static DefaultApi auditManagementWsClient;

    private static final Logger LOG = LoggerFactory.getLogger(AuditIT.class);

    private static void issueVerticaSshCommand(final String command) throws Exception {
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        JSch jsch = new JSch();
        Session session = jsch.getSession(VERTICA_HOST_USERNAME, VERTICA_HOST);
        session.setPassword(VERTICA_HOST_PASSWORD);
        session.setConfig(config);
        session.connect();

        try {
            Channel channel = session.openChannel("exec");

            ((ChannelExec)channel).setCommand(command);
            ((ChannelExec)channel).setErrStream(System.err);

            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] readBuffer = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int bytesRead = in.read(readBuffer, 0, 1024);
                    if (bytesRead < 0) {
                        break;
                    }
                    System.out.print(new String(readBuffer, 0, bytesRead));
                }

                if (channel.isClosed()) {
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch(InterruptedException ie)
                {}
            }

            channel.disconnect();
        } finally {
            session.disconnect();
        }
    }


    private static void createDatabase(final String databaseName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/admintools -t create_db -d {0} -p {0} -s 127.0.0.1", databaseName);
        issueVerticaSshCommand(command);
    }


    private static void startDatabase(final String databaseName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/admintools -t start_db -d {0} -p {0}", databaseName);
        issueVerticaSshCommand(command);
    }


    private static void stopDatabase(final String databaseName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/admintools -t stop_db -d {0} -p {0}", databaseName);
        issueVerticaSshCommand(command);
    }


    private static void dropDatabase(final String databaseName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/admintools -t drop_db -d {0}", databaseName);
        issueVerticaSshCommand(command);
    }


    private static String getAuditEventsXmlApplicationId(File auditEventsXml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new FileInputStream(auditEventsXml));
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        Node applicationIdNode = (Node) xpath.evaluate("//AuditedApplication/ApplicationId", doc, XPathConstants.NODE);
        String applicationId = applicationIdNode.getTextContent();
        return applicationId;
    }


    @BeforeClass
    public static void setup() throws Exception {
        auditManagementWsClient = new DefaultApi();
        auditManagementWsClient.getApiClient().setBasePath(AUDIT_MANAGEMENT_WEBSERVICE_BASE_PATH);
        stopDatabase(CAF_AUDIT_DATABASE_NAME);
    }


    @AfterClass
    public static void teardown() throws Exception {
        startDatabase(CAF_AUDIT_DATABASE_NAME);
    }


    @Test
    public void executeAuditTests() throws Exception {
        LOG.info("*** Beginning Audit tests ***");

        AuditTestCase testCase = getTestCase();
        try (TestCaseDb testCaseDb = new TestCaseDb(AUDIT_IT_DATABASE_NAME)) {
            String applicationId = registerApplicationInDatabase(testCase);
            AuditEventMessages messagesToSend = getTestMessages(testCase);
            String tenantId = getTenantId(messagesToSend);
            addTenantInDatabase(tenantId, applicationId);
            List<HashMap<String, Object>> expectedResultSet = sendTestMessages(messagesToSend);

            LOG.info("Pausing to allow messages to propagate through Kafka to Vertica...");
            Thread.sleep(10000);

            verifyResults(applicationId, tenantId, expectedResultSet);
        }

        LOG.info("*** Completed Audit tests ***");
    }


    private AuditTestCase getTestCase() throws Exception {
        Path xml = Paths.get(auditXMLConfigFile);
        Path yaml = Paths.get(auditYAMLFile);
        AuditTestCase testCase = new AuditTestCase(xml, yaml);
        return testCase;
    }


    private String registerApplicationInDatabase(AuditTestCase testCase) throws Exception {
        LOG.info("Registering application in database...");
        File auditEventsDefFile = testCase.getXml().toFile();
        auditManagementWsClient.applicationsPost(auditEventsDefFile);
        return getAuditEventsXmlApplicationId(auditEventsDefFile);
    }


    private AuditEventMessages getTestMessages(AuditTestCase testCase) throws Exception {
        LOG.info("De-serializing YAML test data...");
        ObjectMapper mapper = new YAMLMapper();
        AuditEventMessages messages = mapper.readValue(Files.readAllBytes(testCase.getYaml()), AuditEventMessages.class);
        int messageCount = messages.getNumberOfMessages();
        LOG.info("Number of messages to be sent is {}.", messageCount);
        return messages;
    }


    private String getTenantId(AuditEventMessages messages) throws Exception {
        LOG.info("Getting the tenant name from the test data...");
        //Use the first tenantId we find in the messages.
        String tenantId = null;
        for (AuditEventMessage message : messages.getMessages()) {
            List<AuditEventMessageParam> methodParams = message.getAuditLogMethodParams();
            for (AuditEventMessageParam param : methodParams) {
                if (param.getName().equalsIgnoreCase("tenantId")) {
                    tenantId = (String)param.getValue();
                    break;
                }
            }
        }
        if (tenantId == null) {
            throw new Exception("No tenantId param found in messages being sent.");
        }
        LOG.info("Tenant name is {}.", tenantId);
        return tenantId;
    }


    private void addTenantInDatabase(String tenantName, String applicationId) throws ApiException {
        LOG.info("Adding tenant in database...");
        List<String> applications = new ArrayList<>();
        applications.add(applicationId);
        auditManagementWsClient.tenantsPost(tenantName, applications);
    }


    private List<HashMap<String, Object>> sendTestMessages(AuditEventMessages messages) throws Exception {
        LOG.info("Loading configuration...");
        BootstrapConfiguration bootstrap = new SystemBootstrapConfiguration();
        Cipher cipher = ModuleLoader.getService(CipherProvider.class, NullCipherProvider.class).getCipher(bootstrap);
        ServicePath path = bootstrap.getServicePath();
        Codec codec = ModuleLoader.getService(Codec.class);
        ManagedConfigurationSource config = ModuleLoader.getService(ConfigurationSourceProvider.class).getConfigurationSource(bootstrap, cipher, path, codec);
        return sendAuditEventMessages(config, messages.getMessages());
    }


    private static List<HashMap<String,Object>> sendAuditEventMessages(final ConfigurationSource config, List<AuditEventMessage> testEventMessages) throws Exception {
        List<HashMap<String,Object>> expectedResultSet = new ArrayList<>();

        LOG.info("Obtaining Kafka connection and channel...");
        try (
                AuditConnection connection = AuditConnectionFactory.createConnection(config);
                AuditChannel channel = connection.createChannel()
        ) {
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

                LOG.info("Adding event message map to the list of expected result sets...");
                expectedResultSet.add(testEventMessageMap);

                LOG.info("Invoking the target method for test event message...");
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

        LOG.info("Completed sending test event messages to Kafka.");

        return expectedResultSet;
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
                    // with expected value. Ignore tenantId as this is not stored in the
                    // DB.
                    HashMap<String, Object> actualHashMap = actual.get(hashMapIndex);
                    Object actualvalue = actualHashMap.get(key);
                    if (!key.equals("tenantId")) {
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
                }

                hashMapIndex++;
            }

            //  Reached this far, so data stored in the database matches test data
            //  in the YAML file.
            databaseDataMatches = true;
        }

        return databaseDataMatches;
    }


    private void verifyResults(final String applicationId, final String tenantId, List<HashMap<String, Object>> expectedResultSet) throws Exception {
        DBUtil dbUtil = new DBUtil(VERTICA_HOST, AUDIT_IT_DATABASE_NAME, AUDIT_IT_DATABASE_PORT, tenantId, applicationId, VERTICA_HOST_USERNAME, AUDIT_IT_DATABASE_NAME);

        LOG.info("Writing database table rows to disk...");
        dbUtil.writeTableRowsToDisk();

        LOG.info("Getting database table rows as a list...");
        List<HashMap<String,Object>> actualResultSet;
        actualResultSet = dbUtil.getTableRowsAsList();

        LOG.info("Verifying all messages and contents have been sent as expected...");
        boolean matches = doesDatabaseMatch(expectedResultSet, actualResultSet);
        if (!matches) {
            String failureMessage = "Test failure - expected data does not match data returned from Vertica!";
            LOG.error(failureMessage);
            throw new Exception(failureMessage);
        }

        LOG.info("Successfully verified all messages and contents have been sent as expected.");
    }


    class AuditTestCase {
        private Path yaml;
        private Path xml;

        public AuditTestCase(Path xml, Path yaml) {
            this.xml = xml;
            this.yaml = yaml;
        }

        public Path getXml() {
            return xml;
        }

        public Path getYaml() {
            return yaml;
        }
    }


    class TestCaseDb implements AutoCloseable {
        private String testDbName;

        public TestCaseDb(final String testDbName) throws Exception {
            this.testDbName = testDbName;
            createDatabase(testDbName);
        }

        @Override
        public void close() throws Exception {
            stopDatabase(testDbName);
            dropDatabase(testDbName);
        }
    }
}
