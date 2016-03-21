package com.hpe.caf.daemon;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.client.model.HostConfig;
import com.github.dockerjava.client.model.RestartPolicy;
import com.github.dockerjava.jaxrs1.JaxRs1Client;

public final class DockerDaemonLauncher implements DaemonLauncher {

    @Override
    public void launch(
        final String id,
        final String image,
        final String[] args
    ) {
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
    }
}
