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

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cafaudit.AuditLog;
import com.hpe.caf.auditing.AuditChannel;

public class StandardMonkey implements Monkey, Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger(StandardMonkey.class);
    
    private AuditChannel channel;
    private MonkeyConfig monkeyConfig;
    private BlockingQueue<Integer> queue;
        
    public StandardMonkey(AuditChannel channel, MonkeyConfig monkeyConfig)
    {
        this.channel = channel;
        this.monkeyConfig = monkeyConfig;
        this.queue = MonkeyFactory.populateQueue(monkeyConfig);
    }
    
    public void execute(AuditChannel channel, MonkeyConfig monkeyConfig) throws Exception
    {
        while(queue.size() > 0){
            int i = queue.take();
            LOG.debug("Sending Audit Event [" + i + "]");
            AuditLog.auditViewDocument(channel, monkeyConfig.getTenantId(), monkeyConfig.getUserId(), monkeyConfig.getCorrelationId(), i);
        }
    }
    
    @Override
    public void run()
    {
        try {
            execute(channel, monkeyConfig);
        } catch (Exception e) {
            LOG.error("Error executing a multi-threaded version of the StandardMonkey" + e);
        }
    }

}
