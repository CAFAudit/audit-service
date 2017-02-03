package com.hpe.caf.auditing;

import java.text.MessageFormat;

final class AuditValidator {

    /**
     * Validates the field length and throws an appropriate exception if min or max constraints are exceeded.
     */
    public static void validateString(String fieldName, String fieldValue, int minLength, int maxLength)
    {
        Integer fieldLength = fieldValue.length();

        if (minLength != -1) {
            if (fieldLength < minLength) {
                throw new AuditValidatorException(MessageFormat.format("Field name {0} is too short, minimum is {1} characters.", fieldName, minLength));
            }
        }

        if (maxLength != -1) {
            if (fieldLength > maxLength) {
                throw new AuditValidatorException(MessageFormat.format("Field name {0} is too long, maximum is {1} characters.", fieldName, maxLength));
            }
        }
    }

}
