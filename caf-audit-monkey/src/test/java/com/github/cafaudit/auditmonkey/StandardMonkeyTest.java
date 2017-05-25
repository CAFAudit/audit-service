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
    private AuditMonkeyConfig monkeyConfig;
    private AuditEventBuilder auditEventBuilder;
    
    @Before
    public void setUp() {
        
        channel = mock(AuditChannel.class);
        monkeyConfig = mock(AuditMonkeyConfig.class);
        auditEventBuilder = mock(AuditEventBuilder.class);
    }
    
    @Test
    public void shouldSendTwoAuditEvents() throws Exception {
        Monkey sm = new StandardMonkey();
        when(monkeyConfig.getNumOfEvents()).thenReturn(2);
        when(channel.createEventBuilder()).thenReturn(auditEventBuilder);
        sm.execute(channel, monkeyConfig);
        verify(auditEventBuilder, times(2)).send();
    }

    @Test
    public void shouldSendSeventeenAuditEvents() throws Exception {
        Monkey sm = new StandardMonkey();
        when(monkeyConfig.getNumOfEvents()).thenReturn(17);
        when(channel.createEventBuilder()).thenReturn(auditEventBuilder);
        sm.execute(channel, monkeyConfig);
        verify(auditEventBuilder, times(17)).send();
    }
}
