package com.hpe.caf.services.audit.api.generated;

import com.hpe.caf.services.audit.api.TenantAddPost;
import com.hpe.caf.services.audit.api.TenantUpdatePartitionsPost;
import com.hpe.caf.services.audit.api.exceptions.BadRequestException;
import com.hpe.caf.services.audit.api.exceptions.NotFoundException;
import com.hpe.caf.services.audit.api.generated.model.NewTenant;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-02-09T14:45:42.907Z")
public class TenantsApiServiceImpl extends TenantsApiService {

    @Override
    public Response tenantsPost(NewTenant newTenant, SecurityContext securityContext) throws Exception {

            TenantAddPost.addTenant(newTenant);
            return Response.ok().build();
    }

    @Override
    public Response tenantsUpdatePartitionCountPost(String tenantId, String applicationId, SecurityContext securityContext) throws Exception{

//        return Response.ok().entity(new ApiSuccessResponseMessage(ApiSuccessResponseMessage.OK, "magic!")).build();
        try {
            int partitionsAdded = TenantUpdatePartitionsPost.checkAndUpdatePartitions(tenantId, applicationId);
//            return Response.ok().entity(new ApiResponseMessage(partitionsAdded, "Success")).build();
            return Response.ok().build();
        } catch(NotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(new ApiResponseMessage(e.getMessage())).build();
        } catch(BadRequestException e){
            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(e.getMessage())).build();
        } catch (Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ApiResponseMessage(e.getMessage())).build();
        }
    }
}
