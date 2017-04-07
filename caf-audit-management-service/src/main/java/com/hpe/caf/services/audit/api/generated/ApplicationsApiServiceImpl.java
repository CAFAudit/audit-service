/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
