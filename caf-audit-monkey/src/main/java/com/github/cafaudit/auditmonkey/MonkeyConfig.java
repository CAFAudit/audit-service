/*
 * Copyright 2015-2020 Micro Focus or one of its affiliates.
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

import java.util.Arrays;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonkeyConfig
{
    private static final Logger LOG = LoggerFactory.getLogger(MonkeyConfig.class);

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
      
        readMonkeyProps();
        
        readAuditEventProps();
        
        LOG.info(this.toString());
    }

    private void readMonkeyProps()
    {
        /*
         * Monkey
         */
        monkeyMode = System.getProperty(MonkeyConstants.CAF_AUDIT_MONKEY_MODE, System.getenv(MonkeyConstants.CAF_AUDIT_MONKEY_MODE));
        if(null == monkeyMode || monkeyMode.isEmpty()) {
            monkeyMode = MonkeyConstants.CAF_AUDIT_STANDARD_MONKEY;
            LOG.info("No [" + MonkeyConstants.CAF_AUDIT_MONKEY_MODE + "] supplied defaulting to [" + monkeyMode + "]");
        } else if (!isMonkeyMode(monkeyMode))  {
            String errorMsg = "The " + MonkeyConstants.CAF_AUDIT_MODE + " supplied [" + monkeyMode + "] does not match the available modes [" + Arrays.toString(MonkeyConstants.ARRAY_OF_MONKEYS) + "]"; 
            LOG.error(errorMsg);
            throw new RuntimeException(errorMsg);
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
    }

    private void readAuditEventProps()
    {
        /*
         * Audit Events
         */
        if (monkeyMode.equals(MonkeyConstants.CAF_AUDIT_DEMO_MONKEY)) {
            tenantId = "Operating in Demo Mode";
            userId = "Operating in Demo Mode";
            LOG.info("Audit Monkey runnning in [" + MonkeyConstants.CAF_AUDIT_DEMO_MONKEY + "] mode, therefore ignoring any supplied ["
                    + MonkeyConstants.CAF_AUDIT_TENANT_ID + "] or [" + MonkeyConstants.CAF_AUDIT_USER_ID
                    + "] as the Audit Monkey generates these itself in [" + MonkeyConstants.CAF_AUDIT_DEMO_MONKEY + "] mode");
        } else {
            tenantId = System.getProperty(MonkeyConstants.CAF_AUDIT_TENANT_ID, System.getenv(MonkeyConstants.CAF_AUDIT_TENANT_ID));
            if(null == tenantId || tenantId.isEmpty()) {
                tenantId = "acmecorp";
                LOG.info("No [" + MonkeyConstants.CAF_AUDIT_TENANT_ID + "] supplied defaulting to [" + tenantId + "]");
            }

            userId = System.getProperty(MonkeyConstants.CAF_AUDIT_USER_ID, System.getenv(MonkeyConstants.CAF_AUDIT_USER_ID));
            if(null == userId || userId.isEmpty()) {
                userId = "road.runner@acme.com";
                LOG.info("No [" + MonkeyConstants.CAF_AUDIT_USER_ID + "] supplied defaulting to [" + userId + "]");
            }
        }
        
        correlationId = System.getProperty(MonkeyConstants.CAF_AUDIT_CORRELATION_ID, System.getenv(MonkeyConstants.CAF_AUDIT_CORRELATION_ID));
        if(null == correlationId || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            LOG.info("No [" + MonkeyConstants.CAF_AUDIT_CORRELATION_ID + "] supplied defaulting to [" + correlationId + "]");
        }
        
    }
    
    private boolean isMonkeyMode(String auditMode) {
        boolean bool = false;
        for(String mode : MonkeyConstants.ARRAY_OF_MONKEYS) {
            if(mode.equalsIgnoreCase(auditMode)) {
                bool = true;
                break;
            }
        }
        return bool;
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
        builder.append("MonkeyConfig [auditMode=").append(tenantId).append(", correlationId=").append(correlationId).append(", userId=")
            .append(userId).append(", monkeyMode=").append(monkeyMode).append(", numOfEvents=").append(numOfEvents)
            .append(", numOfThreads=").append(numOfThreads).append("]");
        return builder.toString();
    }
    
}
