package com.hpe.caf.auditing;

import java.util.Date;

public interface AuditEventBuilder
{
    void setApplication(String applicationId);

    void setUser(String userId);

    void setTenant(String tenantId);

    void setCorrelationId(String correlationId);

    void setEventType
    (
        String eventCategoryId,
        String eventTypeId
    );

    void addEventParameter
    (
        String name,
        String columnName,
        String value
    );

    default void addEventParameter
    (
        String name,
        String columnName,
        String value,
        int minLength,
        int maxLength
    )
    {
        AuditValidator.validateString(name, value, minLength, maxLength);
        addEventParameter(name, columnName, value);
    }

    default void addEventParameter
    (
        String name,
        String columnName,
        short value
    )
    {
        addEventParameter(name, columnName, Short.toString(value));
    }

    default void addEventParameter
    (
        String name,
        String columnName,
        int value
    )
    {
        addEventParameter(name, columnName, Integer.toString(value));
    }

    default void addEventParameter
    (
        String name,
        String columnName,
        long value
    )
    {
        addEventParameter(name, columnName, Long.toString(value));
    }

    default void addEventParameter
    (
        String name,
        String columnName,
        float value
    )
    {
        addEventParameter(name, columnName, Float.toString(value));
    }

    default void addEventParameter
    (
        String name,
        String columnName,
        double value
    )
    {
        addEventParameter(name, columnName, Double.toString(value));
    }

    default void addEventParameter
    (
        String name,
        String columnName,
        boolean value
    )
    {
        addEventParameter(name, columnName, Boolean.toString(value));
    }

    default void addEventParameter
    (
        String name,
        String columnName,
        Date value
    )
    {
        addEventParameter(name, columnName, value.toInstant().toString());
    }

    void send() throws Exception;

}
