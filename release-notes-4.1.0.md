#### Version Number
${version-number}

#### New Features
- US914145: New liveness and readiness endpoints added.
  - A new `/health-check?name=all&type=ALIVE` endpoint has been added on the default REST port (8080) to return the result of the last
    liveness check.
  - A new `/health-check?name=all&type=READY` endpoint has been added on the default REST port (8080) to return the result of the last
    readiness check.
  - The liveness and readiness checks are run on a schedule, which can be configured by the environment variables described in the
    [README.md](https://github.com/CAFAudit/audit-service/tree/develop/caf-audit-service-container#configuration).

#### Known Issues
- None
