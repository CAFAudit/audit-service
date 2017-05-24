package com.github.cafaudit.auditmonkey;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AuditMonkeyConfigTest
{

    @Test
    public void shouldEnsureDefaultValuesAreSet() {
        AuditMonkeyConfig auditMonkeyConfig = new AuditMonkeyConfig();
        assertEquals("elasticsearch-cluster", auditMonkeyConfig.getEsClustername());
        assertEquals("192.168.56.10", auditMonkeyConfig.getEsHostname());
        assertEquals("192.168.56.10:9300", auditMonkeyConfig.getEsHostnameAndPort());
        assertEquals(9300, auditMonkeyConfig.getEsPort());
        assertEquals(AuditMonkeyConstants.STANDARD_MONKEY, auditMonkeyConfig.getMonkeyMode());
        assertEquals(1, auditMonkeyConfig.getNumOfEvents());
        assertEquals("acmecorp", auditMonkeyConfig.getTenantId());
        assertEquals("road.runner@acme.com", auditMonkeyConfig.getUserId());
        assertEquals("192.168.56.10", auditMonkeyConfig.getWsHostname());
        assertEquals("192.168.56.10:25080", auditMonkeyConfig.getWsHostnameAndPort());
        assertEquals(25080, auditMonkeyConfig.getWsPort());
    }
}
