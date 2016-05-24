package com.hpe.caf.services.audit.api;

import com.hpe.caf.auditing.schema.AuditedApplication;
import com.hpe.caf.services.audit.api.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;

/**
 * Utility class for shared functionality.
 */
public class ApiServiceUtil {

    public static final String CUSTOM_EVENT_PARAM_PREFIX = "eventParam";
    public static final String ERR_MSG_TENANTID_CONTAINS_INVALID_CHARS = "The tenantId contains invalid characters (allowed: lowercase letters and digits).";
    public static final String TENANTID_SCHEMA_PREFIX = "account_";
    public static final String KAFKA_SCHEDULER_NAME_PREFIX = "auditscheduler_";
    public static final String KAFKA_TARGET_TOPIC_PREFIX = "AuditEventTopic";
    public static final String KAFKA_REJECT_TABLE = "kafka_rej";
    public static final String KAFKA_TARGET_TABLE_PREFIX = "Audit";
    public static final String TENANTID_INVALID_CHARS_REGEX = "^[a-z0-9]*$";
    public static final Integer VERTICA_MAX_VARCHAR_SIZE = 65000;

    private static final String ERR_MSG_DB_URL_MISSING = "The Vertica database connection URL has not been provided.";
    private static final String ERR_MSG_DB_SERVICE_CREDENTIALS_MISSING = "The credentials for the service database account have not been provided.";
    private static final String ERR_MSG_DB_LOADER_CREDENTIALS_MISSING = "The credentials for the loader database account have not been provided.";
    private static final String ERR_MSG_UNEXPECTED_TYPE = "Unexpected parameter type.";
    private static final String ERR_MSG_XML_READ_FAILURE = "Failed to bind the XML audit events file.";
    private static final String ERR_MSG_INVALID_STRING_LENGTH = "Length for audit event parameter type 'string' must be at least 1.";

    private static final Integer VERTICA_MAX_LONG_VARCHAR_SIZE = 32000000;
    private static final String VERTICA_MAX_VARCHAR_TYPE = "varchar(65000)";
    private static final String VERTICA_MAX_LONG_VARCHAR_TYPE = "long varchar(32000000)";

    private static final Logger LOG = LoggerFactory.getLogger(ApiServiceUtil.class);

    /**
     * Load required inputs from config.properties or environment variables.
     */
    public static AppConfig getAppConfigProperties() throws BadRequestException {
        AppConfig properties;

        AnnotationConfigApplicationContext propertiesApplicationContext = new AnnotationConfigApplicationContext();
        propertiesApplicationContext.register(PropertySourcesPlaceholderConfigurer.class);
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(AppConfig.class);
        propertiesApplicationContext.registerBeanDefinition("AppConfig", beanDefinition);
        propertiesApplicationContext.refresh();

        properties = propertiesApplicationContext.getBean(AppConfig.class);

        try {
            //  Make sure database URL has been provided.
            if (properties.getDatabaseURL() == null) {
                throw new BadRequestException(ERR_MSG_DB_URL_MISSING);
            }
        } catch (NullPointerException npe) {
            throw new BadRequestException(ERR_MSG_DB_URL_MISSING);
        }

        try {
            //  Make sure database service account credentials have been provided.
            if (properties.getDatabaseServiceAccount() == null ||
                    properties.getDatabaseServiceAccountPassword() == null) {
                throw new BadRequestException(ERR_MSG_DB_SERVICE_CREDENTIALS_MISSING);
            }
        } catch (NullPointerException npe) {
            throw new BadRequestException(ERR_MSG_DB_SERVICE_CREDENTIALS_MISSING);
        }

        try {
            //  Make sure database loader account credentials have been provided.
            if (properties.getDatabaseLoaderAccount() == null ||
                    properties.getDatabaseLoaderAccountPassword() == null) {
                throw new BadRequestException(ERR_MSG_DB_LOADER_CREDENTIALS_MISSING);
            }
        } catch (NullPointerException npe) {
            throw new BadRequestException(ERR_MSG_DB_LOADER_CREDENTIALS_MISSING);
        }

        return properties;
    }

    /**
     * Maps the audited application data xml on to the AuditedApplication object (i.e. XML/Java binding).
     */
    public static AuditedApplication getAuditedApplication(byte[] auditXMLConfigBytes) throws Exception {

        AuditedApplication auditAppData;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(auditXMLConfigBytes);
            LOG.debug("getAuditedApplication: Binding audit events XML to AuditedApplication...");
            auditAppData = JAXBUnmarshal.bindAuditEventsXml(bais);
        } catch (JAXBException e) {
            LOG.error("getAuditedApplication: Error - '{}'", ERR_MSG_XML_READ_FAILURE);
            throw new Exception(ERR_MSG_XML_READ_FAILURE);
        }

        return auditAppData;
    }

    /**
     * Returns True if the specified string is not null or empty.
     */
    public static boolean isNotNullOrEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * Returns a Vertica type declaration based on the audited application data xml parameter type.
     */
    public static String getVerticaType(String columnType, Integer maxLengthConstraint) throws BadRequestException {
        String returnValue = null;

        switch (columnType.toLowerCase()) {
            case "short":
            case "int":
            case "long":
                returnValue = "int";
                break;
            case "string":
                returnValue = getVerticaStringType(maxLengthConstraint);
                break;
            case "float":
            case "double":
                returnValue = "float";
                break;
            case "boolean":
                returnValue = "boolean";
                break;
            case "date":
                returnValue = "timestamp";
                break;
            default:
                throw new BadRequestException(ERR_MSG_UNEXPECTED_TYPE);
        }

        return returnValue;
    }

    /**
     * Returns a Vertica string type declaration based on max length column constraint.
     */
    private static String getVerticaStringType(Integer maxLengthConstraint) throws BadRequestException {
        String returnValue = null;

        if (maxLengthConstraint != null) {
            if (maxLengthConstraint <= 0) {
                throw new BadRequestException(ERR_MSG_INVALID_STRING_LENGTH);
            }
            else if (maxLengthConstraint <= VERTICA_MAX_VARCHAR_SIZE) {
                returnValue = "varchar(" + maxLengthConstraint.toString() + ")";
            } else if (maxLengthConstraint > VERTICA_MAX_VARCHAR_SIZE && maxLengthConstraint <= VERTICA_MAX_LONG_VARCHAR_SIZE) {
                returnValue = "long varchar(" + maxLengthConstraint.toString() + ")";
            } else {
                returnValue = VERTICA_MAX_LONG_VARCHAR_TYPE;
            }
        } else {
            returnValue = VERTICA_MAX_VARCHAR_TYPE;
        }
        return returnValue;
    }

}
