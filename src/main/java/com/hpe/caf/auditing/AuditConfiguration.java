package com.hpe.caf.auditing;

import com.hpe.caf.api.Encrypted;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AuditConfiguration {

    /**
     * The host that runs the specified queue.
     */
    @NotNull
    @Size(min = 1)
    private String queueHost;

    /**
     * The port exposed on the host to access the queue by.
     */
    @Min(1024)
    @Max(65535)
    private int queuePort;


    /**
     * The username to access the queue server with.
     */
    @NotNull
    @Size(min = 1)
    private String queueUser;

    /**
     * The password to access the queue server with.
     */
    @Encrypted
    @NotNull
    @Size(min = 1)
    private String queuePassword;

    public AuditConfiguration() {
    }

    public String getQueueHost() {
        return this.queueHost;
    }

    public void setQueueHost(String queueHost) {
        this.queueHost = queueHost;
    }

    public int getQueuePort() {
        return this.queuePort;
    }

    public void setQueuePort(int queuePort) {
        this.queuePort = queuePort;
    }

    public String getQueueUser() {
        return this.queueUser;
    }

    public void setQueueUser(String queueUser) {
        this.queueUser = queueUser;
    }

    public String getQueuePassword() {
        return this.queuePassword;
    }

    public void setQueuePassword(String queuePassword) {
        this.queuePassword = queuePassword;
    }
}
