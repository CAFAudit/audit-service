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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hpe.caf.auditing.AuditChannel;

public class MonkeyFactoryTest
{
    private AuditChannel channel;
    private MonkeyConfig monkeyConfig;
    
    @Before
    public void setUp() {
        System.setProperty(MonkeyConstants.CAF_AUDIT_MODE, MonkeyConstants.ELASTICSEARCH);
        monkeyConfig = new MonkeyConfig();
    }
    
    @After
    public void tearDown()
    {
        System.clearProperty(MonkeyConstants.CAF_AUDIT_MODE);
    }
    
    @Test
    public void shouldReturnStandardMonkey() {
        monkeyConfig.setMonkeyMode(MonkeyConstants.CAF_AUDIT_STANDARD_MONKEY);
        Monkey monkey = MonkeyFactory.selectMonkey(channel, monkeyConfig);
        assertTrue(monkey instanceof StandardMonkey);
    }
    
    @Test
    public void shouldReturnRandomMonkey() {
        monkeyConfig.setMonkeyMode(MonkeyConstants.CAF_AUDIT_RANDOM_MONKEY);
        Monkey monkey = MonkeyFactory.selectMonkey(channel, monkeyConfig);
        assertTrue(monkey instanceof RandomMonkey);
    }
    
    @Test
    public void shouldReturnNull() {
        monkeyConfig.setMonkeyMode("blah");
        Monkey monkey = MonkeyFactory.selectMonkey(channel, monkeyConfig);
        assertNull(monkey);
    }    
}
