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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AuditMonkeyConfigTest
{

    @Test
    public void shouldEnsureDefaultValuesAreSet() {
        MonkeyConfig auditMonkeyConfig = new MonkeyConfig();
        assertEquals("elasticsearch-cluster", auditMonkeyConfig.getEsClustername());
        assertEquals("192.168.56.10", auditMonkeyConfig.getEsHostname());
        assertEquals("192.168.56.10:9300", auditMonkeyConfig.getEsHostnameAndPort());
        assertEquals(9300, auditMonkeyConfig.getEsPort());
        assertEquals(MonkeyConstants.CAF_AUDIT_STANDARD_MONKEY, auditMonkeyConfig.getMonkeyMode());
        assertEquals(1, auditMonkeyConfig.getNumOfEvents());
        assertEquals("acmecorp", auditMonkeyConfig.getTenantId());
        assertEquals("road.runner@acme.com", auditMonkeyConfig.getUserId());
        assertEquals("192.168.56.10", auditMonkeyConfig.getWsHostname());
        assertEquals("192.168.56.10:25080", auditMonkeyConfig.getWsHostnameAndPort());
        assertEquals(25080, auditMonkeyConfig.getWsPort());
    }
}
