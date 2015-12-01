package com.hpe.caf.auditing;

public interface AuditChannel extends AutoCloseable
{
    /**
     * Creates an object which can be used for preparing and sending an audit
     * event.
     *
     * @return an audit event builder
     */
    AuditEventBuilder createEventBuilder();
}
