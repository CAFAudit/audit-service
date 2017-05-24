package com.github.cafaudit.auditmonkey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cafaudit.AuditLog;
import com.hpe.caf.auditing.AuditChannel;

public class RandomMonkey implements Monkey
{
    
    private static final Logger LOG = LoggerFactory.getLogger(RandomMonkey.class);
    
    public void execute(AuditChannel channel, AuditMonkeyConfig monkeyConfig) throws Exception
    {
        int numberOfEvents = monkeyConfig.getNumOfEvents();
        int i = 0;
        while (i < numberOfEvents) {
            double random = Math.random() * 100;
            int num = (int)random;
            int j = 0;
            while (i < numberOfEvents && j < num) {
                LOG.trace("Sending Audit Event [" + i + "][" + j + "]");
                AuditLog.auditPolicyApplied(channel, monkeyConfig.getTenantId(), monkeyConfig.getUserId(),
                        monkeyConfig.getCorrelationId(), i, Integer.toString(j), "[" + i + "][" + j + "]");
                j++;
                i++;
            }
            Thread.sleep(num * 100);
        }
    }
    
}
