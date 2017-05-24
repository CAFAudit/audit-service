package com.github.cafaudit.auditmonkey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cafaudit.AuditLog;
import com.hpe.caf.auditing.AuditChannel;

public class StandardMonkey implements Monkey
{

    private static final Logger LOG = LoggerFactory.getLogger(StandardMonkey.class);
    
    public void execute(AuditChannel channel, AuditMonkeyConfig monkeyConfig) throws Exception
    {
        int numberOfEvents = monkeyConfig.getNumOfEvents();
        int i = 0;
        while (i < numberOfEvents) {
            LOG.trace("Sending Audit Event [" + i + "]");
            AuditLog.auditViewDocument(channel, monkeyConfig.getTenantId(), monkeyConfig.getUserId(), monkeyConfig.getCorrelationId(), i);
            i++;
        }
    }

}
