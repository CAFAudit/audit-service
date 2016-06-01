package com.hpe.caf.auditing.kafka;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.AuditLogHelper;
import org.apache.kafka.clients.producer.*;

import java.text.MessageFormat;

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
                    final String value,
                    final Integer minLength,
                    final Integer maxLength
            ) throws Exception {
        ValidateFieldLength(name, value, minLength, maxLength);
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

    /**
     * Validates the field length and throws an appropriate exception if min or max constraints are exceeded.
     */
    private static void ValidateFieldLength(String fieldName, String fieldValue, Integer minLength, Integer maxLength)
            throws Exception
    {
        Integer fieldLength = fieldValue.length();

        if (minLength != null) {
            if (fieldLength < minLength) {
                throw new Exception(MessageFormat.format("Field name {0} is too short, minimum is {1} characters.", fieldName, minLength));
            }
        }

        if (maxLength != null) {
            if (fieldLength > maxLength) {
                throw new Exception(MessageFormat.format("Field name {0} is too long, maximum is {1} characters.", fieldName, maxLength));
            }
        }
    }

}
