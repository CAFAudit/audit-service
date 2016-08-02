package com.hpe.caf.services.audit.api;

import com.vertica.solutions.kafka.cli.TopicConfigurationCLI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KafkaScheduler is responsible for associating a topic with a Kafka Vertica scheduler.
 */
public class KafkaScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaScheduler.class);

    public static void associateTopic(AppConfig properties, String targetTable, String rejectionTable, String targetTopic) throws Exception {
        // Get the number of partitions in Kafka for the topic.
        int numPartitions = TenantUpdatePartitionsPost.getNumberOfPartitionsKafka(properties, targetTopic);

        // If the topic does not exist in Kafka, it will return 0, therefore set num-partitions to 1 in Vertica (default).
        if(numPartitions==0)
            numPartitions = 1;

        //Associate a topic with the scheduler.
        String[] args = new String[]{"-Dtopic", "--add",
                "--config-schema", ApiServiceUtil.KAFKA_SCHEDULER_NAME,
                "--target", targetTable,
                "--rejection-table", rejectionTable,
                "--topic", targetTopic,
                "--parser", "KafkaJSONParser",
                "--username", properties.getDatabaseLoaderAccount(),
                "--password", properties.getDatabaseLoaderAccountPassword(),
                "--jdbc-url", properties.getDatabaseURL(),
                "--num-partitions", Integer.toString(numPartitions)};
        try {
            LOG.info("associateTopic: Creating a Topic configuration. ");
            TopicConfigurationCLI.run(args);
        } catch (Exception e) {
            LOG.error("associateTopic: Topic configuration could not be created. ");
            throw e;
        }
    }
}
