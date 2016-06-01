package com.hpe.caf.auditing;

/**
 * An unchecked exception to throw if audit event string validation fails.
 */
public final class AuditValidatorException extends RuntimeException {
    public AuditValidatorException(final String message)
    {
        super(message);
    }

    public AuditValidatorException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
