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

import com.github.cafapi.correlation.dropwizard.CorrelationIdBundle;
import com.github.cafapi.dropwizardssl.DropWizardSslBundleProvider;
import com.hpe.caf.services.audit.server.api.CafAuditServiceModule;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.logging.common.LoggingUtil;

public final class CafAuditApplication extends Application<CafAuditConfiguration>
{
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
        environment.healthChecks().register("service", new CafAuditHealthCheck());
        CafAuditServiceModule.registerProviders(environment.jersey()::register);
    }
}
