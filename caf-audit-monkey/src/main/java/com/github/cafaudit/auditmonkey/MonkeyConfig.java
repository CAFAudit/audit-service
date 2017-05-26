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

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonkeyConfig
{
    private static final Logger LOG = LoggerFactory.getLogger(MonkeyConfig.class);
    
    // Elasticsearch
    private String esClustername;
    private String esHostname;
    private int esPort;
    private String esHostnameAndPort;
    
    // Audit WebService
    private String wsHostname;
    private int wsPort;
    private String wsHostnameAndPort;
    
    // Audit Events
    private String tenantId;
    private String correlationId;
    private String userId;

    // Monkey
    private String monkeyMode;
    private int numOfEvents;
    private int numOfThreads;
    
    
    public MonkeyConfig() {
        
        LOG.info("Setting up Audit Monkey Configuration from supplied Environment Variables");

        /*
         * Elasticsearch
         */
        esHostname = System.getProperty(MonkeyConstants.ES_HOSTNAME, System.getenv(MonkeyConstants.ES_HOSTNAME));
        if(null == esHostname || esHostname.isEmpty()) {
            esHostname = "192.168.56.10";
            LOG.info("No [" + MonkeyConstants.ES_HOSTNAME + "] supplied defaulting to [" + esHostname + "]");
        }
        
        String esPortStr = System.getProperty(MonkeyConstants.ES_PORT, System.getenv(MonkeyConstants.ES_PORT));
        if(null == esPortStr || esPortStr.isEmpty()) {
            esPort = 9300;
            LOG.info("No [" + MonkeyConstants.ES_PORT + "] supplied defaulting to [" + esPort + "]");
        }
        else {
            esPort = Integer.parseInt(esPortStr);
            if(esPort == 0 || esPort < 0 || esPort > 99999) {
                esPort = 9200;
                LOG.warn("Invalid [" + MonkeyConstants.ES_PORT + "] supplied defaulting to [" + esPort + "]");
            }
        }
        
        esClustername = System.getProperty(MonkeyConstants.ES_CLUSTERNAME, System.getenv(MonkeyConstants.ES_CLUSTERNAME));
        if(null == esClustername || esClustername.isEmpty()) {
            esClustername = "elasticsearch-cluster";
            LOG.info("No [" + MonkeyConstants.ES_CLUSTERNAME + "] supplied defaulting to [" + esClustername + "]");
        }
        
        esHostnameAndPort = String.format("%s:%s", esHostname, esPort);
        LOG.info("Elasticsearch Hostname and Port set to [" + esHostnameAndPort + "]");
        
        /*
         * WebService
         */
        wsHostname = System.getProperty(MonkeyConstants.WS_HOSTNAME, System.getenv(MonkeyConstants.WS_HOSTNAME));
        if(null == wsHostname || wsHostname.isEmpty()) {
            wsHostname = "192.168.56.10";
            LOG.info("No [" + MonkeyConstants.WS_HOSTNAME + "] supplied defaulting to [" + wsHostname + "]");
        }
        
        String wsPortStr = System.getProperty(MonkeyConstants.WS_PORT, System.getenv(MonkeyConstants.WS_PORT));
        if(null == wsPortStr || wsPortStr.isEmpty()) {
            wsPort = 25080;
            LOG.info("No [" + MonkeyConstants.WS_PORT + "] supplied defaulting to [" + wsPort + "]");            
        } 
        else {
            wsPort = Integer.parseInt(wsPortStr);
            if(wsPort == 0 || wsPort < 0 || wsPort > 9999) {
                wsPort = 25080;
                LOG.warn("Invalid [" + MonkeyConstants.WS_PORT + "] supplied defaulting to [" + wsPort + "]");
            }
        }
        
        wsHostnameAndPort = String.format("%s:%s", wsHostname, wsPort);
        LOG.debug("Audit WebService Hostname and Port set to [" + wsHostnameAndPort + "]");        
        
        /*
         * Audit Events
         */
        tenantId = System.getProperty(MonkeyConstants.CAF_AUDIT_TENANT_ID, System.getenv(MonkeyConstants.CAF_AUDIT_TENANT_ID));
        if(null == tenantId || tenantId.isEmpty()) {
            tenantId = "acmecorp";
            LOG.info("No [" + MonkeyConstants.CAF_AUDIT_TENANT_ID + "] supplied defaulting to [" + tenantId + "]");
        }
        
        correlationId = System.getProperty(MonkeyConstants.CAF_AUDIT_CORRELATION_ID, System.getenv(MonkeyConstants.CAF_AUDIT_CORRELATION_ID));
        if(null == correlationId || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            LOG.info("No [" + MonkeyConstants.CAF_AUDIT_CORRELATION_ID + "] supplied defaulting to [" + correlationId + "]");
        }
        
        userId = System.getProperty(MonkeyConstants.CAF_AUDIT_USER_ID, System.getenv(MonkeyConstants.CAF_AUDIT_USER_ID));
        if(null == userId || userId.isEmpty()) {
            userId = "road.runner@acme.com";
            LOG.info("No [" + MonkeyConstants.CAF_AUDIT_USER_ID + "] supplied defaulting to [" + userId + "]");
        }
        
        /*
         * Monkey
         */
        monkeyMode = System.getProperty(MonkeyConstants.CAF_AUDIT_MONKEY_MODE, System.getenv(MonkeyConstants.CAF_AUDIT_MONKEY_MODE));
        if(null == monkeyMode || monkeyMode.isEmpty()) {
            monkeyMode = MonkeyConstants.CAF_AUDIT_STANDARD_MONKEY;
            LOG.info("No [" + MonkeyConstants.CAF_AUDIT_MONKEY_MODE + "] supplied defaulting to [" + monkeyMode + "]");
        } 
        else if(!monkeyMode.equalsIgnoreCase(MonkeyConstants.CAF_AUDIT_STANDARD_MONKEY) && !monkeyMode.equalsIgnoreCase(MonkeyConstants.CAF_AUDIT_RANDOM_MONKEY)) {
            LOG.error("The " + MonkeyConstants.CAF_AUDIT_MONKEY_MODE + " supplied [" + monkeyMode + "] does not match the available modes [" + MonkeyConstants.CAF_AUDIT_STANDARD_MONKEY + "," + MonkeyConstants.CAF_AUDIT_RANDOM_MONKEY + "]");
        }

        String numOfEventsStr = System.getProperty(MonkeyConstants.CAF_AUDIT_MONKEY_NUM_OF_EVENTS, System.getenv(MonkeyConstants.CAF_AUDIT_MONKEY_NUM_OF_EVENTS));
        if(null == numOfEventsStr || numOfEventsStr.isEmpty()) {
            numOfEvents = 1; 
            LOG.warn("No [" + MonkeyConstants.CAF_AUDIT_MONKEY_NUM_OF_EVENTS + "] supplied defaulting to [" + numOfEvents + "]");
        }
        else {
            numOfEvents = Integer.parseInt(numOfEventsStr);
            if(numOfEvents <= 0) {
                numOfEvents = 1;
                LOG.warn("Invalid [" + MonkeyConstants.CAF_AUDIT_MONKEY_NUM_OF_EVENTS + "] supplied defaulting to [" + numOfEvents + "]");
            }
        }
        
        String numOfThreadsStr = System.getProperty(MonkeyConstants.CAF_AUDIT_MONKEY_NUM_OF_THREADS, System.getenv(MonkeyConstants.CAF_AUDIT_MONKEY_NUM_OF_THREADS));
        if(null == numOfThreadsStr || numOfThreadsStr.isEmpty()) {
            numOfThreads = 1; 
            LOG.info("No [" + MonkeyConstants.CAF_AUDIT_MONKEY_NUM_OF_THREADS + "] supplied defaulting to [" + numOfThreads + "]");
        }
        else {
            numOfThreads = Integer.parseInt(numOfThreadsStr);
            if(numOfThreads <= 0) {
                numOfThreads = 1;
                LOG.warn("Invalid [" + MonkeyConstants.CAF_AUDIT_MONKEY_NUM_OF_THREADS + "] supplied defaulting to [" + numOfThreads + "]");
            }
        }        
        
        
        LOG.info(this.toString());
    }
    
    
    /**
     * @return the esClustername
     */
    public String getEsClustername()
    {
        return esClustername;
    }
    
    /**
     * @param esClustername the esClustername to set
     */
    public void setEsClustername(String esClustername)
    {
        this.esClustername = esClustername;
    }
    
    /**
     * @return the esHostname
     */
    public String getEsHostname()
    {
        return esHostname;
    }
    
    /**
     * @param esHostname the esHostname to set
     */
    public void setEsHostname(String esHostname)
    {
        this.esHostname = esHostname;
    }
    
    /**
     * @return the esPort
     */
    public int getEsPort()
    {
        return esPort;
    }
    
    /**
     * @param esPort the esPort to set
     */
    public void setEsPort(int esPort)
    {
        this.esPort = esPort;
    }
    
    /**
     * @return the esHostnameAndPort
     */
    public String getEsHostnameAndPort()
    {
        return esHostnameAndPort;
    }
    
    /**
     * @param esHostnameAndPort the esHostnameAndPort to set
     */
    public void setEsHostnameAndPort(String esHostnameAndPort)
    {
        this.esHostnameAndPort = esHostnameAndPort;
    }
    
    /**
     * @return the wsHostname
     */
    public String getWsHostname()
    {
        return wsHostname;
    }
    
    /**
     * @param wsHostname the wsHostname to set
     */
    public void setWsHostname(String wsHostname)
    {
        this.wsHostname = wsHostname;
    }
    
    /**
     * @return the wsPort
     */
    public int getWsPort()
    {
        return wsPort;
    }
    
    /**
     * @param wsPort the wsPort to set
     */
    public void setWsPort(int wsPort)
    {
        this.wsPort = wsPort;
    }
    
    /**
     * @return the wsHostnameAndPort
     */
    public String getWsHostnameAndPort()
    {
        return wsHostnameAndPort;
    }
    
    /**
     * @param wsHostnameAndPort the wsHostnameAndPort to set
     */
    public void setWsHostnameAndPort(String wsHostnameAndPort)
    {
        this.wsHostnameAndPort = wsHostnameAndPort;
    }
    
    /**
     * @return the tenantId
     */
    public String getTenantId()
    {
        return tenantId;
    }
    
    /**
     * @param tenantId the tenantId to set
     */
    public void setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
    }
    
    /**
     * @return the correlationId
     */
    public String getCorrelationId()
    {
        return correlationId;
    }
    
    /**
     * @param correlationId the correlationId to set
     */
    public void setCorrelationId(String correlationId)
    {
        this.correlationId = correlationId;
    }
    
    /**
     * @return the userId
     */
    public String getUserId()
    {
        return userId;
    }
    
    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    /**
     * @return the monkeyMode
     */
    public String getMonkeyMode()
    {
        return monkeyMode;
    }
    
    
    /**
     * @param monkeyMode the monkeyMode to set
     */
    public void setMonkeyMode(String monkeyMode)
    {
        this.monkeyMode = monkeyMode;
    }

    /**
     * @return the numOfEvents
     */
    public int getNumOfEvents()
    {
        return numOfEvents;
    }


    /**
     * @param numOfEvents the numOfEvents to set
     */
    public void setNumOfEvents(int numOfEvents)
    {
        this.numOfEvents = numOfEvents;
    }


    /**
     * @return the numOfThreads
     */
    public int getNumOfThreads()
    {
        return numOfThreads;
    }


    /**
     * @param numOfThreads the numOfThreads to set
     */
    public void setNumOfThreads(int numOfThreads)
    {
        this.numOfThreads = numOfThreads;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MonkeyConfig [esClustername=").append(esClustername).append(", esHostname=").append(esHostname)
                .append(", esPort=").append(esPort).append(", esHostnameAndPort=").append(esHostnameAndPort).append(", wsHostname=")
                .append(wsHostname).append(", wsPort=").append(wsPort).append(", wsHostnameAndPort=").append(wsHostnameAndPort)
                .append(", tenantId=").append(tenantId).append(", correlationId=").append(correlationId).append(", userId=")
                .append(userId).append(", monkeyMode=").append(monkeyMode).append(", numOfEvents=").append(numOfEvents)
                .append(", numOfThreads=").append(numOfThreads).append("]");
        return builder.toString();
    }
    
}