package com.hpe.caf.services.audit.api.generated;

import java.util.List;

import com.hpe.caf.services.audit.api.generated.model.NewTenant;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/tenants")


@io.swagger.annotations.Api(description = "the tenants API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-02-09T14:45:42.907Z")
public class TenantsApi  {
    private final TenantsApiService delegate = TenantsApiServiceFactory.getTenantsApi();

    @POST



    @io.swagger.annotations.ApiOperation(value = "Adds a new tenant", notes = "Used to register and create the Vertica database schema for a new tenant.", response = void.class, tags={ "Tenants" })
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request (missing database connection properties or the `tenantId` parameter contains invalid characters)", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = void.class) })
    public Response tenantsPost(
            @ApiParam(value = "Identifies the tenant and the application(s) that the tenant is to be registered with." ,required=true) NewTenant newTenant,
            @Context SecurityContext securityContext)
            throws Exception {
        return delegate.tenantsPost(newTenant,securityContext);
    }
}
