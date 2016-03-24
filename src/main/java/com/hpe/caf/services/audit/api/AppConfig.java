package com.hpe.caf.services.audit.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Configuration class for the audit management api. Includes database connection properties.
 */
@Configuration
@PropertySource(value = "file:${AUDIT_MANAGEMENT_API_CONFIG_PATH}/config.properties", ignoreResourceNotFound = true)
public class AppConfig {

    @Autowired
    private Environment environment;

    public String getDatabaseURL(){
        return environment.getProperty("database.url");
    }

    public String getDatabaseServiceAccount(){
        return environment.getProperty("database.service.account");
    }

    public String getDatabaseServiceAccountPassword(){
        return environment.getProperty("database.service.account.password");
    }

    public String getDatabaseLoaderAccount(){
        return environment.getProperty("database.loader.account");
    }

    public String getDatabaseLoaderAccountPassword(){
        return environment.getProperty("database.loader.account.password");
    }

    public String getDatabaseReaderRole(){
        return environment.getProperty("database.reader.role");
    }

    public String getKafkaBrokers(){
        return environment.getProperty("kafka.brokers");
    }

    public String getCAFAuditManagementCLI(){ return environment.getProperty("caf.audit.management.cli"); }

    public String getMarathonCPUs(){ return environment.getProperty("caf.audit.scheduler.marathon.cpus"); }

    public String getMarathonMem(){ return environment.getProperty("caf.audit.scheduler.marathon.mem"); }

    public String getMarathonInstances(){ return environment.getProperty("caf.audit.scheduler.marathon.instances"); }

    public String[] getMarathonURIs(){ return environment.getProperty("caf.audit.scheduler.marathon.uris").split(","); }

    public String getMarathonContainerType(){ return environment.getProperty("caf.audit.scheduler.marathon.container.type"); }

    public String getMarathonContainerNetwork(){ return environment.getProperty("caf.audit.scheduler.marathon.container.docker.network"); }

    public String getMarathonDockerForcePullImage(){ return environment.getProperty("caf.audit.scheduler.marathon.container.docker.forcePullImage"); }

}
