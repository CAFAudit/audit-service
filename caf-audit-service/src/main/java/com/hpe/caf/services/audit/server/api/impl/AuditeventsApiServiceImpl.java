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
package com.hpe.caf.services.audit.server.api.impl;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.elastic.ElasticAuditConfiguration;
import com.hpe.caf.services.audit.server.api.*;
import com.hpe.caf.services.audit.server.api.exceptions.BadRequestException;
import com.hpe.caf.services.audit.server.model.*;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2017-04-28T07:15:58.947+01:00")
public class AuditeventsApiServiceImpl extends AuditeventsApiService {

    private static final Logger LOG = LogManager.getLogger(AuditeventsApiServiceImpl.class.getName());

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
    private static final String ERR_MSG_ES_HOST_AND_PORT_MISSING = "The Elasticsearch host and port have not been provided";
    private static final String ERR_MSG_EVEN_PARAM_PARSING = "Error parsing value for audit event parameter: ";

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
    private void AddNewAuditEvent(final NewAuditEvent newAuditEvent) throws Exception, BadRequestException, ConfigurationException {

        //  Make sure fixed audit event fields have been supplied.
        LOG.debug("Checking that fixed and custom audit event parameters have been provided");
        areAuditEventFieldsNullOrEmpty(newAuditEvent);

        //  Index audit event message into Elasticsearch.
        try (
                AuditConnection auditConnection = AuditConnectionFactory.createConnection(getConfigurationSource());
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

        if (newAuditEvent.getEventOrder() == null || 0 == newAuditEvent.getEventOrder()) {
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
     * Load required inputs from config.properties or environment variables.
     */
    private AppConfig getAppConfigProperties() throws ConfigurationException {
        AppConfig appConfig;

        LOG.debug("Load application configuration");
        AnnotationConfigApplicationContext propertiesApplicationContext = new AnnotationConfigApplicationContext();
        propertiesApplicationContext.register(PropertySourcesPlaceholderConfigurer.class);
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(AppConfig.class);
        propertiesApplicationContext.registerBeanDefinition("AppConfig", beanDefinition);
        propertiesApplicationContext.refresh();

        appConfig = propertiesApplicationContext.getBean(AppConfig.class);

        //  Make sure Elasticsearch host and port have been provided.
        try {
            if (appConfig.getElasticHostAndPortValues() == null) {
                LOG.error(ERR_MSG_ES_HOST_AND_PORT_MISSING);
                throw new ConfigurationException(ERR_MSG_ES_HOST_AND_PORT_MISSING);
            }
        } catch (NullPointerException npe) {
            LOG.error(ERR_MSG_ES_HOST_AND_PORT_MISSING);
            throw new ConfigurationException(ERR_MSG_ES_HOST_AND_PORT_MISSING);
        }

        return appConfig;
    }

    /**
     * Returns a ConfigurationSource instance.
     */
    private ConfigurationSource getConfigurationSource(){
        final ConfigurationSource configSource = new ConfigurationSource()
        {
            private String DEFAULT_CLUSTER_NAME = "elasticsearch";
            private int DEFAULT_NUMBER_OF_SHARDS = 5;
            private int DEFAULT_NUMBER_OF_REPLICAS = 1;

            @Override
            public <T> T getConfiguration(Class<T> type) throws ConfigurationException
            {
                AppConfig appConfig = getAppConfigProperties();

                //  Host and port must always be provided.
                String hostAndPort = appConfig.getElasticHostAndPortValues();

                //  Support for optional configuration properties and defaults.
                String clusterName = (appConfig.getElasticClusterName() != null) ? appConfig.getElasticClusterName() : DEFAULT_CLUSTER_NAME;
                int numberOfShards = (appConfig.getElasticNumberOfShards() != 0) ? appConfig.getElasticNumberOfShards() : DEFAULT_NUMBER_OF_SHARDS;
                int numberOfReplicas = (appConfig.getElasticNumberOfReplicas() != 0) ? appConfig.getElasticNumberOfReplicas() : DEFAULT_NUMBER_OF_REPLICAS;

                //  Create and return audit configuration source instance.
                ElasticAuditConfiguration config = new ElasticAuditConfiguration();
                config.setClusterName(clusterName);
                config.setHostAndPortValues(hostAndPort);
                config.setNumberOfShards(numberOfShards);
                config.setNumberOfReplicas(numberOfReplicas);
                return (T) config;
            }
        };

        return configSource;
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

            //  Identify parameter type and format.
            EventParam.ParamTypeEnum epParamType = ep.getParamType();
            EventParam.ParamFormatEnum epParamFormat = ep.getParamFormat();

            //  Perform OpenAPI type/format to java primitive type mapping and add event parameter to the message.
            switch (epParamType) {
                //  Type is integer. Could be signed 32 or 64 bits.
                case INTEGER:
                    addIntegerTypeEventParameter(auditEventBuilder, epParamFormat, epParamName, epParamColumn, epParamValue);
                    break;

                //  Type is number. Could be float or double.
                case NUMBER:
                    addNumberTypeEventParameter(auditEventBuilder, epParamFormat, epParamName, epParamColumn, epParamValue);
                    break;

                //  Type is string. Could be string or date.
                case STRING:
                    addStringTypeEventParameter(auditEventBuilder, epParamFormat, epParamName, epParamColumn, epParamValue);
                    break;

                //  Type is boolean.
                case BOOLEAN:
                    auditEventBuilder.addEventParameter(epParamName, null, Boolean.parseBoolean(epParamValue));
                    break;

                //  Unexpected type.
                default:
                    String unexpectedParamTypeErrorMessage = "Unexpected parameter type: " + epParamType.toString();
                    LOG.error(unexpectedParamTypeErrorMessage);
                    throw new BadRequestException(unexpectedParamTypeErrorMessage);
            }
        }
    }

    /**
     * Adds an integer or long audit event parameter to the audit event builder.
     */
    private void addIntegerTypeEventParameter(AuditEventBuilder auditEventBuilder, final EventParam.ParamFormatEnum epParamFormat, final String epParamName, final String epParamColumn, final String epParamValue) throws BadRequestException {
        try {
            if (null != epParamFormat) {
                switch (epParamFormat) {
                    case INT32:
                        auditEventBuilder.addEventParameter(epParamName, epParamColumn, Integer.parseInt(epParamValue));
                        break;
                    case INT64:
                        auditEventBuilder.addEventParameter(epParamName, epParamColumn, Long.parseLong(epParamValue));
                        break;
                    default:
                        // Unexpected format for integer type.
                        String unexpectedParamFormatErrorMessage = "Unexpected parameter format for type 'integer': " + epParamFormat.toString();
                        LOG.error(unexpectedParamFormatErrorMessage);
                        throw new BadRequestException(unexpectedParamFormatErrorMessage);
                }
            } else {
                //  Default to long if format has not been provided.
                auditEventBuilder.addEventParameter(epParamName, epParamColumn, Long.parseLong(epParamValue));
            }
        } catch (NullPointerException | NumberFormatException e) {
            LOG.error(ERR_MSG_EVEN_PARAM_PARSING + epParamName);
            throw new BadRequestException(ERR_MSG_EVEN_PARAM_PARSING + epParamName, e);
        }
    }

    /**
     * Adds a float or double audit event parameter to the audit event builder.
     */
    private void addNumberTypeEventParameter(AuditEventBuilder auditEventBuilder, final EventParam.ParamFormatEnum epParamFormat, final String epParamName, final String epParamColumn, final String epParamValue) throws BadRequestException{
        try {
            if (null != epParamFormat) {
                switch (epParamFormat) {
                    case FLOAT:
                        auditEventBuilder.addEventParameter(epParamName, epParamColumn, Float.parseFloat(epParamValue));
                        break;
                    case DOUBLE:
                        auditEventBuilder.addEventParameter(epParamName, epParamColumn, Double.parseDouble(epParamValue));
                        break;
                    default:
                        // Unexpected format for number type.
                        String unexpectedParamFormatErrorMessage = "Unexpected parameter format for type 'number': " + epParamFormat.toString();
                        LOG.error(unexpectedParamFormatErrorMessage);
                        throw new BadRequestException(unexpectedParamFormatErrorMessage);
                }
            } else {
                //  Default to double if format has not been provided.
                auditEventBuilder.addEventParameter(epParamName, epParamColumn, Double.parseDouble(epParamValue));
            }
        } catch (NullPointerException | NumberFormatException e) {
            LOG.error(ERR_MSG_EVEN_PARAM_PARSING + epParamName);
            throw new BadRequestException(ERR_MSG_EVEN_PARAM_PARSING + epParamName, e);
        }
    }

    /**
     * Adds a date or string audit event parameter to the audit event builder.
     */
    private void addStringTypeEventParameter(AuditEventBuilder auditEventBuilder, final EventParam.ParamFormatEnum epParamFormat, final String epParamName, final String epParamColumn, final String epParamValue) throws BadRequestException {
        try {
            if (null != epParamFormat) {
                switch (epParamFormat) {
                    case DATE:
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));
                        auditEventBuilder.addEventParameter(epParamName, epParamColumn, df.parse(epParamValue));
                        break;
                    default:
                        // Unexpected format for string type.
                        String unexpectedParamFormatErrorMessage = "Unexpected parameter format for type 'string': " + epParamFormat.toString();
                        LOG.error(unexpectedParamFormatErrorMessage);
                        throw new BadRequestException(unexpectedParamFormatErrorMessage);
                }
            } else {
                //  Default to string if format has not been provided.
                auditEventBuilder.addEventParameter(epParamName, epParamColumn, epParamValue);
            }
        } catch (ParseException e) {
            LOG.error(ERR_MSG_EVEN_PARAM_PARSING + epParamName);
            throw new BadRequestException(ERR_MSG_EVEN_PARAM_PARSING + epParamName, e);
        }
    }
}
