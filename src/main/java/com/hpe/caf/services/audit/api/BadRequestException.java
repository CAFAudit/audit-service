package com.hpe.caf.services.audit.api;

/**
 * Custom exception implemented for the audit management api. Exceptions of this type map
 * directly onto http 400 status codes.
 */
public class BadRequestException extends Exception {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
