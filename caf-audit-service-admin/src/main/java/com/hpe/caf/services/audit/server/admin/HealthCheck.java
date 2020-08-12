package com.hpe.caf.services.audit.server.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.auditing.exception.AuditConfigurationException;
import com.hpe.caf.auditing.healthcheck.HealthResult;
import com.hpe.caf.auditing.healthcheck.HealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class HealthCheck extends HttpServlet
{
    private static final Logger LOG = LoggerFactory.getLogger(HealthCheck.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        HealthResult healthResult;
        try {
            final AuditConnection connection = AuditConnectionFactory.createConnection();
             healthResult = connection.createChannel().healthCheck();
            if (healthResult.getStatus() != HealthStatus.HEALTHY) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (final AuditConfigurationException | JsonProcessingException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            healthResult = new HealthResult(HealthStatus.UNHEALTHY, e.getMessage());
            LOG.error("Failed to get health check - {}", e.getMessage());
        }

        final byte[] body = MAPPER.writeValueAsBytes(healthResult);
        resp.setContentLength(body.length);
        resp.setContentType("application/json");
        try (final ServletOutputStream out = resp.getOutputStream()) {
            out.write(body);
            out.flush();
        }
    }
}
