package com.github.cafaudit.auditmonkey;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.hpe.caf.auditing.AuditChannel;

public class AuditMonkeyTest
{
    private AuditChannel channel;
    private StandardMonkey standardMonkey;
    
    @Before
    public void setUp() {
        
        channel = mock(AuditChannel.class);
        standardMonkey = mock(StandardMonkey.class);
        
    }

    @Test
    public void shouldHaveMultiThreadsAreCreated() throws Exception {
        AuditMonkey.runTheMonkey(channel, standardMonkey, 10);
        verify(standardMonkey, times(10)).run();
    }
    
}
