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
package com.github.cafaudit.auditmonkey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cafaudit.AuditLog;
import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.auditing.elastic.ElasticAuditConfiguration;
import com.hpe.caf.auditing.webserviceclient.WebServiceClientAuditConfiguration;

/**
 * Exemplar worker. This is the class responsible for processing the text data by the action
 * specified in the task.
 */
public class AuditMonkey
{
    private static final Logger LOG = LoggerFactory.getLogger(AuditMonkey.class);
    
    private static AuditMonkeyConfig auditMonkeyConfig;
    
    /**
     * Default No Args Constructor
     */
    public AuditMonkey()
    {}

    /**
     * Java Main Method
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        LOG.info("Audit Monkey Starting...");
        
        auditMonkeyConfig = new AuditMonkeyConfig();
        
        ConfigurationSource configSource = getConfigSourceBasedOnAuditMode();

        LOG.debug("Creating Audit Connection...");
        try (
            AuditConnection connection = AuditConnectionFactory.createConnection(configSource);
            AuditChannel channel = connection.createChannel()) {
            
            LOG.debug("Ensuring Audit Queue Exists");
            AuditLog.declareApplication(channel);
            
            Monkey monkey = selectMonkey(auditMonkeyConfig.getMonkeyMode());
            
            LOG.debug("Sending Audit Events...");
            
            monkey.execute(channel, auditMonkeyConfig);
            
//            AuditLog.auditViewDocument(channel, auditMonkeyConfig.getTenantId(), auditMonkeyConfig.getUserId(), auditMonkeyConfig.getCorrelationId(), 1);
//            AuditLog.auditViewDocument(channel, auditMonkeyConfig.getTenantId(), "wile.e.coyote@acme.com", auditMonkeyConfig.getCorrelationId(), 1);
//            AuditLog.auditPolicyApplied(channel, auditMonkeyConfig.getTenantId(), "looney.tunes@acme.com", auditMonkeyConfig.getCorrelationId(), 3, "policyName1", "policyDef1");
            LOG.debug("...Sending of Audit Events Complete");
        }
        
        LOG.info("...Audit Monkey Exiting");
    }

    private static Monkey selectMonkey(String monkeyMode) 
    {
        LOG.debug("Selecting type of Monkey to execute");
        Monkey monkey = MonkeyFactory.selectMonkey(monkeyMode);
        return monkey;
    }
    
    private static ConfigurationSource getConfigSourceBasedOnAuditMode()
    {
        LOG.info("Creating the Configuration Source Based on the supplied " + AuditMonkeyConstants.CAF_AUDIT_MODE);
        
        // Get the CAF_AUDIT_MODE environment variable
        // CAF_AUDIT_MODE should be set to either "direct" or "webservice"
        String auditMode = System.getProperty(AuditMonkeyConstants.CAF_AUDIT_MODE, System.getenv(AuditMonkeyConstants.CAF_AUDIT_MODE));
        if(null == auditMode || auditMode.isEmpty()) {
            String errorMsg = AuditMonkeyConstants.CAF_AUDIT_MODE + " has not been set. " + AuditMonkeyConstants.CAF_AUDIT_MODE + " must be supplied";
            LOG.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        ConfigurationSource configSource;
        
        if(auditMode.equalsIgnoreCase("direct")) {
            
            LOG.debug(AuditMonkeyConstants.CAF_AUDIT_MODE + " set to [" + auditMode +"], therefore the Audit Monkey going direct to Elasticsearch");
            configSource = getDirectToElasticConfig();
            
        } else if (auditMode.equalsIgnoreCase("webservice")) {
        
            LOG.debug(AuditMonkeyConstants.CAF_AUDIT_MODE + " set to [" + auditMode +"], therefore the Audit Monkey is using the WebService");
            configSource = getWebServiceToElasticConfig();
            
        } else {
            String errorMsg = AuditMonkeyConstants.CAF_AUDIT_MODE + " set to [" + auditMode +"], this is not a recognised value for " + AuditMonkeyConstants.CAF_AUDIT_MODE;
            LOG.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        return configSource;
    }


    private static ConfigurationSource getWebServiceToElasticConfig()
    {
        return new ConfigurationSource()
        {
            @Override
            public <T> T getConfiguration(Class<T> type) throws ConfigurationException
            {
                WebServiceClientAuditConfiguration config = new WebServiceClientAuditConfiguration();
                config.setWebServiceEndpoint("http://" + auditMonkeyConfig.getWsHostnameAndPort() + "/caf-audit-service/v1");
                return (T) config;
            }
        };
    }


    private static ConfigurationSource getDirectToElasticConfig()
    {
        return new ConfigurationSource()
        {
            @Override
            public <T> T getConfiguration(Class<T> type) throws ConfigurationException
            {
                ElasticAuditConfiguration config = new ElasticAuditConfiguration();
                config.setClusterName(auditMonkeyConfig.getEsClustername());
                config.setHostAndPortValues(auditMonkeyConfig.getEsHostnameAndPort());
//                config.setHostAndPortValues("192.168.56.10:9300,192.168.56.10:9301,192.168.56.10:9302");
//                config.setHostAndPortValues("elasticsearch1:9300,elasticsearch2:9301,elasticsearch3:9302");
                return (T) config;
            }
        };
    }

}
