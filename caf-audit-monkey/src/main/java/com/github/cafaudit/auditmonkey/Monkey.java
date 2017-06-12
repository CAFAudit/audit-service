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

import com.hpe.caf.auditing.AuditChannel;

public interface Monkey
{
    /**
     * Uses the supplied channel and configuration to send audit events directly to Elasticsearch or to the Audit Web Service.
     * @param channel Channel to send audit events down
     * @param monkeyConfig Configuration details
     * @throws Exception Thrown during the sending of audit events
     */
    public void execute(AuditChannel channel, MonkeyConfig monkeyConfig) throws Exception;
    
    /**
     * Set the channel for auditing
     * @param channel for the audit events to be sent
     */
    public void setChannel(AuditChannel channel);
    
    /**
     * Set the config for the instance of the Audit Monkey
     * @param monkeyConfig configuration for the instance of the Monkey
     */
    public void setMonkeyConfig(MonkeyConfig monkeyConfig);
    
}
