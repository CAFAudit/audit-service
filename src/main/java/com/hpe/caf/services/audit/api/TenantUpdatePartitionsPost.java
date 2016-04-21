package com.hpe.caf.services.audit.api;

import com.hpe.caf.services.audit.api.exceptions.BadRequestException;
import com.hpe.caf.services.audit.api.exceptions.NotFoundException;
import com.vertica.solutions.kafka.cli.TopicConfigurationCLI;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by CS on 07/04/2016.
 */
public class TenantUpdatePartitionsPost {

    private static final String ERR_MSG_SCHEDULER_SCHEMA_NOT_FOUND = "Application not registered with CAF Audit Management Service for this tenant.";
    private static final String ERR_MSG_FAILED_TO_INCREMENT_PARTITIONS = "Failed to increment the number of partitions.";
    private static final String ERR_MSG_PARTITIONS_ZERO = "Tenant not found. Application not registered with CAF Audit Management Service for this tenant.";
    private static final Logger LOG = LoggerFactory.getLogger(TenantAddPost.class);

    /**
     * Checks the number of partitions for the topic for the provided tenantId and applicationId.
     * @param tenantId
     * @param applicationId
     * @return the number of partitions that were added to the Vertica topic configuration.
     * @throws Exception
     */
    public static int checkAndUpdatePartitions(String tenantId, String applicationId) throws Exception{

        //  Get app config settings.
        LOG.debug("checkAndUpdatePartitions: Reading kafka connection properties...");
        AppConfig properties = ApiServiceUtil.getAppConfigProperties();

        //  Only proceed if audit management web service has not been disabled.
        if (properties.getCAFAuditManagementDisable() == null ||
                (properties.getCAFAuditManagementDisable() != null &&
                        properties.getCAFAuditManagementDisable().toUpperCase().equals("FALSE"))) {

            validateInputParameters(tenantId, applicationId);

            // Build the names of the strings which are used to find the partition counts.
            //i.e. AuditEventTopic.MyDemo.tenant1
            String topicName = new StringBuilder()
                    .append(ApiServiceUtil.KAFKA_TARGET_TOPIC_PREFIX)
                    .append(".")
                    .append(applicationId)
                    .append(".")
                    .append(tenantId)
                    .toString();

            //i.e. account_tenant1.AuditMyDemo
            String targetTable = new StringBuilder()
                    .append(ApiServiceUtil.TENANTID_SCHEMA_PREFIX)
                    .append(tenantId)
                    .append(".")
                    .append(ApiServiceUtil.KAFKA_TARGET_TABLE_PREFIX)
                    .append(applicationId)
                    .toString();

            //i.e. account_tenant1.kafka_rej
            String rejectionTable = new StringBuilder()
                    .append(ApiServiceUtil.TENANTID_SCHEMA_PREFIX)
                    .append(tenantId)
                    .append(".")
                    .append(ApiServiceUtil.KAFKA_REJECT_TABLE)
                    .toString();

            //i.e. auditScheduler_tenant1
            String schedulerName = new StringBuilder()
                    .append(ApiServiceUtil.KAFKA_SCHEDULER_NAME_PREFIX)
                    .append(tenantId)
                    .toString();

            // Get the number of partitions in Kafka
            final int numberOfPartitionsKafka = getNumberOfPartitionsKafka(properties, topicName);

            // Get the number of partitions in Vertica
            final int originalNumberOfPartitionsVertica = getNumberOfPartitionsVertica(properties, schedulerName, topicName);
            int currentPartitionsVertica = originalNumberOfPartitionsVertica;

            // If there are more partitions in kafka than in Vertica, add partitions incrementally.
            while( numberOfPartitionsKafka > currentPartitionsVertica ) {
                int partitionToAdd = currentPartitionsVertica;
                //create the CLI command
                String[] args = new String[]{"-Dtopic", "--add",
                        "--config-schema", schedulerName,
                        "--target", targetTable,
                        "--rejection-table", rejectionTable,
                        "--topic", topicName,
                        "--partition", Integer.toString(partitionToAdd),
                        "--parser", "KafkaJSONParser",
                        "--username", properties.getDatabaseLoaderAccount(),
                        "--password", properties.getDatabaseLoaderAccountPassword(),
                        "--jdbc-url", properties.getDatabaseURL()};

                try {
                    //Run the CLI option for adding partition to topic
                    LOG.info("checkAndUpdatePartitions: Adding an extra partition to the topic configuration. ");
                    TopicConfigurationCLI.run(args);
                } catch (Exception e) {
                    LOG.error("checkAndUpdatePartitions: Extra partition could not be added to the topic configuration. ");
                    throw e;
                }

                // Check number of partitions in Vertica to make sure they were incremented
                currentPartitionsVertica = getNumberOfPartitionsVertica(properties, schedulerName, topicName);
            }

            // Get number partitionsAdded. If this partitionsAdded < 0, the topic isn't created in Kafka, leave partitionsAdded as 0.
            int partitionsAdded = numberOfPartitionsKafka - originalNumberOfPartitionsVertica;
            if (partitionsAdded < 0) partitionsAdded = 0;
            LOG.debug("checkAndUpdatePartitions: Number of Vertica topic configuration partitions added: '{}'", partitionsAdded);
            return partitionsAdded;
        }
        return 0;
    }


    /**
     *
     * Public method to get hte number of partitions in Kafka by calling the kafka-clients library and using a consumer
     * function.
     * @param properties - AppConfig with environment variables to create the KafkaConsumer
     * @param topicName - Name of the topic to check number of partitions
     * @return the number of partitions in Kafka for the topic name.
     * @throws Exception
     */
    public static int getNumberOfPartitionsKafka(AppConfig properties, String topicName) throws Exception{
        int numPartitionsKafka = 0;

        //create properties for the Kafka consumer to connect to the Kafka server
        Properties kafkaConsumerProperties = new Properties();
        kafkaConsumerProperties.put("bootstrap.servers", properties.getKafkaBrokers());
        kafkaConsumerProperties.put("key.deserializer", StringDeserializer.class.getName());
        kafkaConsumerProperties.put("value.deserializer", StringDeserializer.class.getName());

        //create the kafka consumer which can be used to get topic information
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaConsumerProperties)){
            LOG.debug("checkAndUpdatePartitions: Getting number of partitions in Kafka for topic '{}'...", topicName);

            // Get a list of all topics
            Map<String, List<PartitionInfo>> topicList = consumer.listTopics();

            // From the topicList map, get the topic with the topicName as the key
            List<PartitionInfo> partitionInfoList = topicList.get(topicName);

            // If the topic does not exist it will return null. Therefore, leave numberOfPartitionsKafka as 0.
            if(partitionInfoList==null) {
                LOG.debug("checkAndUpdatePartitions: Topic '{}' does not exist in Kafka.", topicName);
            } else {
                //otherwise, set the number of partitions to the size of the partitionInfoList for that topicName.
                numPartitionsKafka = partitionInfoList.size();
            }
        } catch (WakeupException e) {
            LOG.error("checkAndUpdatePartitions: Wakeup() is called before or while this function was called.");
            throw e;
        } catch (AuthorizationException e) {
            LOG.error("checkAndUpdatePartitions: Not authorized to the specified topic.");
            throw e;
        } catch (org.apache.kafka.common.errors.TimeoutException e) {
            LOG.error("checkAndUpdatePartitions: Topic metadata could not be fetched before the expiration of the configured request timeout.");
            throw e;
        } catch (KafkaException e) {
            LOG.error("checkAndUpdatePartitions: Unrecoverable error occurred.");
            throw e;
        }
        return numPartitionsKafka;
    }


    /**
     * Private method to get the number of partitions in Vertica by querying the Vertica database provided in the config
     * (from environment variable).
     * @param properties
     * @param schedulerName
     * @param topicName
     * @return
     * @throws Exception
     */
    private static int getNumberOfPartitionsVertica(AppConfig properties, String schedulerName, String topicName) throws Exception {
        int numPartitonsVertica = 0;

        DatabaseHelper databaseHelper = new DatabaseHelper(properties);

        boolean schemaExists = databaseHelper.doesSchemaExist(schedulerName);
        if(!schemaExists){
            LOG.error("checkAndUpdatePartitions: Error - '{}'", ERR_MSG_SCHEDULER_SCHEMA_NOT_FOUND);
            throw new NotFoundException(ERR_MSG_SCHEDULER_SCHEMA_NOT_FOUND);
        }

        try {
            LOG.debug("checkAndUpdatePartitions: Getting number of partitions in Vertica for topic '{}' in schema '{}'...", topicName, schedulerName);
            numPartitonsVertica = databaseHelper.getNumberPartitions(schedulerName, topicName);
        } catch (Exception e) {
            LOG.error("checkAndUpdatePartitions: Error - '{}'", "Error retrieving number of partitions from Vertica.");
            throw e;
        }

        if(numPartitonsVertica==0){
            LOG.error("checkAndUpdatePartitions: Error - '{}'", ERR_MSG_PARTITIONS_ZERO);
            throw new NotFoundException(ERR_MSG_PARTITIONS_ZERO);
        }
        return numPartitonsVertica;
    }


    /**
     * Private method to validate the inputs to the web method.
     * @param tenantId
     * @param applicationId
     * @throws BadRequestException
     */
    private static void validateInputParameters(String tenantId, String applicationId) throws BadRequestException {
        //  Make sure the tenant id does not contain any invalid characters.
        if (containsInvalidCharacters(tenantId)) {
            LOG.error("checkAndUpdatePartitions: Error - '{}'", ApiServiceUtil.ERR_MSG_TENANTID_CONTAINS_INVALID_CHARS);
            throw new BadRequestException(ApiServiceUtil.ERR_MSG_TENANTID_CONTAINS_INVALID_CHARS);
        }
    }


    /**
     * Returns TRUE if the specified tenantId contains invalid characters, otherwise FALSE.
     */
    private static boolean containsInvalidCharacters(String tenantId) {
        if (tenantId.matches(ApiServiceUtil.TENANTID_INVALID_CHARS_REGEX)) {
            return false;
        }
        return true;
    }

}
