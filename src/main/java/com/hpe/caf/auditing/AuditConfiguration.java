package com.hpe.caf.auditing;

import com.hpe.caf.api.Encrypted;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * The Audit configuration class.
 */

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

    /**
     * Getter for property 'queueHost'.
     *
     * @return Value for property 'queueHost'.
     */
    public String getQueueHost() {
        return this.queueHost;
    }

    /**
     * Setter for property 'queueHost'.
     *
     * @param queueHost Value to set for property 'queueHost'.
     */
    public void setQueueHost(String queueHost) {
        this.queueHost = queueHost;
    }

    /**
     * Getter for property 'queuePort'.
     *
     * @return Value for property 'queuePort'.
     */
    public int getQueuePort() {
        return this.queuePort;
    }

    /**
     * Setter for property 'queuePort'.
     *
     * @param queuePort Value to set for property 'queuePort'.
     */
    public void setQueuePort(int queuePort) {
        this.queuePort = queuePort;
    }

    /**
     * Getter for property 'queueUser'.
     *
     * @return Value for property 'queueUser'.
     */
    public String getQueueUser() {
        return this.queueUser;
    }

    /**
     * Setter for property 'queueUser'.
     *
     * @param queueUser Value to set for property 'queueUser'.
     */
    public void setQueueUser(String queueUser) {
        this.queueUser = queueUser;
    }

    /**
     * Getter for property 'queuePassword'.
     *
     * @return Value for property 'queuePassword'.
     */
    public String getQueuePassword() {
        return this.queuePassword;
    }

    /**
     * Setter for property 'queuePassword'.
     *
     * @param queuePassword Value to set for property 'queuePassword'.
     */
    public void setQueuePassword(String queuePassword) {
        this.queuePassword = queuePassword;
    }
}
