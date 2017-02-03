package com.hpe.caf.services.audit.api.generated;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;

@Path("/applications")


@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the applications API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-02-02T09:59:08.217Z")
public class ApplicationsApi  {
   private final ApplicationsApiService delegate = ApplicationsApiServiceFactory.getApplicationsApi();

    @POST
    
    @Consumes({ "multipart/form-data" })
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Used to create the Vertica database schema for an application.", response = Void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad request", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = Void.class) })

// CM - original generated code
//    public Response applicationsPost(  @FormDataParam("file") InputStream inputStream,
//      @FormDataParam("file") FormDataContentDisposition fileDetail,@Context SecurityContext securityContext)
//    throws NotFoundException {
//        return delegate.applicationsPost(fileDetail,securityContext);
//    }

    public Response applicationAddPost(  @FormDataParam("file") InputStream inputStream,
                                         @FormDataParam("file") FormDataContentDisposition fileDetail,@Context SecurityContext securityContext)
            throws Exception {

        return delegate.applicationAddPost(inputStream);
    }

}
