/*
 * Copyright 2015-2024 Open Text.
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
package com.hpe.caf.services.audit.server.api.impl;

import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.AuditIndexingHint;
import com.hpe.caf.services.audit.server.api.exceptions.BadRequestException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpe.caf.services.audit.server.api.AuditeventsApiService;
import com.hpe.caf.services.audit.server.api.NotFoundException;
import com.hpe.caf.services.audit.server.model.EventParam;
import com.hpe.caf.services.audit.server.model.NewAuditEvent;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@jakarta.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-24T10:10:27.102+01:00")
public class AuditeventsApiServiceImpl extends AuditeventsApiService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditeventsApiServiceImpl.class.getName());

    private static final String ERR_MSG_APPLICATION_ID_NOT_SPECIFIED = "The application identifier has not been specified";
    private static final String ERR_MSG_PROCESS_ID_NOT_SPECIFIED = "The process identifier has not been specified";
    private static final String ERR_MSG_THREAD_ID_NOT_SPECIFIED = "The thread identifier has not been specified";
    private static final String ERR_MSG_EVENT_ORDER_NOT_SPECIFIED = "The event order has not been specified";
    private static final String ERR_MSG_EVENT_TIME_NOT_SPECIFIED = "The event time has not been specified";
    private static final String ERR_MSG_EVENT_TIME_SOURCE_NOT_SPECIFIED = "The event time source has not been specified";
    private static final String ERR_MSG_USER_ID_NOT_SPECIFIED = "The user identifier has not been specified";
    private static final String ERR_MSG_TENANT_ID_NOT_SPECIFIED = "The tenant identifier has not been specified";
    private static final String ERR_MSG_CORRELATION_ID_NOT_SPECIFIED = "The correlation identifier has not been specified";
    private static final String ERR_MSG_EVENT_TYPE_ID_NOT_SPECIFIED = "The event type identifier has not been specified";
    private static final String ERR_MSG_EVENT_CATEGORY_ID_NOT_SPECIFIED = "The event category identifier has not been specified";
    private static final String ERR_MSG_CUSTOM_FIELDS_NOT_SPECIFIED = "Custom audit event fields have not been specified";
    private static final String ERR_MSG_EVEN_PARAM_PARSING = "Error parsing value for audit event parameter: ";

    private static final String EVENT_PARAM_TYPE_STRING = "STRING";
    private static final String EVENT_PARAM_TYPE_SHORT = "SHORT";
    private static final String EVENT_PARAM_TYPE_INT = "INT";
    private static final String EVENT_PARAM_TYPE_LONG = "LONG";
    private static final String EVENT_PARAM_TYPE_FLOAT = "FLOAT";
    private static final String EVENT_PARAM_TYPE_DOUBLE = "DOUBLE";
    private static final String EVENT_PARAM_TYPE_BOOLEAN = "BOOLEAN";
    private static final String EVENT_PARAM_TYPE_DATE = "DATE";
    private static final String EVENT_PARAM_INDEXING_HINT_FULLTEXT = "FULLTEXT";
    private static final String EVENT_PARAM_INDEXING_HINT_KEYWORD = "KEYWORD";

    private AuditConnection auditConnection;

    @Override
    public Response auditeventsPost(NewAuditEvent newAuditEvent, SecurityContext securityContext) throws NotFoundException {
        //  Index new audit event into Elasticsearch.
        try {
            LOG.debug("Start indexing audit event message into Elasticsearch");
            AddNewAuditEvent(newAuditEvent);
            LOG.debug("Indexing audit event message into Elasticsearch complete");
            return Response.noContent().build();
        } catch (BadRequestException e){
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type("text/plain").build();
        } catch(Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type("text/plain").build();
        }
    }

    /**
     * Indexes a new audit event message into Elasticsearch.
     */
    private void AddNewAuditEvent(final NewAuditEvent newAuditEvent) throws Exception, BadRequestException {

        //  Validate the incoming new audit event parameter.
        validateNewAuditEventFields(newAuditEvent);

        // If an AuditConnection has been been established create one with the ConfigurationSource.
        if (auditConnection == null) {
            auditConnection = AuditConnectionFactory.createConnection();
        }

        //  Index audit event message into Elasticsearch.
        try (
                AuditChannel auditChannel = auditConnection.createChannel()
        ) {
            LOG.debug("AuditConnection and AuditChannel created");

            //  Get an instance of AuditCoreMetadataProvider comprising a set of auto-generated field data including
            //  processid, threadId, eventOrder, eventTime and eventTimeSource.
            final AuditCoreMetadataProvider acmp = getAuditCoreMetadataProvider(newAuditEvent);

            //  Create a new event builder object for the audit event message to be indexed into Elasticsearch.
            LOG.debug("Create audit event builder object");
            AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder(acmp);

            //  Add fixed field data to the audit event message.
            LOG.debug("Add fixed audit event parameters to the event builder object");
            addFixedFieldsToAuditEventMessage(auditEventBuilder, newAuditEvent);

            //  Add custom field data to the audit event message.
            LOG.debug("Add custom audit event parameters to the event builder object");
            addCustomFieldsToAuditEventMessage(auditEventBuilder, newAuditEvent);

            //  Send the audit event message to Elasticsearch.
            LOG.debug("Send audit event message to Elasticsearch");
            auditEventBuilder.send();
        } catch (DateTimeParseException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * Throws a BadRequestException if:
     *  fixed or custom fields have not been supplied OR
     *  an unsupported event parameter type or indexing hint has been specified
     */
    private void validateNewAuditEventFields(final NewAuditEvent newAuditEvent) throws BadRequestException {
        //  Verify all fixed fields and at least one custom field has been specified.
        LOG.debug("Checking that fixed and custom audit event parameters have been provided");
        areAuditEventFieldsNullOrEmpty(newAuditEvent);

        //  Validate custom field event type and indexing hint values.
        for (EventParam ep : newAuditEvent.getEventParams()) {
            // Verify the event type matches one of the supported types.
            LOG.debug("Verifying event parameter type is supported");
            if (!isEventParameterTypeSupported(ep)) {
                final String unexpectedParamTypeErrorMessage = "Unexpected parameter type: " + ep.getParamType();
                LOG.error(unexpectedParamTypeErrorMessage);
                throw new BadRequestException(unexpectedParamTypeErrorMessage);
            }
        }
    }

    /**
     * Returns true if the specified event parameter has a supported event type, otherwise false.
     * Throws a BadRequestException if the event type is of type 'string' but comprises an unsupported indexing hint.
     */
    private boolean isEventParameterTypeSupported(final EventParam ep) throws BadRequestException {
        boolean isSupported = false;

        //  Identify parameter type.
        final String epParamType = ep.getParamType();

        switch (epParamType.toUpperCase(Locale.ENGLISH)) {
            case EVENT_PARAM_TYPE_STRING:
                //  Has an indexing hint been specified. If so, make sure it is one of the supported hints.
                if (ep.getParamIndexingHint() != null) {
                    LOG.debug("Verifying event parameter indexing hint is supported");
                    if (!isEventParameterIndexingHintSupported(ep.getParamIndexingHint())) {
                        final String unexpectedParamIndexingHintErrorMessage = "Unexpected parameter indexing hint: "
                                + ep.getParamIndexingHint();
                        LOG.error(unexpectedParamIndexingHintErrorMessage);
                        throw new BadRequestException(unexpectedParamIndexingHintErrorMessage);
                    }
                }
                isSupported = true;
                break;

            case EVENT_PARAM_TYPE_SHORT:
            case EVENT_PARAM_TYPE_INT:
            case EVENT_PARAM_TYPE_LONG:
            case EVENT_PARAM_TYPE_FLOAT:
            case EVENT_PARAM_TYPE_DOUBLE:
            case EVENT_PARAM_TYPE_BOOLEAN:
            case EVENT_PARAM_TYPE_DATE:
                isSupported = true;
                break;

            //  Unexpected parameter event type.
            default:
                break;
        }

        return isSupported;
    }

    /**
     * Returns true if the specified eventParameterIndexingHint is a supported indexing hint, otherwise false.
     */
    private boolean isEventParameterIndexingHintSupported(final String eventParameterIndexingHint) {
        boolean isSupported = false;

        switch (eventParameterIndexingHint.toUpperCase(Locale.ENGLISH)) {
            case EVENT_PARAM_INDEXING_HINT_FULLTEXT:
            case EVENT_PARAM_INDEXING_HINT_KEYWORD:
                isSupported = true;
                break;

            //  Unexpected parameter event indexing hint.
            default:
                break;
        }

        return isSupported;
    }

    /**
     * Throws a BadRequestException if any fixed field is null or empty or no custom audit event fields
     * have been provided.
     */
    private void areAuditEventFieldsNullOrEmpty(final NewAuditEvent newAuditEvent) throws BadRequestException {

        //  Make sure the fixed audit event fields have been supplied.
        if (isNullOrEmpty(newAuditEvent.getApplicationId())) {
            LOG.error(ERR_MSG_APPLICATION_ID_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_APPLICATION_ID_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getProcessId())) {
            LOG.error(ERR_MSG_PROCESS_ID_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_PROCESS_ID_NOT_SPECIFIED);
        }

        if (newAuditEvent.getThreadId() == null || 0 == newAuditEvent.getThreadId()) {
            LOG.error(ERR_MSG_THREAD_ID_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_THREAD_ID_NOT_SPECIFIED);
        }

        if (newAuditEvent.getEventOrder() == null || newAuditEvent.getEventOrder() < 0) {
            LOG.error(ERR_MSG_EVENT_ORDER_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_EVENT_ORDER_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getEventTime())) {
            LOG.error(ERR_MSG_EVENT_TIME_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_EVENT_TIME_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getEventTimeSource())) {
            LOG.error(ERR_MSG_EVENT_TIME_SOURCE_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_EVENT_TIME_SOURCE_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getUserId())) {
            LOG.error(ERR_MSG_USER_ID_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_USER_ID_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getTenantId())) {
            LOG.error(ERR_MSG_TENANT_ID_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_TENANT_ID_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getCorrelationId())) {
            LOG.error(ERR_MSG_CORRELATION_ID_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_CORRELATION_ID_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getEventTypeId())) {
            LOG.error(ERR_MSG_EVENT_TYPE_ID_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_EVENT_TYPE_ID_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getEventCategoryId())) {
            LOG.error(ERR_MSG_EVENT_CATEGORY_ID_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_EVENT_CATEGORY_ID_NOT_SPECIFIED);
        }

        //  Make sure at least one custom audit event field has been supplied.
        if (newAuditEvent.getEventParams().isEmpty()) {
            LOG.error(ERR_MSG_CUSTOM_FIELDS_NOT_SPECIFIED);
            throw new BadRequestException(ERR_MSG_CUSTOM_FIELDS_NOT_SPECIFIED);
        }

    }

    /**
     * Returns TRUE if the specified string is null or empty, otherwise FALSE.
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Returns a AuditCoreMetadataProvider instance comprising the processId, threadId, eventOrder,
     * eventTime and eventTimeSource.
     */
    private AuditCoreMetadataProvider getAuditCoreMetadataProvider(final NewAuditEvent newAuditEvent) throws DateTimeParseException {
        return new AuditCoreMetadataProvider()
        {
            @Override
            public UUID getProcessId()
            {
                return UUID.fromString(newAuditEvent.getProcessId());
            }

            @Override
            public long getThreadId()
            {
                return newAuditEvent.getThreadId().longValue();
            }

            @Override
            public long getEventOrder() { return newAuditEvent.getEventOrder().longValue(); }

            @Override
            public Instant getEventTime()
            {
                return Instant.parse(newAuditEvent.getEventTime());
            }

            @Override
            public String getEventTimeSource()
            {
                return newAuditEvent.getEventTimeSource();
            }
        };
    }

    /**
     * Adds fixed field data to the audit event builder.
     */
    private void addFixedFieldsToAuditEventMessage(AuditEventBuilder auditEventBuilder, final NewAuditEvent newAuditEvent) {
        auditEventBuilder.setApplication(newAuditEvent.getApplicationId());
        auditEventBuilder.setEventType(newAuditEvent.getEventCategoryId(), newAuditEvent.getEventTypeId());
        auditEventBuilder.setCorrelationId(newAuditEvent.getCorrelationId());
        auditEventBuilder.setTenant(newAuditEvent.getTenantId());
        auditEventBuilder.setUser(newAuditEvent.getUserId());
    }

    /**
     * Adds custom field data to the audit event builder.
     */
    private void addCustomFieldsToAuditEventMessage(AuditEventBuilder auditEventBuilder, final NewAuditEvent newAuditEvent) throws BadRequestException {

        //  Iterate through the list of custom event parameter fields and add field data values to the audit event message.
        for (EventParam ep : newAuditEvent.getEventParams()) {

            //  Get parameter name and value.
            final String epParamName = ep.getParamName();
            final String epParamValue = ep.getParamValue();

            //  Get alternative field name to be used in the event that multiple audit events comprise of parameters
            //  with the same name.
            final String epParamColumn = ep.getParamColumnName();

            //  Identify parameter type.
            String epParamType = ep.getParamType();

            try {
                //  Add event parameter details to the audit event builder object by type.
                switch (epParamType.toUpperCase(Locale.ENGLISH)) {
                    case EVENT_PARAM_TYPE_STRING:
                        //  Has an indexing hint been specified.
                        if (ep.getParamIndexingHint() != null) {
                            final String indexingHint = ep.getParamIndexingHint();
                            switch (indexingHint.toUpperCase(Locale.ENGLISH)) {
                                case EVENT_PARAM_INDEXING_HINT_FULLTEXT:
                                    auditEventBuilder.addEventParameter(epParamName, epParamColumn, epParamValue, AuditIndexingHint.FULLTEXT);
                                    break;

                                case EVENT_PARAM_INDEXING_HINT_KEYWORD:
                                    auditEventBuilder.addEventParameter(epParamName, epParamColumn, epParamValue, AuditIndexingHint.KEYWORD);
                                    break;

                                default:
                                    // Only FULLTEXT and KEYWORD supported.
                                    String unexpectedParamIndexingHintErrorMessage = "Unexpected parameter indexing hint: " + ep.getParamIndexingHint().toString();
                                    LOG.error(unexpectedParamIndexingHintErrorMessage);
                                    throw new RuntimeException(unexpectedParamIndexingHintErrorMessage);
                            }
                        } else {
                            auditEventBuilder.addEventParameter(epParamName, epParamColumn, epParamValue);
                        }
                        break;
                    case EVENT_PARAM_TYPE_SHORT:
                        auditEventBuilder.addEventParameter(epParamName, epParamColumn, Short.parseShort(epParamValue));
                        break;
                    case EVENT_PARAM_TYPE_INT:
                        auditEventBuilder.addEventParameter(epParamName, epParamColumn, Integer.parseInt(epParamValue));
                        break;
                    case EVENT_PARAM_TYPE_LONG:
                        auditEventBuilder.addEventParameter(epParamName, epParamColumn, Long.parseLong(epParamValue));
                        break;
                    case EVENT_PARAM_TYPE_FLOAT:
                        auditEventBuilder.addEventParameter(epParamName, epParamColumn, Float.parseFloat(epParamValue));
                        break;
                    case EVENT_PARAM_TYPE_DOUBLE:
                        auditEventBuilder.addEventParameter(epParamName, epParamColumn, Double.parseDouble(epParamValue));
                        break;
                    case EVENT_PARAM_TYPE_BOOLEAN:
                        auditEventBuilder.addEventParameter(epParamName, null, Boolean.parseBoolean(epParamValue));
                        break;
                    case EVENT_PARAM_TYPE_DATE:
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));
                        auditEventBuilder.addEventParameter(epParamName, epParamColumn, df.parse(epParamValue));
                        break;

                    //  Unexpected type.
                    default:
                        String unexpectedParamTypeErrorMessage = "Unexpected parameter type: " + epParamType;
                        LOG.error(unexpectedParamTypeErrorMessage);
                        throw new BadRequestException(unexpectedParamTypeErrorMessage);
                }
            } catch (NullPointerException | NumberFormatException | ParseException e) {
                LOG.error(ERR_MSG_EVEN_PARAM_PARSING + epParamName);
                throw new BadRequestException(ERR_MSG_EVEN_PARAM_PARSING + epParamName, e);
            }
        }
    }
}
