package com.hpe.caf.services.audit.server.dropwizard;

import jakarta.validation.constraints.Min;

import java.util.Optional;

public final class HealthConfiguration
{
    @Min(0)
    private final int livenessInitialDelaySeconds;

    @Min(1)
    private final int livenessCheckIntervalSeconds;

    @Min(1)
    private final int livenessDowntimeIntervalSeconds;

    @Min(1)
    private final int livenessSuccessAttempts;

    @Min(1)
    private final int livenessFailureAttempts;

    @Min(0)
    private final int readinessInitialDelaySeconds;

    @Min(1)
    private final int readinessCheckIntervalSeconds;

    @Min(1)
    private final int readinessDowntimeIntervalSeconds;

    @Min(1)
    private final int readinessSuccessAttempts;

    @Min(1)
    private final int readinessFailureAttempts;

    public HealthConfiguration() {
        this.livenessInitialDelaySeconds = getEnvVar("CAF_AUDIT_SERVICE_LIVENESS_INITIAL_DELAY_SECONDS", 15);
        this.livenessCheckIntervalSeconds = getEnvVar("CAF_AUDIT_SERVICE_LIVENESS_CHECK_INTERVAL_SECONDS", 60);
        this.livenessDowntimeIntervalSeconds = getEnvVar("CAF_AUDIT_SERVICE_LIVENESS_DOWNTIME_INTERVAL_SECONDS", 60);
        this.livenessSuccessAttempts = getEnvVar("CAF_AUDIT_SERVICE_LIVENESS_SUCCESS_ATTEMPTS", 1);
        this.livenessFailureAttempts = getEnvVar("CAF_AUDIT_SERVICE_LIVENESS_FAILURE_ATTEMPTS", 3);
        this.readinessInitialDelaySeconds = getEnvVar("CAF_AUDIT_SERVICE_READINESS_INITIAL_DELAY_SECONDS", 15);
        this.readinessCheckIntervalSeconds = getEnvVar("CAF_AUDIT_SERVICE_READINESS_CHECK_INTERVAL_SECONDS", 60);
        this.readinessDowntimeIntervalSeconds = getEnvVar("CAF_AUDIT_SERVICE_READINESS_DOWNTIME_INTERVAL_SECONDS", 60);
        this.readinessSuccessAttempts = getEnvVar("CAF_AUDIT_SERVICE_READINESS_SUCCESS_ATTEMPTS", 1);
        this.readinessFailureAttempts = getEnvVar("CAF_AUDIT_SERVICE_READINESS_FAILURE_ATTEMPTS", 3);
    }

    public int getLivenessInitialDelaySeconds()
    {
        return livenessInitialDelaySeconds;
    }

    public int getLivenessCheckIntervalSeconds()
    {
        return livenessCheckIntervalSeconds;
    }

    public int getLivenessDowntimeIntervalSeconds()
    {
        return livenessDowntimeIntervalSeconds;
    }

    public int getLivenessSuccessAttempts()
    {
        return livenessSuccessAttempts;
    }


    public int getLivenessFailureAttempts()
    {
        return livenessFailureAttempts;
    }

    public int getReadinessInitialDelaySeconds()
    {
        return readinessInitialDelaySeconds;
    }

    public int getReadinessCheckIntervalSeconds()
    {
        return readinessCheckIntervalSeconds;
    }

    public int getReadinessDowntimeIntervalSeconds()
    {
        return readinessDowntimeIntervalSeconds;
    }

    public int getReadinessSuccessAttempts()
    {
        return readinessSuccessAttempts;
    }

    public int getReadinessFailureAttempts()
    {
        return readinessFailureAttempts;
    }

    private static int getEnvVar(String envVarName, int defaultValue)
    {
        return Optional.ofNullable(System.getenv(envVarName))
                .map(Integer::parseInt)
                .orElse(defaultValue);
    }
}
