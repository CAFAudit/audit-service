package com.hpe.caf.daemon;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.client.model.HostConfig;
import com.github.dockerjava.client.model.RestartPolicy;
import com.github.dockerjava.jaxrs1.JaxRs1Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DockerDaemonLauncher implements DaemonLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(DockerDaemonLauncher.class);

    @Override
    public void launch(
        final String id,
        final String image,
        final String[] args
    ) {
        LOG.info("launch: Launching Scheduler via Docker...");

        final DockerClient docker = new JaxRs1Client();

        final HostConfig hostConfig = new HostConfig();
        hostConfig.setRestartPolicy(RestartPolicy.unlessStopped());

        final CreateContainerResponse newContainer = docker
                .createContainerCmd(image)
                .withName(id)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withHostConfig(hostConfig)
                .withCmd(args)
                .exec();

        final String newContainerId = newContainer.getId();

        docker.startContainerCmd(newContainerId).exec();

        LOG.info("launch: Scheduler launched via Docker...");
    }
}
