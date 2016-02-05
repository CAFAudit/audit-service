package com.hpe.caf.services.audit.api.generated;

import com.hpe.caf.services.audit.api.ApiException;
import com.hpe.caf.services.audit.api.ApplicationAddPost;
import com.hpe.caf.services.audit.api.generated.ApplicationsApiService;

import javax.ws.rs.core.Response;
import java.io.InputStream;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-02-02T09:59:08.217Z")
public class ApplicationsApiServiceImpl extends ApplicationsApiService {

// CM - original generated code
//      @Override
//      public Response applicationsPost(FormDataContentDisposition fileDetail,SecurityContext securityContext)
//      throws NotFoundException {
//      // do some magic!
//      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
//  }

    @Override
    public Response applicationAddPost(InputStream inputStream)
            throws Exception {

        ApplicationAddPost.addApplication(inputStream);
        return Response.ok().build();
    }
}
