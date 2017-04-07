/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    public String getCAFAuditManagementDisable(){ return environment.getProperty("CAF_AUDIT_MANAGEMENT_DISABLE"); }
}
