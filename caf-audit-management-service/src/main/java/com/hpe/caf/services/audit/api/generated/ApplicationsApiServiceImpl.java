package com.hpe.caf.services.audit.api.generated;

import com.hpe.caf.services.audit.api.ApplicationAddPost;
import com.hpe.caf.services.audit.api.exceptions.BadRequestException;

import javax.ws.rs.core.Response;
import java.io.InputStream;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-02-02T09:59:08.217Z")
public class ApplicationsApiServiceImpl extends ApplicationsApiService {

// CM - original generated code
//      @Override
//      public Response applicationsPost(FormDataContentDisposition fileDetail,SecurityContext securityContext)
//      throws NotFoundException {
//      // do some magic!
//      return Response.ok().entity(new ApiSuccessResponseMessage(ApiSuccessResponseMessage.OK, "magic!")).build();
//  }

    @Override
    public Response applicationAddPost(InputStream inputStream)
            throws Exception {

        try {
            ApplicationAddPost.addApplication(inputStream);
            return Response.status(Response.Status.OK).entity(new ApiResponseMessage("Success")).build();
        } catch (BadRequestException e){
            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(e.getMessage())).build();
        } catch (Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ApiResponseMessage(e.getMessage())).build();
        }
    }
}
