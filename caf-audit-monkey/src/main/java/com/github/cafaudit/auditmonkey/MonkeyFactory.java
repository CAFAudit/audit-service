package com.github.cafaudit.auditmonkey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonkeyFactory
{

    private static final Logger LOG = LoggerFactory.getLogger(MonkeyFactory.class);
    
    public static Monkey selectMonkey(String mode) {
        LOG.debug("Selecting type of Monkey to from the MonkeyFactory");
        Monkey monkey = null;
        if(mode.equalsIgnoreCase(AuditMonkeyConstants.STANDARD_MONKEY)) {
            LOG.info("Standard Monkey selected");
            monkey = new StandardMonkey();
        } 
        else if(mode.equalsIgnoreCase(AuditMonkeyConstants.RANDOM_MONKEY)) {
            LOG.info("Random Monkey selected");
            monkey = new RandomMonkey();
        }
        return monkey;
    }
    
}
