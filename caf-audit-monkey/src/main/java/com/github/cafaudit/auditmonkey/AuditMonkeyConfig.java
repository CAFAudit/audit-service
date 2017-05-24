package com.github.cafaudit.auditmonkey;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditMonkeyConfig
{
    private static final Logger LOG = LoggerFactory.getLogger(AuditMonkeyConfig.class);
    
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
    private int numOfEvents;
    private String monkeyMode;
    
    
    public AuditMonkeyConfig() {
        
        LOG.info("Setting up Audit Monkey Configuration from supplied Environment Variables");

        /*
         * Elasticsearch
         */
        esHostname = System.getProperty(AuditMonkeyConstants.ES_HOSTNAME, System.getenv(AuditMonkeyConstants.ES_HOSTNAME));
        if(null == esHostname || esHostname.isEmpty()) {
            esHostname = "192.168.56.10";
            LOG.debug("No [" + AuditMonkeyConstants.ES_HOSTNAME + "] supplied defaulting to [" + esHostname + "]");
        }
        
        String esPortStr = System.getProperty(AuditMonkeyConstants.ES_PORT, System.getenv(AuditMonkeyConstants.ES_PORT));
        if(null == esPortStr || esPortStr.isEmpty()) {
            esPort = 9300;
            LOG.debug("No [" + AuditMonkeyConstants.ES_PORT + "] supplied defaulting to [" + esPort + "]");
        }
        else {
            esPort = Integer.parseInt(esPortStr);
            if(esPort == 0 || esPort < 0 || esPort > 99999) {
                esPort = 9200;
                LOG.debug("Invalid [" + AuditMonkeyConstants.ES_PORT + "] supplied defaulting to [" + esPort + "]");
            }
        }
        
        esClustername = System.getProperty(AuditMonkeyConstants.ES_CLUSTERNAME, System.getenv(AuditMonkeyConstants.ES_CLUSTERNAME));
        if(null == esClustername || esClustername.isEmpty()) {
            esClustername = "elasticsearch-cluster";
            LOG.debug("No [" + AuditMonkeyConstants.ES_CLUSTERNAME + "] supplied defaulting to [" + esClustername + "]");
        }
        
        esHostnameAndPort = String.format("%s:%s", esHostname, esPort);
        LOG.debug("Elasticsearch Hostname and Port set to [" + esHostnameAndPort + "]");
        
        /*
         * WebService
         */
        wsHostname = System.getProperty(AuditMonkeyConstants.WS_HOSTNAME, System.getenv(AuditMonkeyConstants.WS_HOSTNAME));
        if(null == wsHostname || wsHostname.isEmpty()) {
            wsHostname = "192.168.56.10";
            LOG.debug("No [" + AuditMonkeyConstants.WS_HOSTNAME + "] supplied defaulting to [" + wsHostname + "]");
        }
        
        String wsPortStr = System.getProperty(AuditMonkeyConstants.WS_PORT, System.getenv(AuditMonkeyConstants.WS_PORT));
        if(null == wsPortStr || wsPortStr.isEmpty()) {
            wsPort = 25080;
            LOG.debug("No [" + AuditMonkeyConstants.WS_PORT + "] supplied defaulting to [" + wsPort + "]");            
        } 
        else {
            wsPort = Integer.parseInt(wsPortStr);
            if(wsPort == 0 || wsPort < 0 || wsPort > 9999) {
                wsPort = 25080;
                LOG.debug("Invalid [" + AuditMonkeyConstants.WS_PORT + "] supplied defaulting to [" + wsPort + "]");
            }
        }
        
        wsHostnameAndPort = String.format("%s:%s", wsHostname, wsPort);
        LOG.debug("Audit WebService Hostname and Port set to [" + wsHostnameAndPort + "]");        
        
        /*
         * Audit Events
         */
        tenantId = System.getProperty(AuditMonkeyConstants.CAF_AUDIT_TENANT_ID, System.getenv(AuditMonkeyConstants.CAF_AUDIT_TENANT_ID));
        if(null == tenantId || tenantId.isEmpty()) {
            tenantId = "acmecorp";
            LOG.debug("No [" + AuditMonkeyConstants.CAF_AUDIT_TENANT_ID + "] supplied defaulting to [" + tenantId + "]");
        }
        
        correlationId = System.getProperty(AuditMonkeyConstants.CAF_AUDIT_CORRELATION_ID, System.getenv(AuditMonkeyConstants.CAF_AUDIT_CORRELATION_ID));
        if(null == correlationId || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            LOG.debug("No [" + AuditMonkeyConstants.CAF_AUDIT_CORRELATION_ID + "] supplied defaulting to [" + correlationId + "]");
        }
        
        userId = System.getProperty(AuditMonkeyConstants.CAF_AUDIT_USER_ID, System.getenv(AuditMonkeyConstants.CAF_AUDIT_USER_ID));
        if(null == userId || userId.isEmpty()) {
            userId = "road.runner@acme.com";
            LOG.debug("No [" + AuditMonkeyConstants.CAF_AUDIT_USER_ID + "] supplied defaulting to [" + userId + "]");
        }
        
        /*
         * Monkey
         */
        String numOfEventsStr = System.getProperty(AuditMonkeyConstants.NUM_OF_EVENTS, System.getenv(AuditMonkeyConstants.NUM_OF_EVENTS));
        if(null == numOfEventsStr || numOfEventsStr.isEmpty()) {
            numOfEvents = 1; 
            LOG.debug("No [" + AuditMonkeyConstants.NUM_OF_EVENTS + "] supplied defaulting to [" + numOfEvents + "]");
        }
        else {
            numOfEvents = Integer.parseInt(numOfEventsStr);
            if(numOfEvents <= 0) {
                numOfEvents = 1;
                LOG.debug("Invalid [" + AuditMonkeyConstants.NUM_OF_EVENTS + "] supplied defaulting to [" + numOfEvents + "]");
            }
        }
        
        monkeyMode = System.getProperty(AuditMonkeyConstants.CAF_AUDIT_MONKEY_MODE, System.getenv(AuditMonkeyConstants.CAF_AUDIT_MONKEY_MODE));
        if(null == monkeyMode || monkeyMode.isEmpty()) {
            monkeyMode = AuditMonkeyConstants.STANDARD_MONKEY;
            LOG.debug("No [" + AuditMonkeyConstants.CAF_AUDIT_MONKEY_MODE + "] supplied defaulting to [" + monkeyMode + "]");
        } 
        else if(!monkeyMode.equalsIgnoreCase(AuditMonkeyConstants.STANDARD_MONKEY) || !monkeyMode.equalsIgnoreCase(AuditMonkeyConstants.RANDOM_MONKEY)) {
            LOG.error("The " + AuditMonkeyConstants.CAF_AUDIT_MONKEY_MODE + " supplied [" + monkeyMode + "] does not match the available modes [" + AuditMonkeyConstants.STANDARD_MONKEY + "," + AuditMonkeyConstants.RANDOM_MONKEY + "]");
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


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("AuditMonkeyConfig [esClustername=").append(esClustername).append(", esHostname=").append(esHostname)
                .append(", esPort=").append(esPort).append(", esHostnameAndPort=").append(esHostnameAndPort).append(", wsHostname=")
                .append(wsHostname).append(", wsPort=").append(wsPort).append(", wsHostnameAndPort=").append(wsHostnameAndPort)
                .append(", tenantId=").append(tenantId).append(", correlationId=").append(correlationId).append(", userId=")
                .append(userId).append(", numOfEvents=").append(numOfEvents).append(", monkeyMode=").append(monkeyMode).append("]");
        return builder.toString();
    }
    
}
