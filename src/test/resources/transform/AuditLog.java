
package com.hpe.caf.auditing.plugins.unittest;

import com.hpe.caf.auditing.AuditLogHelper;
import com.hpe.caf.worker.audit.AuditWorkerTask;
import com.hpe.caf.auditing.AuditChannel;

import java.io.IOException;
import java.util.Date;

/**
 * Auto-generated class for writing ProductX events to the audit log
 */
public final class AuditLog
{
    private static final String APPLICATION_IDENTIFIER = "ProductX";
    private static final String QUEUE_NAME = "AuditEventQueue." + APPLICATION_IDENTIFIER;

    private AuditLog() {
    }

    /**
     * Checks that the AuditLog queue exists and creates it if it doesn't.
     * This function should be called before any of the audit... functions are called.
     */
    public static void ensureQueueExists(final AuditChannel channel)
			throws IOException
    {
        channel.declareQueue(QUEUE_NAME);
    }

            																	            																																																													            																																																													            																																																													            																																																													            																																																													            																																																															
    /**
     * Audit the viewDocument event
	 * @param channel Identifies the channel to be used for message queuing 
	 * @param userId Identifies the user who triggered the event 
	 * @param param1 Description for param1 
	 * @param param2 Description for param2 
	 * @param param3 Description for param3 
	 * @param param4 Description for param4 
	 * @param param5 Description for param5 
	 * @param param6 Description for param6 		 
     */
    public static void auditViewDocument
	(
		final AuditChannel channel,
		final String userId,
		final String param1,
		final short param2,
		final int param3,
		final long param4,
		final float param5,
		final Date param6
	)
     throws IOException
    {
        final AuditWorkerTask auditWorkerTask = AuditLogHelper.createAuditWorkerTask();
        auditWorkerTask.setApplicationId(APPLICATION_IDENTIFIER);
        auditWorkerTask.setUserId(userId);
        auditWorkerTask.setEventTypeId("viewDocument");
        auditWorkerTask.setEventParams(new String[] {param1,String.valueOf(param2),String.valueOf(param3),String.valueOf(param4),String.valueOf(param5),String.valueOf(param6)});

        AuditLogHelper.sendAuditWorkerTask(channel, QUEUE_NAME, auditWorkerTask);
    }
	
}
