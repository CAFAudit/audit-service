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

    public String getDatabaseUsername(){
        return environment.getProperty("database.username");
    }

    public String getDatabasePassword(){
        return environment.getProperty("database.password");
    }

    public String getKafkaBrokers(){
        return environment.getProperty("kafka.brokers");
    }
}
