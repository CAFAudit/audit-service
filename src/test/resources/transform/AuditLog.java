
package com.hpe.caf.auditing.plugins.unittest;

import com.hpe.caf.auditing.AuditLogHelper;
import com.hpe.caf.worker.audit.AuditWorkerTask;
import com.hpe.caf.auditing.AuditChannel;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
	 * @param String_Param Description for String_Param 
	 * @param Int16_Param Description for Int16_Param 
	 * @param Int32_Param Description for Int32_Param 
	 * @param Int64_Param Description for Int64_Param 
	 * @param Float_Param Description for Float_Param 
	 * @param Double_Param Description for Double_Param 
	 * @param Boolean_Param Description for Boolean_Param 
	 * @param Date_Param Description for Date_Param 		 
     */
    public static void auditViewDocument
	(
		final AuditChannel channel,
		final String userId,
		final String String_Param,
		final short Int16_Param,
		final int Int32_Param,
		final long Int64_Param,
		final float Float_Param,
		final double Double_Param,
		final boolean Boolean_Param,
		final Date Date_Param
	)
     throws IOException
    {
        final AuditWorkerTask auditWorkerTask = AuditLogHelper.createAuditWorkerTask();
        auditWorkerTask.setApplicationId(APPLICATION_IDENTIFIER);
        auditWorkerTask.setUserId(userId);
        auditWorkerTask.setEventTypeId("viewDocument");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        auditWorkerTask.setEventParams(new String[] {String_Param,String.valueOf(Int16_Param),String.valueOf(Int32_Param),String.valueOf(Int64_Param),String.valueOf(Float_Param),String.valueOf(Double_Param),String.valueOf(Boolean_Param),df.format(Date_Param)});

        AuditLogHelper.sendAuditWorkerTask(channel, QUEUE_NAME, auditWorkerTask);
    }
	
}
