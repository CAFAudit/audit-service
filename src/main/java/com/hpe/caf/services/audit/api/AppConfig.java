package com.hpe.caf.services.audit.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Configuration class for the audit management api. Includes database connection properties.
 */
@Configuration
@PropertySource(value = "file:${CAF_AUDIT_MANAGEMENT_API_CONFIG_PATH}/config.properties", ignoreResourceNotFound = true)
public class AppConfig {

    @Autowired
    private Environment environment;

    public String getDatabaseURL(){
        return environment.getProperty("CAF_DATABASE_URL");
    }

    public String getDatabaseServiceAccount(){
        return environment.getProperty("CAF_DATABASE_SERVICE_ACCOUNT");
    }

    public String getDatabaseServiceAccountPassword(){
        return environment.getProperty("CAF_DATABASE_SERVICE_ACCOUNT_PASSWORD");
    }

    public String getDatabaseLoaderAccount(){
        return environment.getProperty("CAF_DATABASE_LOADER_ACCOUNT");
    }

    public String getDatabaseLoaderAccountPassword(){
        return environment.getProperty("CAF_DATABASE_LOADER_ACCOUNT_PASSWORD");
    }

    public String getDatabaseReaderRole(){
        return environment.getProperty("CAF_DATABASE_READER_ROLE");
    }

    public String getKafkaBrokers(){
        return environment.getProperty("CAF_KAFKA_BROKERS");
    }

    public String getCAFAuditManagementCLI(){ return environment.getProperty("CAF_AUDIT_MANAGEMENT_CLI"); }

    public String getMarathonCPUs(){ return environment.getProperty("CAF_AUDIT_SCHEDULER_MARATHON_CPUS"); }

    public String getMarathonContainerDockerCredentials(){ return environment.getProperty("CAF_AUDIT_SCHEDULER_MARATHON_CONTAINER_DOCKER_CREDENTIALS"); }

    public String getMarathonContainerDockerNetwork(){ return environment.getProperty("CAF_AUDIT_SCHEDULER_MARATHON_CONTAINER_DOCKER_NETWORK"); }

    public String getMarathonContainerDockerForcePullImage(){ return environment.getProperty("CAF_AUDIT_SCHEDULER_MARATHON_CONTAINER_DOCKER_FORCEPULLIMAGE"); }

    public String getMarathonMem(){ return environment.getProperty("CAF_AUDIT_SCHEDULER_MARATHON_MEM"); }

    public String getMarathonConstraints() {
        return environment.getProperty("CAF_AUDIT_SCHEDULER_MARATHON_CONSTRAINTS");
    }

    public String getMarathonUrl(){ return environment.getProperty("CAF_MARATHON_URL"); }

    public String getCAFAuditManagementDisable(){ return environment.getProperty("CAF_AUDIT_MANAGEMENT_DISABLE"); }
}
