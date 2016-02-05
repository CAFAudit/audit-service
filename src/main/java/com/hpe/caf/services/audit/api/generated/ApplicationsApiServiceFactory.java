package com.hpe.caf.services.audit.api.generated;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-02-02T09:59:08.217Z")
public class ApplicationsApiServiceFactory {

   private final static ApplicationsApiService service = new ApplicationsApiServiceImpl();

   public static ApplicationsApiService getApplicationsApi()
   {
      return service;
   }
}
