package com.hpe.caf.services.audit.api.generated;

import com.hpe.caf.services.audit.api.generated.model.NewTenant;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/tenants")


@Produces({ "application/json" })
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


    @POST



    @Path("/{tenantId}/updatePartitionCount")
    @io.swagger.annotations.ApiOperation(value = "Keeps Vertica topic configuration and Kafka topic partitions consistent", notes = "Used to check the number of partitions registered with a topic in Vertica and equate this with the actual number of partitions in Kafka.", response = void.class, tags={ "Tenants" })
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request (missing database connection properties or the `tenantId` parameter contains invalid characters)", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not found (`tenantId` or `applicationId` not found)", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = void.class) })

    public Response tenantsTenantIdUpdatePartitionCountPost(
            @ApiParam(value = "Identifies the tenant.",required=true) @PathParam("tenantId") String tenantId,
            @ApiParam(value = "Identifies the application.",required=true) @QueryParam("applicationId") String applicationId,
            @Context SecurityContext securityContext)
            throws Exception {
        return delegate.tenantsUpdatePartitionCountPost(tenantId,applicationId,securityContext);
    }
}
