/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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
package com.hpe.caf.auditing;

import java.util.Date;

public interface AuditEventBuilder
{
    void setApplication(String applicationId);

    void setUser(String userId);

    void setTenant(String tenantId);

    void setCorrelationId(String correlationId);

    void setEventType(
        String eventCategoryId,
        String eventTypeId
    );

    void addEventParameter(
        String name,
        String columnName,
        String value
    );

    void addEventParameter(
        String name,
        String columnName,
        String value,
        AuditIndexingHint indexingHint
    );

    default void addEventParameter(
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

    default void addEventParameter(
        String name,
        String columnName,
        String value,
        AuditIndexingHint indexingHint,
        int minLength,
        int maxLength
    )
    {
        AuditValidator.validateString(name, value, minLength, maxLength);
        addEventParameter(name, columnName, value, indexingHint);
    }

    default void addEventParameter(
        String name,
        String columnName,
        short value
    )
    {
        addEventParameter(name, columnName, Short.toString(value));
    }

    default void addEventParameter(
        String name,
        String columnName,
        int value
    )
    {
        addEventParameter(name, columnName, Integer.toString(value));
    }

    default void addEventParameter(
        String name,
        String columnName,
        long value
    )
    {
        addEventParameter(name, columnName, Long.toString(value));
    }

    default void addEventParameter(
        String name,
        String columnName,
        float value
    )
    {
        addEventParameter(name, columnName, Float.toString(value));
    }

    default void addEventParameter(
        String name,
        String columnName,
        double value
    )
    {
        addEventParameter(name, columnName, Double.toString(value));
    }

    default void addEventParameter(
        String name,
        String columnName,
        boolean value
    )
    {
        addEventParameter(name, columnName, Boolean.toString(value));
    }

    default void addEventParameter(
        String name,
        String columnName,
        Date value
    )
    {
        addEventParameter(name, columnName, value.toInstant().toString());
    }

    void send() throws Exception;

}
