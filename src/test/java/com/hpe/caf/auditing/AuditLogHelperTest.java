package com.hpe.caf.auditing;

import com.hpe.caf.worker.audit.AuditWorkerTask;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class AuditLogHelperTest {

    @Test
    public void testCreateAuditWorkerTask() {

        AuditWorkerTask awk = AuditLogHelper.createAuditWorkerTask();
        Assert.assertNotNull(awk.getProcessId());
        Assert.assertNotNull(awk.getThreadId());
        Assert.assertNotNull(awk.getEventOrder());
        Assert.assertNotNull(awk.getEventTime());
        Assert.assertNotNull(awk.getEventTimeSource());
    }

    @Test
    public void testSendAuditWorkerTask() throws IOException {

        AuditChannel mockChannel = Mockito.mock(AuditChannel.class);
        AuditWorkerTask awk = AuditLogHelper.createAuditWorkerTask();
        AuditLogHelper.sendAuditWorkerTask(mockChannel,"testQueue",awk);
        Mockito.verify(mockChannel, Mockito.times(1)).publish(Mockito.anyString(),Mockito.any(byte[].class));
    }

}
