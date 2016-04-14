package com.hpe.caf.services.audit.api;

import com.hpe.caf.services.audit.api.exceptions.BadRequestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * The ApiExceptionMapper class maps exceptions thrown by the audit management api
 * to response http status codes.
 */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        //  Default response to HTTP 500 (i.e. INTERNAL SERVER ERROR)
        Response.Status httpStatus = Response.Status.INTERNAL_SERVER_ERROR;

        //  Map BadRequestExceptions thrown to HTTP 400 (i.e. BAD REQUEST)
        if (exception instanceof BadRequestException)
            httpStatus = Response.Status.BAD_REQUEST;

        //  Include exception message in response.
        return Response.status(httpStatus).entity(exception.getMessage())
                .build();
    }
}
