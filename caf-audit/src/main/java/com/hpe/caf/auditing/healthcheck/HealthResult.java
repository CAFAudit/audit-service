package com.hpe.caf.auditing.healthcheck;

public final class HealthResult
{
    private final HealthStatus status;
    private final String message;

    public static final HealthResult HEALTHY = new HealthResult(HealthStatus.HEALTHY);

    public HealthResult(final HealthStatus status)
    {
        this.status = status;
        this.message = null;
    }

    public HealthResult(final HealthStatus status, final String message)
    {
        this.status = status;
        this.message = message;
    }

    public HealthStatus getStatus()
    {
        return status;
    }

    public String getMessage()
    {
        return message;
    }
}
