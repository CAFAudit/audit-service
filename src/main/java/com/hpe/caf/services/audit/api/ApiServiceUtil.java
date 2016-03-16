package com.hpe.caf.services.audit.api;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Utility class for shared functionality.
 */
public class ApiServiceUtil {

    private static final String ERR_MSG_DB_URL_MISSING = "The Vertica database connection URL has not been provided.";
    private static final String ERR_MSG_DB_SERVICE_CREDENTIALS_MISSING = "The credentials for the service database account have not been provided.";
    private static final String ERR_MSG_DB_LOADER_CREDENTIALS_MISSING = "The credentials for the loader database account have not been provided.";

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

}
