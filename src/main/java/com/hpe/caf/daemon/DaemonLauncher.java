package com.hpe.caf.daemon;

import java.util.Map;

public interface DaemonLauncher {

    static DaemonLauncher create() {

        //  If CAF_MARATHON_URL environment variable present then launch via Marathon, otherwise Docker.
        String value = System.getenv("CAF_MARATHON_URL");
        if (value != null) {
            return new MarathonDaemonLauncher(value);
        } else {
            return new DockerDaemonLauncher();
        }

    }

    void launch(
        final String id,
        final String image,
        final String[] args,
        final String marathonCPUs,
        final String marathonMem,
        final String marathonInstances,
        final String[] marathonURIs,
        final String marathonContainerType,
        final String marathonContainerNetwork,
        final String marathonDockerForcePullImage
    ) throws Exception;
}
