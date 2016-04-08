package com.hpe.caf.daemon;

import com.hpe.caf.services.audit.api.AppConfig;

public interface DaemonLauncher {

    static DaemonLauncher create(final AppConfig properties) {

        //  If CAF_MARATHON_URL environment variable present then launch via Marathon, otherwise Docker.
        if (properties.getMarathonUrl() != null) {
            return new MarathonDaemonLauncher(properties);
        } else {
            return new DockerDaemonLauncher();
        }

    }

    void launch(
        final String id,
        final String image,
        final String[] args
    ) throws Exception;
}
