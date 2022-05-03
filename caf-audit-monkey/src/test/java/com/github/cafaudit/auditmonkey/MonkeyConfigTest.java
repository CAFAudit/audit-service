/*
 * Copyright 2015-2022 Micro Focus or one of its affiliates.
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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MonkeyConfigTest
{
    @Before
    public void setUp()
    {
        System.setProperty(MonkeyConstants.CAF_AUDIT_MODE, MonkeyConstants.ELASTICSEARCH);
    }
    
    @After
    public void tearDown()
    {
        System.clearProperty(MonkeyConstants.CAF_AUDIT_MODE);
    }
    
    @Test
    public void shouldEnsureDefaultValuesAreSet() {
        MonkeyConfig monkeyConfig = new MonkeyConfig();
        assertEquals(MonkeyConstants.CAF_AUDIT_STANDARD_MONKEY, monkeyConfig.getMonkeyMode());
        assertEquals(1, monkeyConfig.getNumOfEvents());
        assertEquals("acmecorp", monkeyConfig.getTenantId());
        assertEquals("road.runner@acme.com", monkeyConfig.getUserId());
    }
}
