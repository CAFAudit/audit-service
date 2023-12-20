/*
 * Copyright 2015-2024 Open Text.
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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpe.caf.auditing.AuditChannel;

public class MonkeyFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(MonkeyFactory.class);
    
    public static Monkey selectMonkey(AuditChannel channel, MonkeyConfig monkeyConfig) {
        LOG.debug("Selecting type of Monkey to from the MonkeyFactory");
        Monkey monkey = null;
        if(monkeyConfig.getMonkeyMode().equalsIgnoreCase(MonkeyConstants.CAF_AUDIT_STANDARD_MONKEY)) {
            LOG.info("Standard Monkey selected");
            monkey = new StandardMonkey(channel, monkeyConfig);
        } 
        else if(monkeyConfig.getMonkeyMode().equalsIgnoreCase(MonkeyConstants.CAF_AUDIT_RANDOM_MONKEY)) {
            LOG.info("Random Monkey selected");
            monkey = new RandomMonkey(channel, monkeyConfig);
        } else if(monkeyConfig.getMonkeyMode().equalsIgnoreCase(MonkeyConstants.CAF_AUDIT_DEMO_MONKEY)) {
            LOG.info("Demo Monkey selected");
            monkey = new DemoMonkey(channel, monkeyConfig);
        }
        return monkey;
    }
    
    public static BlockingQueue<Integer> populateQueue(MonkeyConfig monkeyConfig)
    {
        int num = monkeyConfig.getNumOfEvents();
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(num);
        for(int i = 1; i <= num; i++) {
            try {
                queue.put(new Integer(i));
            } catch (InterruptedException ie) {
                LOG.error("Error populating work queue for StandardMonkey", ie);
            }
        }
        return queue;
    }
}
