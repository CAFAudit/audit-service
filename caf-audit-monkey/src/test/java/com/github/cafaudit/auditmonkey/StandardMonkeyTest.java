/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditEventBuilder;

public class StandardMonkeyTest
{

    private AuditChannel channel;
    private MonkeyConfig monkeyConfig;
    private AuditEventBuilder auditEventBuilder;
    
    @Before
    public void setUp() {
        
        channel = mock(AuditChannel.class);
        monkeyConfig = mock(MonkeyConfig.class);
        auditEventBuilder = mock(AuditEventBuilder.class);
    }
    
    @Test
    public void shouldSendTwoAuditEvents() throws Exception {
        when(monkeyConfig.getNumOfEvents()).thenReturn(2);
        Monkey sm = new StandardMonkey(channel, monkeyConfig);
        when(channel.createEventBuilder()).thenReturn(auditEventBuilder);
        sm.execute(channel, monkeyConfig);
        verify(auditEventBuilder, times(2)).send();
    }

    @Test
    public void shouldSendSeventeenAuditEvents() throws Exception {
        when(monkeyConfig.getNumOfEvents()).thenReturn(17);
        Monkey sm = new StandardMonkey(channel, monkeyConfig);
        when(channel.createEventBuilder()).thenReturn(auditEventBuilder);
        sm.execute(channel, monkeyConfig);
        verify(auditEventBuilder, times(17)).send();
    }
    
    @Test
    public void shouldSendTwoHundredAuditEventsMultiThreaded() throws Exception {
        when(monkeyConfig.getNumOfEvents()).thenReturn(200);
        when(monkeyConfig.getNumOfThreads()).thenReturn(3);
        Monkey sm = new StandardMonkey(channel, monkeyConfig);
        when(channel.createEventBuilder()).thenReturn(auditEventBuilder);
        sm.execute(channel, monkeyConfig);
        verify(auditEventBuilder, times(200)).send();
    }
}
