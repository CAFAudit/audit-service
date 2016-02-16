package com.hpe.caf.services.audit.api;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Utility class for shared functionality.
 */
public class ApiServiceUtil {

    private static final String ERR_MSG_DB_CONNECTION_PROPS_MISSING = "One or more Vertica database connection properties have not been provided.";

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
            //  Make sure DB connection properties have been specified.
            if (properties.getDatabaseURL() == null ||
                    properties.getDatabaseUsername() == null ||
                    properties.getDatabasePassword() == null) {
                throw new BadRequestException(ERR_MSG_DB_CONNECTION_PROPS_MISSING);
            }
        } catch (NullPointerException npe) {
            throw new BadRequestException(ERR_MSG_DB_CONNECTION_PROPS_MISSING);
        }

        return properties;
    }

}
