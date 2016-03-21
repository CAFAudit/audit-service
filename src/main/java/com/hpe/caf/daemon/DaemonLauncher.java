package com.hpe.caf.daemon;

public interface DaemonLauncher {

    static DaemonLauncher create() {
        return new DockerDaemonLauncher();
    }

    void launch(
        final String id,
        final String image,
        final String[] args
    ) throws Exception;
}
