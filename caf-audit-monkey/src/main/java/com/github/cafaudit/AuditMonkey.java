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
package com.github.cafaudit;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.auditing.elastic.ElasticAuditConfiguration;
import com.hpe.caf.auditing.webserviceclient.WebServiceClientAuditConfiguration;
import com.github.cafaudit.AuditLog;

/**
 * Exemplar worker. This is the class responsible for processing the text data by the action
 * specified in the task.
 */
public class AuditMonkey
{
    private static final Logger LOG = LoggerFactory.getLogger(AuditMonkey.class);
    
    // Monkey Mode: Direct to Elasticsearch OR Via Webservice
    private static final String CAF_AUDIT_MODE = "CAF_AUDIT_MODE";
    
    // Configurable data fields
    private static final String CAF_AUDIT_TENANT_ID = "CAF_AUDIT_TENANT_ID";
    private static final String CAF_AUDIT_CORRELATION_ID = "CAF_AUDIT_CORRELATION_ID";
    private static final String CAF_AUDIT_USER_ID = "CAF_AUDIT_USER_ID"; 

    // Elasticsearch environment variables
    private static final String ES_CLUSTERNAME = "ES_CLUSTERNAME";
    private static final String ES_HOSTNAME = "ES_HOSTNAME";
    private static final String ES_PORT = "ES_PORT";

    // Audit WebService environment variables
    private static final String WS_HOSTNAME = "WS_HOSTNAME";
    private static final String WS_PORT = "WS_PORT";
    
    // Elasticsearch
    private static String esClustername;
    private static String esHostname;
    private static int esPort;
    private static String esHostnameAndPort;
    
    // Audit WebService
    private static String wsHostname;
    private static int wsPort;
    private static String wsHostnameAndPort;
    
    // Audit Events
    private static String tenantId;
    private static String correlationId;
    private static String userId;

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
        
        setConfigFromEnv();
        
        ConfigurationSource configSource = getConfigSourceBasedOnAuditMode();

        LOG.debug("Creating Audit Connection...");
        try (
            AuditConnection connection = AuditConnectionFactory.createConnection(configSource);
            AuditChannel channel = connection.createChannel()) {
            
            LOG.debug("Ensuring Audit Queue Exists");
            AuditLog.declareApplication(channel);
            
            LOG.debug("Sending Audit Events...");
            AuditLog.auditViewDocument(channel, tenantId, userId, correlationId, 1);
            AuditLog.auditViewDocument(channel, tenantId, "wile.e.coyote@acme.com", correlationId, 1);
            AuditLog.auditPolicyApplied(channel, tenantId, "looney.tunes@acme.com", correlationId, 3, "policyName1", "policyDef1");
            LOG.debug("...Sending of Audit Events Complete");
        }
        
        LOG.info("...Audit Monkey Exiting");
    }


    private static ConfigurationSource getConfigSourceBasedOnAuditMode()
    {
        LOG.info("Creating the Configuration Source Based on the supplied " + CAF_AUDIT_MODE);
        
        // Get the CAF_AUDIT_MODE environment variable
        // CAF_AUDIT_MODE should be set to either "direct" or "webservice"
        String auditMode = System.getProperty(CAF_AUDIT_MODE, System.getenv(CAF_AUDIT_MODE));
        if(null == auditMode || auditMode.isEmpty()) {
            String errorMsg = CAF_AUDIT_MODE + " has not been set. " + CAF_AUDIT_MODE + " must be supplied";
            LOG.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        ConfigurationSource configSource;
        
        if(auditMode.equalsIgnoreCase("direct")) {
            
            LOG.debug(CAF_AUDIT_MODE + " set to [" + auditMode +"], therefore the Audit Monkey going direct to Elasticsearch");
            configSource = getDirectToElasticConfig();
            
        } else if (auditMode.equalsIgnoreCase("webservice")) {
        
            LOG.debug(CAF_AUDIT_MODE + " set to [" + auditMode +"], therefore the Audit Monkey is using the WebService");
            configSource = getWebServiceToElasticConfig();
            
        } else {
            String errorMsg = CAF_AUDIT_MODE + " set to [" + auditMode +"], this is not a recognised value for " + CAF_AUDIT_MODE;
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
                config.setWebServiceEndpoint("http://" + wsHostnameAndPort + "/caf-audit-service/v1");
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
                config.setClusterName(esClustername);
                config.setHostAndPortValues(esHostnameAndPort);
//                config.setHostAndPortValues("192.168.56.10:9300,192.168.56.10:9301,192.168.56.10:9302");
//                config.setHostAndPortValues("elasticsearch1:9300,elasticsearch2:9301,elasticsearch3:9302");
                return (T) config;
            }
        };
    }
    
    private static void setConfigFromEnv() {
        
        LOG.info("Setting up Audit Monkey Configuration from supplied Environment Variables");

        /*
         * Elasticsearch
         */
        esHostname = System.getProperty(ES_HOSTNAME, System.getenv(ES_HOSTNAME));
        if(null == esHostname || esHostname.isEmpty()) {
            esHostname = "192.168.56.10";
            LOG.debug("No [" + ES_HOSTNAME + "] supplied defaulting to [" + esHostname + "]");
        }
        
        String esPortStr = System.getProperty(ES_PORT, System.getenv(ES_PORT));
        if(null == esPortStr || esPortStr.isEmpty()) {
            esPort = 9300;
            LOG.debug("No [" + ES_PORT + "] supplied defaulting to [" + esPort + "]");
        }
        else {
            esPort = Integer.parseInt(esPortStr);
            if(esPort == 0 || esPort < 0 || esPort > 99999) {
                esPort = 9200;
                LOG.debug("Invalid [" + ES_PORT + "] supplied defaulting to [" + esPort + "]");
            }
        }
        
        esClustername = System.getProperty(ES_CLUSTERNAME, System.getenv(ES_CLUSTERNAME));
        if(null == esClustername || esClustername.isEmpty()) {
            esClustername = "elasticsearch-cluster";
            LOG.debug("No [" + ES_CLUSTERNAME + "] supplied defaulting to [" + esClustername + "]");
        }
        
        esHostnameAndPort = String.format("%s:%s", esHostname, esPort);
        LOG.debug("Elasticsearch Hostname and Port set to [" + esHostnameAndPort + "]");
        
        /*
         * WebService
         */
        wsHostname = System.getProperty(WS_HOSTNAME, System.getenv(WS_HOSTNAME));
        if(null == wsHostname || wsHostname.isEmpty()) {
            wsHostname = "192.168.56.10";
            LOG.debug("No [" + WS_HOSTNAME + "] supplied defaulting to [" + wsHostname + "]");
        }
        
        String wsPortStr = System.getProperty(WS_PORT, System.getenv(WS_PORT));
        if(null == wsPortStr || wsPortStr.isEmpty()) {
            wsPort = 25080;
            LOG.debug("No [" + WS_PORT + "] supplied defaulting to [" + wsPort + "]");            
        } 
        else {
            wsPort = Integer.parseInt(wsPortStr);
            if(wsPort == 0 || wsPort < 0 || wsPort > 9999) {
                wsPort = 25080;
                LOG.debug("Invalid [" + WS_PORT + "] supplied defaulting to [" + wsPort + "]");
            }
        }
        
        wsHostnameAndPort = String.format("%s:%s", wsHostname, wsPort);
        LOG.debug("Audit WebService Hostname and Port set to [" + wsHostnameAndPort + "]");        
        
        /*
         * Audit Events
         */
        tenantId = System.getProperty(CAF_AUDIT_TENANT_ID, System.getenv(CAF_AUDIT_TENANT_ID));
        if(null == tenantId || tenantId.isEmpty()) {
            tenantId = "acmecorp";
            LOG.debug("No [" + CAF_AUDIT_TENANT_ID + "] supplied defaulting to [" + tenantId + "]");
        }
        
        correlationId = System.getProperty(CAF_AUDIT_CORRELATION_ID, System.getenv(CAF_AUDIT_CORRELATION_ID));
        if(null == correlationId || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            LOG.debug("No [" + CAF_AUDIT_CORRELATION_ID + "] supplied defaulting to [" + correlationId + "]");
        }
        
        userId = System.getProperty(CAF_AUDIT_USER_ID, System.getenv(CAF_AUDIT_USER_ID));
        if(null == userId || userId.isEmpty()) {
            userId = "road.runner@acme.com";
            LOG.debug("No [" + CAF_AUDIT_USER_ID + "] supplied defaulting to [" + userId + "]");
        }
    }

}
