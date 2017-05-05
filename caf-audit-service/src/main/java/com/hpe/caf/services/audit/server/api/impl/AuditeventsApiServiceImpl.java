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
import com.hpe.caf.auditing.*;
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
import java.util.TimeZone;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2017-04-28T07:15:58.947+01:00")
public class AuditeventsApiServiceImpl extends AuditeventsApiService {

    private static final String ERR_MSG_APPLICATION_ID_NOT_SPECIFIED = "The application identifier has not been specified.";
    private static final String ERR_MSG_PROCESS_ID_NOT_SPECIFIED = "The process identifier has not been specified.";
    private static final String ERR_MSG_THREAD_ID_NOT_SPECIFIED = "The thread identifier has not been specified.";
    private static final String ERR_MSG_EVENT_ORDER_NOT_SPECIFIED = "The event order has not been specified.";
    private static final String ERR_MSG_EVENT_TIME_NOT_SPECIFIED = "The event time has not been specified.";
    private static final String ERR_MSG_EVENT_TIME_SOURCE_NOT_SPECIFIED = "The event time source has not been specified.";
    private static final String ERR_MSG_USER_ID_NOT_SPECIFIED = "The user identifier has not been specified.";
    private static final String ERR_MSG_TENANT_ID_NOT_SPECIFIED = "The tenant identifier has not been specified.";
    private static final String ERR_MSG_CORRELATION_ID_NOT_SPECIFIED = "The correlation identifier has not been specified.";
    private static final String ERR_MSG_EVENT_TYPE_ID_NOT_SPECIFIED = "The event type identifier has not been specified.";
    private static final String ERR_MSG_EVENT_CATEGORY_ID_NOT_SPECIFIED = "The event category identifier has not been specified.";
    private static final String ERR_MSG_CUSTOM_FIELDS_NOT_SPECIFIED = "Custom audit event fields have not been specified.";
    private static final String ERR_MSG_ES_HOST_AND_PORT_MISSING = "The Elasticsearch host and port have not been provided.";

    @Override
    public Response auditeventsPost(NewAuditEvent newAuditEvent,SecurityContext securityContext) throws NotFoundException {

        //  Index new audit event into Elasticsearch.
        try {
            AddNewAuditEvent(newAuditEvent);
            return Response.noContent().build();
// TODO            return Response.ok().build();
//            return Response.status(Response.Status.OK).entity("Audit event successfully indexed into Elasticsearch").build();
        } catch (BadRequestException | ConfigurationException e){
//            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(ApiResponseMessage.ERROR,e.getMessage())).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch(Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ApiResponseMessage(ApiResponseMessage.ERROR,e.getMessage())).build();
        }

    }

    /**
     * Indexes a new audit event message into Elasticsearch.
     */
    private void AddNewAuditEvent(final NewAuditEvent newAuditEvent) throws Exception {

        //  Make sure fixed audit event fields have been supplied.
        areAuditEventFieldsNullOrEmpty(newAuditEvent);

        //  Index audit event message into Elasticsearch.
        try (
                AuditConnection auditConnection = AuditConnectionFactory.createConnection(getConfigurationSource());
                AuditChannel auditChannel = auditConnection.createChannel()
        ) {
            //  Get an instance of AuditCoreMetadataProvider comprising a set of auto-generated field data including
            //  processid, threadId, eventOrder, eventTime and eventTimeSource.
            final AuditCoreMetadataProvider acmp = getAuditCoreMetadataProvider(newAuditEvent);

            //  Create a new event builder object for the audit event message to be indexed into Elasticsearch.
            AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder(acmp);

            //  Add fixed field data to the audit event message.
            addFixedFieldsToAuditEventMessage(auditEventBuilder, newAuditEvent);

            //  Add custom field data to the audit event message.
            addCustomFieldsToAuditEventMessage(auditEventBuilder, newAuditEvent);

            //  Send the audit event message to Elasticsearch.
            auditEventBuilder.send();
        }
    }

    /**
     * Throws a BadRequestException if any fixed field is null or empty or no custom audit event fields
     * have been provided.
     */
    private void areAuditEventFieldsNullOrEmpty(final NewAuditEvent newAuditEvent) throws BadRequestException {

        //  Make sure the fixed audit event fields have been supplied.
        if (isNullOrEmpty(newAuditEvent.getApplicationId())) {
            throw new BadRequestException(ERR_MSG_APPLICATION_ID_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getProcessId())) {
            throw new BadRequestException(ERR_MSG_PROCESS_ID_NOT_SPECIFIED);
        }

        if (0 == newAuditEvent.getThreadId()) {
            throw new BadRequestException(ERR_MSG_THREAD_ID_NOT_SPECIFIED);
        }

        if (0 == newAuditEvent.getEventOrder()) {
            throw new BadRequestException(ERR_MSG_EVENT_ORDER_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getEventTime())) {
            throw new BadRequestException(ERR_MSG_EVENT_TIME_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getEventTimeSource())) {
            throw new BadRequestException(ERR_MSG_EVENT_TIME_SOURCE_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getUserId())) {
            throw new BadRequestException(ERR_MSG_USER_ID_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getTenantId())) {
            throw new BadRequestException(ERR_MSG_TENANT_ID_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getCorrelationId())) {
            throw new BadRequestException(ERR_MSG_CORRELATION_ID_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getEventTypeId())) {
            throw new BadRequestException(ERR_MSG_EVENT_TYPE_ID_NOT_SPECIFIED);
        }

        if (isNullOrEmpty(newAuditEvent.getEventCategoryId())) {
            throw new BadRequestException(ERR_MSG_EVENT_CATEGORY_ID_NOT_SPECIFIED);
        }

        //  Make sure at least one custom audit event field has been supplied.
        if (newAuditEvent.getEventParams().isEmpty()) {
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

        AnnotationConfigApplicationContext propertiesApplicationContext = new AnnotationConfigApplicationContext();
        propertiesApplicationContext.register(PropertySourcesPlaceholderConfigurer.class);
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(AppConfig.class);
        propertiesApplicationContext.registerBeanDefinition("AppConfig", beanDefinition);
        propertiesApplicationContext.refresh();

        appConfig = propertiesApplicationContext.getBean(AppConfig.class);

        //  Make sure Elasticsearch host and port have been provided.
        try {
            if (appConfig.getElasticHostAndPort() == null) {
                throw new ConfigurationException(ERR_MSG_ES_HOST_AND_PORT_MISSING);
            }
        } catch (NullPointerException npe) {
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
                String hostAndPort = appConfig.getElasticHostAndPort();

                //  Support for optional configuration properties and defaults.
                String clusterName = (appConfig.getElasticClusterName() != null) ? appConfig.getElasticClusterName() : DEFAULT_CLUSTER_NAME;
                int numberOfShards = (appConfig.getElasticNumberOfShards() != 0) ? appConfig.getElasticNumberOfShards() : DEFAULT_NUMBER_OF_SHARDS;
                int numberOfReplicas = (appConfig.getElasticNumberOfReplicas() != 0) ? appConfig.getElasticNumberOfReplicas() : DEFAULT_NUMBER_OF_REPLICAS;

                //  Create and return audit configuration source instance.
                ElasticAuditConfiguration config = new ElasticAuditConfiguration();
                config.setClusterName(clusterName);
                config.setHostAndPort(hostAndPort);
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
    private AuditCoreMetadataProvider getAuditCoreMetadataProvider(final NewAuditEvent newAuditEvent) {
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
    private void addCustomFieldsToAuditEventMessage(AuditEventBuilder auditEventBuilder, final NewAuditEvent newAuditEvent) throws ParseException {

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
            switch(epParamType) {
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
                    throw new IllegalArgumentException("Unexpected paramater type: " + epParamType.toString());
            }
        }
    }

    /**
     * Adds an integer or long audit event parameter to the audit event builder.
     */
    private void addIntegerTypeEventParameter(AuditEventBuilder auditEventBuilder, final EventParam.ParamFormatEnum epParamFormat, final String epParamName, final String epParamColumn, final String epParamValue){
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
                    throw new IllegalArgumentException("Unexpected paramater format: " + epParamFormat.toString());
            }
        } else {
            //  Default to long if format has not been provided.
            auditEventBuilder.addEventParameter(epParamName, epParamColumn, Long.parseLong(epParamValue));
        }
    }

    /**
     * Adds a float or double audit event parameter to the audit event builder.
     */
    private void addNumberTypeEventParameter(AuditEventBuilder auditEventBuilder, final EventParam.ParamFormatEnum epParamFormat, final String epParamName, final String epParamColumn, final String epParamValue){
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
                    throw new IllegalArgumentException("Unexpected paramater format: " + epParamFormat.toString());
            }
        } else {
            //  Default to double if format has not been provided.
            auditEventBuilder.addEventParameter(epParamName, epParamColumn, Double.parseDouble(epParamValue));
        }
    }

    /**
     * Adds a date or string audit event parameter to the audit event builder.
     */
    private void addStringTypeEventParameter(AuditEventBuilder auditEventBuilder, final EventParam.ParamFormatEnum epParamFormat, final String epParamName, final String epParamColumn, final String epParamValue) throws ParseException {
        if (null != epParamFormat) {
            switch (epParamFormat) {
                case DATE:
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    auditEventBuilder.addEventParameter(epParamName, epParamColumn, df.parse(epParamValue));
                    break;
                default:
                    // Unexpected format for string type.
                    throw new IllegalArgumentException("Unexpected paramater format: " + epParamFormat.toString());
            }
        } else {
            //  Default to string if format has not been provided.
            auditEventBuilder.addEventParameter(epParamName, epParamColumn, epParamValue);
        }
    }
}
