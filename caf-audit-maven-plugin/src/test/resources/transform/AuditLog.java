
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

package com.hpe.caf.auditing.plugins.unittest;

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.AuditIndexingHint;

import java.util.Date;

/**
 * Auto-generated class for writing ProductX events to the audit log
 */
public final class AuditLog
{
    private static final String APPLICATION_IDENTIFIER = "ProductX";

    private AuditLog() {
    }

    /**
     * Checks that the AuditLog queue exists and creates it if it doesn't.
     * This function should be called before any of the audit... functions are called.
     */
    public static void declareApplication(final AuditChannel channel)
        throws Exception
    {
        channel.declareApplication(APPLICATION_IDENTIFIER);
    }

                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
    /**
     * Audit the viewDocument event
     * @param channel Identifies the channel to be used for message queuing 
     * @param tenantId Identifies the tenant that the user belongs to 
     * @param userId Identifies the user who triggered the event 
     * @param correlationId Identifies the same user action 
     * @param String_Param Description for String_Param 
     * @param String_Fulltext_Param Description for String_Fulltext_Param 
     * @param String_Keyword_Param Description for String_Keyword_Param 
     * @param String_With_Constraints_Param Description for String_With_Constraints_Param 
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
        final String tenantId,
        final String userId,
        final String correlationId,
        final String String_Param,
        final String String_Fulltext_Param,
        final String String_Keyword_Param,
        final String String_With_Constraints_Param,
        final short Int16_Param,
        final int Int32_Param,
        final long Int64_Param,
        final float Float_Param,
        final double Double_Param,
        final boolean Boolean_Param,
        final Date Date_Param
    )
        throws Exception
    {
        final AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
        auditEventBuilder.setApplication(APPLICATION_IDENTIFIER);
        auditEventBuilder.setTenant(tenantId);
        auditEventBuilder.setUser(userId);
        auditEventBuilder.setCorrelationId(correlationId);
        auditEventBuilder.setEventType("documentEvents", "viewDocument");
        auditEventBuilder.addEventParameter("String_Param", null, String_Param);
        auditEventBuilder.addEventParameter("String_Fulltext_Param", null, String_Fulltext_Param, AuditIndexingHint.FULLTEXT);
        auditEventBuilder.addEventParameter("String_Keyword_Param", null, String_Keyword_Param, AuditIndexingHint.KEYWORD);
        auditEventBuilder.addEventParameter("String_With_Constraints_Param", null, String_With_Constraints_Param, 1, 32);
        auditEventBuilder.addEventParameter("Int16_Param", null, Int16_Param);
        auditEventBuilder.addEventParameter("Int32_Param", null, Int32_Param);
        auditEventBuilder.addEventParameter("Int64_Param", null, Int64_Param);
        auditEventBuilder.addEventParameter("Float_Param", null, Float_Param);
        auditEventBuilder.addEventParameter("Double_Param", null, Double_Param);
        auditEventBuilder.addEventParameter("Boolean_Param", null, Boolean_Param);
        auditEventBuilder.addEventParameter("Date_Param", null, Date_Param);

        auditEventBuilder.send();
    }
}
