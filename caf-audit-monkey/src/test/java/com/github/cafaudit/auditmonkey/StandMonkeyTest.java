package com.github.cafaudit.auditmonkey;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.hpe.caf.auditing.AuditChannel;

public class StandMonkeyTest
{

    private AuditChannel channel;
    private AuditMonkeyConfig monkeyConfig;
    
    @Before
    public void setUp() {
        
        channel = mock(AuditChannel.class);
        monkeyConfig = mock(AuditMonkeyConfig.class);
    }
    
    @Test
    public void shouldSendAuditEvent() throws Exception {
        Monkey sm = new StandardMonkey();
        sm.execute(channel, monkeyConfig);
        
        // TODO (Greg) Start Here on Thursday Morning
        
    }
}
