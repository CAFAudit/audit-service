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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cafaudit.AuditLog;
import com.hpe.caf.auditing.AuditChannel;

public class RandomMonkey implements Monkey, Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger(RandomMonkey.class);

    private AuditChannel channel;
    private MonkeyConfig monkeyConfig;
    private BlockingQueue<Integer> queue;    
    
    public RandomMonkey(AuditChannel channel, MonkeyConfig monkeyConfig)
    {
        this.channel = channel;
        this.monkeyConfig = monkeyConfig;
        populateQueue();
    }
    
    public void execute(AuditChannel channel, MonkeyConfig monkeyConfig) throws Exception
    {       
        while(queue.size() > 0){
            double random = Math.random() * 100;
            int num = (int)random;
            int j = 1;
            while (queue.size() > 0 && j <= num) {
                int i = queue.take();
                LOG.trace("Sending Audit Event [" + i + "][" + j + "]");
                AuditLog.auditPolicyApplied(channel, monkeyConfig.getTenantId(), monkeyConfig.getUserId(),
                        monkeyConfig.getCorrelationId(), i, Integer.toString(j), "[" + i + "][" + j + "]");
                j++;
            }
            if (queue.size() > 0) {
                Thread.sleep(num * 250);
            }
        }
    }

    @Override
    public void run()
    {
        try {
            execute(channel, monkeyConfig);
        } catch (Exception e) {
            LOG.error("Error executing a multi-threaded version of the RandomMonkey" + e);
        }
    }

    private void populateQueue()
    {
        int num = monkeyConfig.getNumOfEvents();
        queue = new ArrayBlockingQueue<>(num);
        for(int i = 1; i <= num; i++) {
            try {
                queue.put(new Integer(i));
            } catch (InterruptedException ie) {
                LOG.error("Error populating work queue for StandardMonkey", ie);
            }
        }
    }
    
    /**
     * @return the channel
     */
    public AuditChannel getChannel()
    {
        return channel;
    }

    /**
     * @param channel the channel to set
     */
    public void setChannel(AuditChannel channel)
    {
        this.channel = channel;
    }

    /**
     * @return the monkeyConfig
     */
    public MonkeyConfig getMonkeyConfig()
    {
        return monkeyConfig;
    }

    /**
     * @param monkeyConfig the monkeyConfig to set
     */
    public void setMonkeyConfig(MonkeyConfig monkeyConfig)
    {
        this.monkeyConfig = monkeyConfig;
    }

}
