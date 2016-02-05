package com.hpe.caf.services.audit.api;

/**
 * Custom exception implemented for the audit management api.
 */
public class ApiException extends Exception {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
