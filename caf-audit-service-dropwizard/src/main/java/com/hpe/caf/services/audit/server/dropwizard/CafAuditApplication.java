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
package com.hpe.caf.services.audit.server.dropwizard;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cafapi.correlation.dropwizard.CorrelationIdBundle;
import com.github.cafapi.ssl.dropwizard.DropWizardSslBundleProvider;
import com.hpe.caf.auditing.exception.AuditConfigurationException;
import com.hpe.caf.services.audit.server.api.CafAuditServiceModule;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.health.DefaultHealthFactory;
import io.dropwizard.health.HealthCheckConfiguration;
import io.dropwizard.health.HealthCheckType;
import io.dropwizard.health.Schedule;
import io.dropwizard.logging.common.LoggingUtil;
import io.dropwizard.util.Duration;

public final class CafAuditApplication extends Application<CafAuditConfiguration>
{
    private static final Logger LOG = LoggerFactory.getLogger(CafAuditApplication.class.getName());

    private final boolean useInternalConfig;

    private CafAuditApplication(final boolean useInternalConfig)
    {
        this.useInternalConfig = useInternalConfig;
    }

    public static void main(final String[] args) throws Exception
    {
        if (args.length == 0) {
            new CafAuditApplication(true).run("server", "/config.yaml");
        } else {
            new CafAuditApplication(false).run(args);
        }
    }

    @Override
    protected void bootstrapLogging()
    {
        LoggingUtil.hijackJDKLogging();
    }

    @Override
    public void initialize(final Bootstrap<CafAuditConfiguration> bootstrap)
    {
        // Pick up the built-in config file from resources
//        if (useInternalConfig) {
//            bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
//                    new ResourceConfigurationSourceProvider(),
//                    new EnvironmentVariableSubstitutor(false, true)));
//        } else {
//            bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
//                    bootstrap.getConfigurationSourceProvider(),
//                    new EnvironmentVariableSubstitutor(false, true))
//            );
//        }

        if (useInternalConfig) {
            bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
        }

        // Add functionality bundles
        bootstrap.addBundle(new CorrelationIdBundle<>());
        bootstrap.addBundle(DropWizardSslBundleProvider.getInstance());
        bootstrap.addBundle(CafAuditSwaggerUiBundle.INSTANCE);
        bootstrap.addBundle(new AssetsBundle("/root-redirect/", "/", "index.html", "root-redirect"));
    }

    @Override
    public void run(
        final CafAuditConfiguration configuration,
        final Environment environment
    ) throws Exception
    {
        initHealthChecks(configuration, environment);
        CafAuditServiceModule.registerProviders(environment.jersey()::register);
    }

    private void initHealthChecks(
            final CafAuditConfiguration configuration,
            final Environment environment) throws AuditConfigurationException
    {
        final List<HealthCheckConfiguration> healthCheckConfigurations = new ArrayList<>();

        /////////////////////////////
        // Liveness Checks
        /////////////////////////////

        final Schedule livenessSchedule = createSchedule(
                configuration.getHealthConfiguration().getLivenessInitialDelaySeconds(),
                configuration.getHealthConfiguration().getLivenessCheckIntervalSeconds(),
                configuration.getHealthConfiguration().getLivenessDowntimeIntervalSeconds(),
                configuration.getHealthConfiguration().getLivenessSuccessAttempts(),
                configuration.getHealthConfiguration().getLivenessFailureAttempts());

        // TODO debug
        LOG.error("Liveness checks will be run on the following schedule: " +
                        "initialDelay={}, checkInterval={}, downtimeInterval={}, successAttempts={}, failureAttempts={}",
                livenessSchedule.getInitialDelay(), livenessSchedule.getCheckInterval(), livenessSchedule.getDowntimeInterval(),
                livenessSchedule.getSuccessAttempts(), livenessSchedule.getFailureAttempts());

        // deadlocks is supplied by default by Dropwizard, we don't need to register it
        healthCheckConfigurations.add(createHealthCheckConfiguration("deadlocks", HealthCheckType.ALIVE, livenessSchedule));

        /////////////////////////////
        // Readiness Checks
        /////////////////////////////

        final Schedule readinessSchedule = createSchedule(
                configuration.getHealthConfiguration().getReadinessInitialDelaySeconds(),
                configuration.getHealthConfiguration().getReadinessCheckIntervalSeconds(),
                configuration.getHealthConfiguration().getReadinessDowntimeIntervalSeconds(),
                configuration.getHealthConfiguration().getReadinessSuccessAttempts(),
                configuration.getHealthConfiguration().getReadinessFailureAttempts());

        // TODO debug
        LOG.error("Readiness checks will be run on the following schedule: " +
                        "initialDelay={}, checkInterval={}, downtimeInterval={}, successAttempts={}, failureAttempts={}",
                readinessSchedule.getInitialDelay(), readinessSchedule.getCheckInterval(), readinessSchedule.getDowntimeInterval(),
                readinessSchedule.getSuccessAttempts(), readinessSchedule.getFailureAttempts());

        environment.healthChecks().register("service", new CafAuditHealthCheck());
        healthCheckConfigurations.add(createHealthCheckConfiguration("service", HealthCheckType.READY, readinessSchedule));

        /////////////////////////////
        // HealthFactory Creation
        /////////////////////////////

        final DefaultHealthFactory healthFactory = new DefaultHealthFactory();

        healthFactory.setHealthCheckConfigurations(healthCheckConfigurations);
        healthFactory.configure(environment.lifecycle(), environment.servlets(), environment.jersey(), environment.health(),
                environment.getObjectMapper(), getName());

        configuration.setHealthFactory(healthFactory);
    }

    private static Schedule createSchedule(
            final int initialDelaySeconds,
            final int checkIntervalSeconds,
            final int downtimeIntervalSeconds,
            final int successAttempts,
            final int failureAttempts) {
        final Schedule schedule = new Schedule();

        schedule.setInitialDelay(Duration.seconds(initialDelaySeconds));
        schedule.setCheckInterval(Duration.seconds(checkIntervalSeconds));
        schedule.setDowntimeInterval(Duration.seconds(downtimeIntervalSeconds));
        schedule.setSuccessAttempts(successAttempts);
        schedule.setFailureAttempts(failureAttempts);

        return schedule;
    }

    private static HealthCheckConfiguration createHealthCheckConfiguration(
            final String name,
            final HealthCheckType healthCheckType,
            final Schedule schedule)
    {
        final HealthCheckConfiguration healthCheckConfiguration = new HealthCheckConfiguration();

        healthCheckConfiguration.setName(name);
        healthCheckConfiguration.setType(healthCheckType);
        healthCheckConfiguration.setInitialState(false);
        healthCheckConfiguration.setSchedule(schedule);

        // Setting critical to false means that the /health-check endpoint returns HTTP 200 even if a healthcheck fails, which is
        // not desired. Setting critical to true means that the /health-check endpoint returns HTTP 503 when a healthcheck fails.
        healthCheckConfiguration.setCritical(true);

        return healthCheckConfiguration;
    }
}
