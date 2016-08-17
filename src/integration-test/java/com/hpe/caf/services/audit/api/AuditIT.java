package com.hpe.caf.services.audit.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.client.model.HostConfig;
import com.github.dockerjava.client.model.RestartPolicy;
import com.github.dockerjava.jaxrs1.JaxRs1Client;
import com.hpe.caf.api.*;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.cipher.NullCipherProvider;
import com.hpe.caf.config.system.SystemBootstrapConfiguration;
import com.hpe.caf.naming.ServicePath;
import com.hpe.caf.services.audit.client.ApiException;
import com.hpe.caf.services.audit.client.api.ApplicationsApi;
import com.hpe.caf.services.audit.client.api.TenantsApi;
import com.hpe.caf.services.audit.client.model.NewTenant;
import com.hpe.caf.util.ModuleLoader;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.vertica.solutions.kafka.cli.SchedulerConfigurationCLI;
import kafka.admin.AdminOperationException;
import kafka.admin.AdminUtils;
import kafka.common.TopicExistsException;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class AuditIT {

    private static String VERTICA_HOST;
    private static String VERTICA_SSH_PORT;
    private static final String VERTICA_HOST_USERNAME = "dbadmin";
    private static final String VERTICA_HOST_PASSWORD = "password";

    private static String AUDIT_MANAGEMENT_WEBSERVICE_BASE_PATH;

    private static final String AUDIT_MANAGEMENT_ZOOKEEPER_ADDRESS = "192.168.56.20:2181";
    private static final String AUDIT_MANAGEMENT_KAFKA_BROKERS = "192.168.56.20:9092";

    // Default database already created
    private static final String CAF_AUDIT_DATABASE_NAME = "CAFAudit";
    
    // New database to be created by tests
    private static final String AUDIT_IT_DATABASE_NAME = "AuditIT";
    private static String AUDIT_IT_DATABASE_PORT;

    private static final String AUDIT_IT_DATABASE_LOADER_USER = "caf-audit-loader";
    private static final String AUDIT_IT_DATABASE_LOADER_USER_PASSWORD = "loader";
    private static final String AUDIT_IT_DATABASE_READER_ROLE = "caf-audit-read";
    private static final String AUDIT_IT_DATABASE_READER_USER = "caf-audit-reader";
    private static final String AUDIT_IT_DATABASE_READER_USER_PASSWORD = "'reader'";
    private static final String AUDIT_IT_DATABASE_SERVICE_USER = "caf-audit-service";
    private static final String AUDIT_IT_DATABASE_SERVICE_USER_PASSWORD = "'service'";
    private static final String AUDIT_IT_DATABASE_PSEUDOSUPERUSER_ROLE = "PSEUDOSUPERUSER";

    private static final String AUDIT_IT_DATABASE_SCHEMA_PREFIX = "account_";

    private static final String AUDIT_KAFKA_SCHEDULER_ID = "caf-audit-scheduler-it";
    private static String AUDIT_KAFKA_SCHEDULER_IMAGE;
    private static final String AUDIT_KAFKA_SCHEDULER_NAME = "auditscheduler";

    //The audit events XML file in the test case directory must be the same events XML file used in the caf-audit-maven-plugin, see pom.xml property auditXMLConfigFile.
    private static final String testCaseDirectory = "./test-case";

    private static ApplicationsApi auditManagementApplicationsApi;
    private static TenantsApi auditManagementTenantsApi;

    private static final Logger LOG = LoggerFactory.getLogger(AuditIT.class);

    private static void issueVerticaSshCommand(final String command, boolean useDbadmin) throws Exception {
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        JSch jsch = new JSch();
        Session session = jsch.getSession("root", VERTICA_HOST, Integer.parseInt(VERTICA_SSH_PORT));
        session.setConfig(config);
        session.connect();
        
        final String execCmd = useDbadmin
                ? "su - dbadmin -c \"" + command.replace("\"","\\\"") + "\""
                : command;

        try {
            Channel channel = session.openChannel("exec");

            ((ChannelExec)channel).setCommand(execCmd);
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
        issueVerticaSshCommand(command, true);
    }


    private static void startDatabase(final String databaseName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/admintools -t start_db -d {0} -p {0}", databaseName);
        issueVerticaSshCommand(command, true);
    }


    private static void stopDatabase(final String databaseName, final boolean force) throws Exception {
        String command = MessageFormat.format( 
            "/opt/vertica/bin/admintools -t stop_db -d {0} -p {0}" + (force ? " -F" : ""), databaseName);
        issueVerticaSshCommand(command, true);
    }


    private static void dropDatabase(final String databaseName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/admintools -t drop_db -d {0}", databaseName);
        issueVerticaSshCommand(command, true);
    }

    private static void createDatabaseRole(final String databaseName, final String roleName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/vsql -d {0} -U dbadmin -w {0} -c \"CREATE ROLE \\\"{1}\\\" \"", databaseName, roleName);
        issueVerticaSshCommand(command, false);
    }

    private static void grantDatabaseRole(final String databaseName, final String roleName, final String userName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/vsql -d {0} -U dbadmin -w {0} -c \"GRANT \\\"{1}\\\" TO \\\"{2}\\\"\"", databaseName, roleName, userName);
        issueVerticaSshCommand(command, false);
    }

    private static void enableDatabaseRole(final String databaseName, final String roleName, final String userName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/vsql -d {0} -U dbadmin -w {0} -c \"ALTER USER \\\"{1}\\\" DEFAULT ROLE \\\"{2}\\\"\"", databaseName, userName, roleName);
        issueVerticaSshCommand(command, false);
    }

    private static void createDatabaseUser(final String databaseName, final String userName, final String userPassword) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/vsql -d {0} -U dbadmin -w {0} -c \"CREATE USER \\\"{1}\\\" IDENTIFIED BY {2}\"", databaseName, userName, userPassword);
        issueVerticaSshCommand(command, false);
    }

    private static void grantCreateOnDBPrivilege(final String databaseName, final String userName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/vsql -d {0} -U dbadmin -w {0} -c \"GRANT CREATE ON DATABASE {0} TO \\\"{1}\\\" \"", databaseName, userName);
        issueVerticaSshCommand(command, false);
    }

    private static void grantPseudoSuperUserRole(final String databaseName, final String userName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/vsql -d {0} -U dbadmin -w {0} -c \"GRANT PSEUDOSUPERUSER TO \\\"{1}\\\" \"", databaseName, userName);
        issueVerticaSshCommand(command, false);
    }

    private static void grantUsageOnScheduler(final String databaseName, final String userName) throws Exception {
        String command = MessageFormat.format("/opt/vertica/bin/vsql -d {0} -U dbadmin -w {0} -c \"GRANT USAGE ON SCHEMA " + AUDIT_KAFKA_SCHEDULER_NAME + " TO \\\"{1}\\\" \"", databaseName, userName);
        issueVerticaSshCommand(command, false);
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
        
        AUDIT_MANAGEMENT_WEBSERVICE_BASE_PATH = System.getenv("webserviceurl");
        AUDIT_KAFKA_SCHEDULER_IMAGE = System.getenv("auditschedulerimagename");

        VERTICA_HOST = System.getProperty("vertica.host.address");
        VERTICA_SSH_PORT = System.getProperty("vertica.image.ssh.port");
        AUDIT_IT_DATABASE_PORT = System.getProperty("vertica.image.port");

        auditManagementApplicationsApi = new ApplicationsApi();
        auditManagementApplicationsApi.getApiClient().setBasePath(AUDIT_MANAGEMENT_WEBSERVICE_BASE_PATH);

        auditManagementTenantsApi = new TenantsApi();
        auditManagementTenantsApi.getApiClient().setBasePath(AUDIT_MANAGEMENT_WEBSERVICE_BASE_PATH);

        Thread.sleep(10000);
        stopDatabase(CAF_AUDIT_DATABASE_NAME, true);
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

            //  Make sure Kafka scheduler is created and started for test.
            DockerClient docker = new JaxRs1Client();
            createKafkaScheduler(AUDIT_IT_DATABASE_NAME);
            String schedulerContainerId = startKafkaScheduler(docker, AUDIT_IT_DATABASE_NAME);

            String applicationId = registerApplicationInDatabase(testCase);
            for (Path eventsYaml : testCase.getYaml()) {
                AuditEventMessages messagesToSend = getTestMessages(eventsYaml);
                String tenantId = "tenant" + UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
                addTenantInDatabase(tenantId, applicationId);
                List<HashMap<String, Object>> expectedResultSet = sendTestMessages(messagesToSend, tenantId);

                LOG.info("Pausing to allow messages to propagate through Kafka to Vertica...");
                Thread.sleep(10000);

                verifyResults(applicationId, tenantId, expectedResultSet);
            }

            stopKafkaScheduler(docker, schedulerContainerId);
        }

        LOG.info("*** Completed Audit tests ***");
    }

    /**
     * Integration test to verify multiple partitions when:
     * 1. The topic is created with multiple partitions
     * 2. The topic is altered to increase the number of partitions
     */
    @Test
    public void testUpdatePartitionCount() throws Exception{
        LOG.info("*** Beginning Audit UpdatePartitionCount test for topic creation ***");
        partitionTester(true);

        LOG.info("*** Beginning Audit UpdatePartitionCount test for addition of partitions to existing topic ***");
        partitionTester(false);

    }

    /**
     *  Verify partition test, pass in which mode.
     */
    private void partitionTester(boolean createTopicWithMultiplePartitions) throws Exception {

        String schedulerContainerId="";
        DockerClient docker = new JaxRs1Client();

        AuditTestCase testCase = getTestCase();
        try (TestCaseDb testCaseDb = new TestCaseDb(AUDIT_IT_DATABASE_NAME)) {

            //  Make sure Kafka scheduler is created and started for test.
            createKafkaScheduler(AUDIT_IT_DATABASE_NAME);
            schedulerContainerId = startKafkaScheduler(docker,AUDIT_IT_DATABASE_NAME);

            String applicationId = registerApplicationInDatabase(testCase);
            for (Path eventsYaml : testCase.getYaml()) {
                AuditEventMessages messagesToSend = getTestMessages(eventsYaml);
                String tenantId = "tenant" + UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();

                // Create a Kafka Producer
                Properties props = new Properties();
                props.put("bootstrap.servers", AUDIT_MANAGEMENT_KAFKA_BROKERS);
                props.put("acks", "all");
                props.put("retries", 0);
                props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

                //i.e. AuditEventTopic.MyDemo.tenant1
                String topicName = new StringBuilder()
                        .append("AuditEventTopic")
                        .append(".")
                        .append(applicationId)
                        .append(".")
                        .append(tenantId)
                        .toString();

                // If we are testing topic creation with multiple partitions we create topic with zookeeper client in Kafka
                if(createTopicWithMultiplePartitions) {
                    LOG.info("Creating a topic in Kafka with multiple partitions...");

                    // number of partitions to create in the kafka topic
                    int numPartitions = 2;

                    // creating a zookeeper client to interact with zookeeper service running on kafka server.
                    ZkClient zkClient = new ZkClient(AUDIT_MANAGEMENT_ZOOKEEPER_ADDRESS, 10000, 10000, ZKStringSerializer$.MODULE$);

                    // Make sure the topic does not exist then create it with multiple partitions
                    try {
                        AdminUtils.createTopic(zkClient, topicName, numPartitions, 1, new Properties());
                    } catch (TopicExistsException e) {
                        LOG.error("Error - partitionTester: topic already exists '{}'. - '{}'", topicName, e);
                        throw e;
                    }
                }

                // sends the test messages (default is to set Kafka topic to 1 partition)
                LOG.info("Processing message test data - sending each test event message to Kafka...");
                List<HashMap<String, Object>> expectedResultSet = sendTestMessages(messagesToSend, tenantId);

                //Call the addTenant api client method after creating topic in Kafka
                addTenantInDatabase(tenantId, applicationId);

                LOG.info("Pausing to allow messages to propagate through Kafka to Vertica...");
                Thread.sleep(20000);

                //call client to update partition count
                if(!createTopicWithMultiplePartitions) {
                    // number of partitions to create in the kafka topic
                    int numPartitions = 2;

                    // creating a zookeeper client to interact with zookeeper service running on kafka server.
                    ZkClient zkClient = new ZkClient(AUDIT_MANAGEMENT_ZOOKEEPER_ADDRESS, 10000, 10000, ZKStringSerializer$.MODULE$);

                    // Make sure the topic does not exist then create it with multiple partitions
                    if (AdminUtils.topicExists(zkClient, topicName)) {
                        try {
                            AdminUtils.addPartitions(zkClient, topicName, numPartitions, "", true, new Properties());
                        } catch (AdminOperationException e){
                            LOG.error("Error - partitionTester: Failed to add partitions - '{}'", e);
                            throw e;
                        }
                    } else {
                        LOG.error("Error - partitionTester: '{}'", "Topic was not created and partitions cannot be added.");
                        throw new Exception("Topic was not created and partitions cannot be added.");
                    }
                    auditManagementTenantsApi.tenantsTenantIdUpdatePartitionCountPost(tenantId, applicationId);

                    // Send messages again and add the expected messages to the expectedResultSet
                    expectedResultSet.addAll(sendTestMessages(messagesToSend, tenantId));
                    LOG.info("Pausing to allow messages from all partitions to propagate through Kafka to Vertica...");

                    Thread.sleep(20000);
                }

                verifyMultiplePartitionResults(applicationId, tenantId, expectedResultSet);
            }

            stopKafkaScheduler(docker, schedulerContainerId);

        } catch(Exception e){
            System.out.println(e.getMessage());
            stopKafkaScheduler(docker, schedulerContainerId);
            throw e;
        }

        LOG.info("*** Completed Audit UpdatePartitionCount tests ***");

    }


    private AuditTestCase getTestCase() throws Exception {
        AuditTestCase testCase = new AuditTestCase(Paths.get(testCaseDirectory));
        return testCase;
    }


    private String registerApplicationInDatabase(AuditTestCase testCase) throws Exception {
        LOG.info("Registering application in database...");
        File auditEventsDefFile = testCase.getXml().toFile();
        auditManagementApplicationsApi.applicationsPost(auditEventsDefFile);
        return getAuditEventsXmlApplicationId(auditEventsDefFile);
    }


    private AuditEventMessages getTestMessages(Path eventsYaml) throws Exception {
        LOG.info("De-serializing YAML test data {} ...", eventsYaml.toString());
        ObjectMapper mapper = new YAMLMapper();
        AuditEventMessages messages = mapper.readValue(Files.readAllBytes(eventsYaml), AuditEventMessages.class);
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

        NewTenant newTenant = new NewTenant();
        newTenant.setTenantId(tenantName);
        newTenant.setApplication(applications);
        auditManagementTenantsApi.tenantsPost(newTenant);
    }


    private List<HashMap<String, Object>> sendTestMessages(AuditEventMessages messages, String tenantId) throws Exception {
        LOG.info("Loading configuration...");
        BootstrapConfiguration bootstrap = new SystemBootstrapConfiguration();
        Cipher cipher = ModuleLoader.getService(CipherProvider.class, NullCipherProvider.class).getCipher(bootstrap);
        ServicePath path = bootstrap.getServicePath();
        Codec codec = ModuleLoader.getService(Codec.class);
        ManagedConfigurationSource config = ModuleLoader.getService(ConfigurationSourceProvider.class).getConfigurationSource(bootstrap, cipher, path, codec);
        return sendAuditEventMessages(config, messages.getMessages(), tenantId);
    }


    private static List<HashMap<String,Object>> sendAuditEventMessages(final ConfigurationSource config, List<AuditEventMessage> testEventMessages, String tenantId) throws Exception {
        List<HashMap<String,Object>> expectedResultSet = new ArrayList<>();

        LOG.info("Obtaining Kafka connection and channel...");
        try (
                AuditConnection connection = AuditConnectionFactory.createConnection(config);
                com.hpe.caf.auditing.AuditChannel channel = connection.createChannel()
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
                                testMethodArgs.add(tenantId);
                                testEventMessageMap.put(param.getName(), tenantId);
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
                    // alcMethod.invoke(null, params);

                    //thread to send messages to multiple partitions.
                    SendMessageRunnable sendMessageRunnable = new SendMessageRunnable(alcMethod, params);
                    Thread t = new Thread(sendMessageRunnable);
                    t.start();
                    t.join(); //wait for thread
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
        DBUtil dbUtil = new DBUtil(VERTICA_HOST, AUDIT_IT_DATABASE_NAME, AUDIT_IT_DATABASE_PORT, AUDIT_IT_DATABASE_SCHEMA_PREFIX + tenantId, applicationId, VERTICA_HOST_USERNAME, AUDIT_IT_DATABASE_NAME);

        LOG.info("Writing database table rows to disk...");
        dbUtil.writeTableRowsToDisk();

        LOG.info("Getting database table rows as a list...");
        List<HashMap<String,Object>> actualResultSet;
        actualResultSet = dbUtil.getTableRowsAsList();

        LOG.info("Verifying all messages and contents have been sent as expected...");
        boolean matches = doesDatabaseMatch(expectedResultSet, actualResultSet);
        if (!matches) {
            String failureMessage = String.format("Test failure - expected data does not match data returned from Vertica!" +
                    " Expected %d messages, received %d messages from Vertica.",
                    expectedResultSet.size(),
                    actualResultSet.size());
            LOG.error(failureMessage);
            throw new Exception(failureMessage);
        }

        LOG.info("Successfully verified all messages and contents have been sent as expected.");
    }

    private void verifyMultiplePartitionResults(final String applicationId, final String tenantId, List<HashMap<String, Object>> expectedResultSet) throws Exception{
        DBUtil dbUtil = new DBUtil(VERTICA_HOST, AUDIT_IT_DATABASE_NAME, AUDIT_IT_DATABASE_PORT, AUDIT_IT_DATABASE_SCHEMA_PREFIX + tenantId, applicationId, VERTICA_HOST_USERNAME, AUDIT_IT_DATABASE_NAME);

        LOG.info("Writing database table rows to disk...");
        dbUtil.writeTableRowsToDisk();

        LOG.info("Getting database table rows as a list...");
        List<HashMap<String,Object>> actualResultSet;
        actualResultSet = dbUtil.getTableRowsAsList();

        boolean matches = false;
        LOG.info("Verifying all messages and contents have been sent as expected...");
        if(!(expectedResultSet.size() == actualResultSet.size())){
            String failureMessage = String.format("Test failure - expected %d messages, received %d messages from Vertica.",
                    expectedResultSet.size(),
                    actualResultSet.size());
            LOG.error(failureMessage);
            throw new Exception(failureMessage);
        }

        LOG.info(String.format("Test successful - expected %d messages, received %d messages from Vertica.",
                expectedResultSet.size(),
                actualResultSet.size()));
    }

    private static void createKafkaScheduler(String databaseName) throws Exception {
        LOG.info("createKafkaScheduler: Creating Scheduler ...");
        String dbURL = MessageFormat.format("jdbc:vertica://{0}:{1}/{2}", Objects.requireNonNull(VERTICA_HOST), Objects.requireNonNull(AUDIT_IT_DATABASE_PORT), Objects.requireNonNull(databaseName));

        //  Create a scheduler configuration.
        String[] args = new String[]{"-Dscheduler", "--add",
                "--config-schema", AUDIT_KAFKA_SCHEDULER_NAME,
                "--brokers", AUDIT_MANAGEMENT_KAFKA_BROKERS,
                "--username", AUDIT_IT_DATABASE_LOADER_USER,
                "--password", AUDIT_IT_DATABASE_LOADER_USER_PASSWORD,
                "--jdbc-url", dbURL};
        try {
            SchedulerConfigurationCLI.run(args);
        } catch (Exception e) {
            LOG.error("createKafkaScheduler: Scheduler could not be created. ");
            throw e;
        }

        //  Grant usage on scheduler schema to audit service account.
        grantUsageOnScheduler(databaseName,AUDIT_IT_DATABASE_SERVICE_USER);

        LOG.info("createKafkaScheduler: Scheduler created...");
    }

    private static String startKafkaScheduler(DockerClient docker, String databaseName) throws Exception {

        LOG.info("startKafkaScheduler: Launching Scheduler via Docker...");

        final String dbURL = MessageFormat.format("jdbc:vertica://{0}:{1}/{2}", Objects.requireNonNull(VERTICA_HOST), Objects.requireNonNull(AUDIT_IT_DATABASE_PORT), Objects.requireNonNull(databaseName));
        final String[] args = new String[]{
                "launch",
                "--config-schema", AUDIT_KAFKA_SCHEDULER_NAME,
                "--jdbc-url", dbURL,
                "--username", AUDIT_IT_DATABASE_LOADER_USER,
                "--password", AUDIT_IT_DATABASE_LOADER_USER_PASSWORD
        };

        final HostConfig hostConfig = new HostConfig();
        hostConfig.setRestartPolicy(RestartPolicy.unlessStopped());

        String auditSchedulerName = AUDIT_KAFKA_SCHEDULER_ID + "-" + UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();

        final CreateContainerResponse newContainer = docker
                .createContainerCmd(AUDIT_KAFKA_SCHEDULER_IMAGE)
                .withName(auditSchedulerName)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withHostConfig(hostConfig)
                .withCmd(args)
                .exec();

        final String schedulerContainerId = newContainer.getId();

        docker.startContainerCmd(schedulerContainerId).exec();

        LOG.info("startKafkaScheduler: Scheduler launched via Docker...");

        return schedulerContainerId;
    }

    private static void stopKafkaScheduler(DockerClient docker, String schedulerContainerId) throws Exception {

        LOG.info("stopKafkaScheduler: Stopping Scheduler via Docker...");

        docker.stopContainerCmd(schedulerContainerId).exec();
        docker.removeContainerCmd(schedulerContainerId).exec();

        LOG.info("stopKafkaScheduler: Scheduler stopped via Docker...");
    }

    class AuditTestCase {
        private Collection<Path> yaml;
        private Path xml;

        public AuditTestCase(Path testCaseDirectory) throws Exception {
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

        public Path getXml() {
            return xml;
        }

        public Collection<Path> getYaml() {
            return yaml;
        }
    }


    class TestCaseDb implements AutoCloseable {
        private String testDbName;

        public TestCaseDb(final String testDbName) throws Exception {
            this.testDbName = testDbName;
            createDatabase(testDbName);

            createDatabaseRole(testDbName,AUDIT_IT_DATABASE_READER_ROLE);

            createDatabaseUser(testDbName,AUDIT_IT_DATABASE_READER_USER,AUDIT_IT_DATABASE_READER_USER_PASSWORD);
            grantDatabaseRole(testDbName,AUDIT_IT_DATABASE_READER_ROLE,AUDIT_IT_DATABASE_READER_USER);
            enableDatabaseRole(testDbName,AUDIT_IT_DATABASE_READER_ROLE,AUDIT_IT_DATABASE_READER_USER);

            createDatabaseUser(testDbName,AUDIT_IT_DATABASE_SERVICE_USER,AUDIT_IT_DATABASE_SERVICE_USER_PASSWORD);
            grantCreateOnDBPrivilege(testDbName,AUDIT_IT_DATABASE_SERVICE_USER);

            createDatabaseUser(testDbName,AUDIT_IT_DATABASE_LOADER_USER, "'" + AUDIT_IT_DATABASE_LOADER_USER_PASSWORD + "'");
            grantPseudoSuperUserRole(testDbName,AUDIT_IT_DATABASE_LOADER_USER);
            enableDatabaseRole(testDbName,AUDIT_IT_DATABASE_PSEUDOSUPERUSER_ROLE,AUDIT_IT_DATABASE_LOADER_USER);

        }

        @Override
        public void close() throws Exception {
            stopDatabase(testDbName, true);
            dropDatabase(testDbName);
        }
    }

    private static class SendMessageRunnable implements Runnable {

        Object[] params;
        Method method;

        public SendMessageRunnable(Method method, Object[] params) {
            this.params = params;
            this.method = method;
        }

        @Override
        public void run() {
            try {
                method.invoke(null, params);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("Exception caught during method invocation for method {}",method);
                throw new AssertionError(e);
            }
        }
    }
}
