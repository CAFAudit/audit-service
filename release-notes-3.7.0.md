
#### Version Number
${version-number}

#### New Features
- **SCMOD-9181**: updated jre11 to support generic SSL cert dir. The "MESOS_SANDBOX" variable has been replaced with "SSL_CA_CRT_DIR" in order to maintain backward compatibility (ex: for use on Kubernetes)

- **SCMOD-5156**: Fully-qualified DockerHub images. DockerHub security: Abandon the generic hpeemployee account as we have the internal DockerHub proxy on dockerhub-private.svsartifactory.swinfra.net.
  
- **SCMOD-11069**: Updated to use the latest release of base image (opensuse-jre11-3.1.0-SNAPSHOT). Release the whole hierarchy of base images and update the services that are currently using pre-release versions of the base images to use the new released versions of the base images.
  
- **SCMOD-11768**: Explicitly specify base image. This will help in finding the relevant images when required.

#### Known Issues

- None

