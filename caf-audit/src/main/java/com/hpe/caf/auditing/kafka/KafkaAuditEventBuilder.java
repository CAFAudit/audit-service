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
package com.hpe.caf.auditing.kafka;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.AuditLogHelper;
import org.apache.kafka.clients.producer.*;

final class KafkaAuditEventBuilder implements AuditEventBuilder
{
    private static final String processId = AuditLogHelper.getProcessId().toString();
    private static final int processIdHashCode = AuditLogHelper.getProcessId().hashCode();
    private static final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;

    private final Producer<String, String> producer;
    private final ObjectNode jsonAuditMessage;
    private String applicationId;
    private String tenantId;

    public KafkaAuditEventBuilder(final Producer<String, String> producer)
    {
        this.producer = producer;
        this.jsonAuditMessage = createJsonAuditMessage();
    }

    private static ObjectNode createJsonAuditMessage()
    {
        final ObjectNode jsonAuditMessage = jsonFactory.objectNode();
        jsonAuditMessage.put("processId", processId);
        jsonAuditMessage.put("threadId", AuditLogHelper.getThreadId());
        jsonAuditMessage.put("eventOrder", AuditLogHelper.getNextEventId());
        jsonAuditMessage.put("eventTime", AuditLogHelper.getCurrentTime().toString());
        jsonAuditMessage.put("eventTimeSource", AuditLogHelper.getCurrentTimeSource());

        return jsonAuditMessage;
    }

    @Override
    public void setApplication(final String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public void setUser(final String userId) {
        jsonAuditMessage.put("userId", userId);
    }

    @Override
    public void setTenant(final String tenantId) {
        this.tenantId = tenantId;
        jsonAuditMessage.put("tenantId", tenantId);
    }

    @Override
    public void setCorrelationId(final String correlationId) {
        jsonAuditMessage.put("correlationId", correlationId);
    }

    @Override
    public void setEventType
    (
        final String eventCategoryId,
        final String eventTypeId
    )
    {
        jsonAuditMessage.put("eventCategoryId", eventCategoryId);
        jsonAuditMessage.put("eventTypeId", eventTypeId);
    }

    @Override
    public void addEventParameter
    (
        final String name,
        final String columnName,
        final String value
    )
    {
        jsonAuditMessage.put(getEventParamName(name, columnName), value);
    }

    @Override
    public void addEventParameter
    (
        final String name,
        final String columnName,
        final short value
    )
    {
        jsonAuditMessage.put(getEventParamName(name, columnName), value);
    }

    @Override
    public void addEventParameter
    (
        final String name,
        final String columnName,
        final int value
    )
    {
        jsonAuditMessage.put(getEventParamName(name, columnName), value);
    }

    @Override
    public void addEventParameter
    (
        final String name,
        final String columnName,
        final long value
    )
    {
        jsonAuditMessage.put(getEventParamName(name, columnName), value);
    }

    @Override
    public void addEventParameter
    (
        final String name,
        final String columnName,
        final float value
    )
    {
        jsonAuditMessage.put(getEventParamName(name, columnName), value);
    }

    @Override
    public void addEventParameter
    (
        final String name,
        final String columnName,
        final double value
    )
    {
        jsonAuditMessage.put(getEventParamName(name, columnName), value);
    }

    @Override
    public void addEventParameter
    (
        final String name,
        final String columnName,
        final boolean value
    )
    {
        jsonAuditMessage.put(getEventParamName(name, columnName), value);
    }

    private static String getEventParamName
    (
        final String name,
        final String columnName
    )
    {
        return "eventParam" +
            ((columnName == null) ? name : columnName);
    }

    @Override
    public void send() throws Exception
    {
        // Get the topic name
        final String topic = KafkaAuditTopic.getTopicName(applicationId,tenantId);

        // Get the partition key
        final int partitionKey =
            processIdHashCode ^ (int)AuditLogHelper.getThreadId();

        // Get the actual message
        final String auditEventJson = jsonAuditMessage.toString();

        // Put together the record
        final ProducerRecord<String, String> auditEventRecord =
            new ProducerRecord(topic, Integer.toString(partitionKey), auditEventJson);

        // Send the record (synchronously)
        final RecordMetadata metadata = producer.send(auditEventRecord).get();
    }
}
