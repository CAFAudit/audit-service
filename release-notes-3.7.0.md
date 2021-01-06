
#### Version Number
${version-number}

#### New Features
* SCMOD-9181: Updated JRE 11 base image  
    The JRE 11 base image has been updated to support a generic SSL cert directory. The `MESOS_SANDBOX` variable has been replaced with `SSL_CA_CRT_DIR` in order to maintain backward compatibility (ex: for use on Kubernetes).
    
* SCMOD-5156: Use fully-qualified DockerHub images  
    DockerHub image names have been updated so that images are pulled from our DockerHub proxy.
    
* SCMOD-11768: Explicitly specify base image
    Explicitly specify the busybox base image so that it is pulled from our DockerHub proxy rather than `docker.io`.

#### Known Issues

- None

