/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 * Audit Monkey.
 * This is the main class for the Audit Monkey.
 * This class orchestrates the operation of the Audit Monkey.
 */
public class AuditMonkey
{
    private static final Logger LOG = LoggerFactory.getLogger(AuditMonkey.class);

    private static MonkeyConfig monkeyConfig;

    /**
     * Java Main Method
     * @param args Array of String arguments
     * @throws Exception from Main method
     */
    public static void main(String[] args) throws Exception
    {
        LOG.info("Audit Monkey Starting...");
        
        monkeyConfig = new MonkeyConfig();
        
        ConfigurationSource configSource = getConfigSourceBasedOnAuditMode();

        LOG.debug("Creating Audit Connection...");
        try (
            AuditConnection connection = AuditConnectionFactory.createConnection(configSource);
            AuditChannel channel = connection.createChannel()) {
            
            LOG.debug("Ensuring Audit Queue Exists");
            AuditLog.declareApplication(channel);
            
            Monkey monkey = MonkeyFactory.selectMonkey(channel, monkeyConfig);
            
            LOG.debug("Sending Audit Events...");
            
            runTheMonkey(channel, monkey, monkeyConfig.getNumOfThreads());
            
            LOG.debug("...Sending of Audit Events Complete");
        }
        
        LOG.info("...Audit Monkey Exiting");
    }

    protected static void runTheMonkey(AuditChannel channel, Monkey monkey, int numOfThreads)
    {
        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        for (int i = 0; i < numOfThreads; i++) {
            executor.execute((Runnable)monkey);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        LOG.debug("All threads in the multi-threaded Monkey are now finished");
    }

    private static ConfigurationSource getConfigSourceBasedOnAuditMode()
    {
        LOG.info("Creating the Configuration Source Based on the supplied " + MonkeyConstants.CAF_AUDIT_MODE);

        // Get the CAF_AUDIT_MODE environment variable
        // CAF_AUDIT_MODE should be set to either "direct" or "webservice"
        String auditMode = monkeyConfig.getAuditMode();

        ConfigurationSource configSource = null;

        if (auditMode.equalsIgnoreCase(MonkeyConstants.ELASTICSEARCH)) {

            LOG.debug(MonkeyConstants.CAF_AUDIT_MODE + " set to [" + auditMode
                    + "], therefore the Audit Monkey going direct to Elasticsearch");
            configSource = getDirectToElasticConfig();

        } else if (auditMode.equalsIgnoreCase(MonkeyConstants.WEBSERVICE)) {

            LOG.debug(MonkeyConstants.CAF_AUDIT_MODE + " set to [" + auditMode
                    + "], therefore the Audit Monkey is using the WebService");
            configSource = getWebServiceToElasticConfig();
        }
        return configSource;
    }


    private static ConfigurationSource getWebServiceToElasticConfig()
    {
        return new ConfigurationSource() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T getConfiguration(Class<T> type) throws ConfigurationException
            {
                WebServiceClientAuditConfiguration config = new WebServiceClientAuditConfiguration();
                config.setWebServiceEndpoint("http://" + monkeyConfig.getWsHostnameAndPort() + "/caf-audit-service/v1");
                return (T) config;
            }
        };
    }


    private static ConfigurationSource getDirectToElasticConfig()
    {
        return new ConfigurationSource() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T getConfiguration(Class<T> type) throws ConfigurationException
            {
                ElasticAuditConfiguration config = new ElasticAuditConfiguration();
                config.setClusterName(monkeyConfig.getEsClustername());
                config.setHostAndPortValues(monkeyConfig.getEsHostnameAndPort());
                return (T) config;
            }
        };
    }

}
