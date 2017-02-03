package com.hpe.caf.auditing;

import java.io.IOException;

public interface AuditChannel extends AutoCloseable
{
    /**
     * Prepares the auditing infrastructure to receive events for the specified
     * application.
     *
     * @param applicationId the identifier of the application
     * @throws java.io.IOException if an error is encountered
     */
    void declareApplication(String applicationId) throws IOException;

    /**
     * Creates an object which can be used for preparing and sending an audit
     * event.
     *
     * @return an audit event builder
     */
    AuditEventBuilder createEventBuilder();
}
