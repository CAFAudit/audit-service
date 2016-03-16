package com.hpe.caf.services.audit.api;

import com.vertica.solutions.kafka.cli.SchedulerConfigurationCLI;
import com.vertica.solutions.kafka.cli.TopicConfigurationCLI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KafkaScheduler is responsible for creating a Kafka scheduler and associating a topic with it.
 */
public class KafkaScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaScheduler.class);

    public static void createScheduler(AppConfig properties, String schedulerName) {
        //Create a scheduler configuration.
        String[] args = new String[]{"-Dscheduler", "--add",
                "--config-schema", schedulerName,
                "--brokers", properties.getKafkaBrokers(),
                "--username", properties.getDatabaseLoaderAccount(),
                "--password", properties.getDatabaseLoaderAccountPassword(),
                "--jdbc-url", properties.getDatabaseURL()};
        try {
            LOG.info("createScheduler: Creating a Scheduler configuration. ");
            SchedulerConfigurationCLI.run(args);
        } catch (Exception e) {
            LOG.error("createScheduler: Scheduler configuration could not be created. ");
        }
    }

    public static void associateTopic(AppConfig properties, String schedulerName, String targetTable, String rejectionTable, String targetTopic) {
        //Associate a topic with the scheduler.
        String[] args = new String[]{"-Dtopic", "--add",
                "--config-schema", schedulerName,
                "--target", targetTable,
                "--rejection-table", rejectionTable,
                "--topic", targetTopic,
                "--parser", "KafkaJSONParser",
                "--username", properties.getDatabaseLoaderAccount(),
                "--password", properties.getDatabaseLoaderAccountPassword(),
                "--jdbc-url", properties.getDatabaseURL()};
        try {
            LOG.info("associateTopic: Creating a Topic configuration. ");
            TopicConfigurationCLI.run(args);
        } catch (Exception e) {
            LOG.error("associateTopic: Topic configuration could not be created. ");
        }
    }

}
