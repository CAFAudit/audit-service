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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditEventBuilder;

public class DemoMonkeyTest
{
    private static final Logger LOG = LoggerFactory.getLogger(DemoMonkeyTest.class);
    
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
        Monkey sm = new DemoMonkey(channel, monkeyConfig);
        when(channel.createEventBuilder()).thenReturn(auditEventBuilder);
        sm.execute(channel, monkeyConfig);
        verify(auditEventBuilder, times(2)).send();
    }
    
    @Test
    public void shouldReturnRandomString() {
        when(monkeyConfig.getNumOfEvents()).thenReturn(2);
        DemoMonkey dm = new DemoMonkey(channel, monkeyConfig);
        String[] array = new String[]{"string1", "string2", "string3"};
        String str = (String) dm.selectRandom(array);
        LOG.debug("Random String is [" + str + "]");
        assertNotEquals(-1, Arrays.binarySearch(array, str));
    }
    
    @Test
    public void shouldReturnRandomDate() {
        when(monkeyConfig.getNumOfEvents()).thenReturn(2);
        DemoMonkey dm = new DemoMonkey(channel, monkeyConfig);
        Date date = dm.randomDate();
        LOG.info("Random Date is [" + date + "]");
        LocalDate upper = LocalDate.now().plusDays(1);
        LocalDate lower = LocalDate.now().minusMonths(1).minusDays(1);
        assertTrue(date.before(upper.toDate()));
        assertTrue(date.after(lower.toDate()));
    }
}
