package com.hpe.caf.services.audit.api;

import com.hpe.caf.daemon.DaemonLauncher;
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

    public static void launchScheduler (
        final AppConfig properties,
        final String tenantId,
        final String schedulerName
    ) throws Exception {
        final String id = "caf-audit-" + tenantId;
        final String image = properties.getCAFAuditManagementCLI();
        final String[] args = new String[] {
                "launch",
                "--config-schema", schedulerName,
                "--jdbc-url", properties.getDatabaseURL(),
                "--username", properties.getDatabaseLoaderAccount(),
                "--password", properties.getDatabaseLoaderAccountPassword()
        };

        DaemonLauncher.create(properties).launch(properties, id, image, args);
    }
}
