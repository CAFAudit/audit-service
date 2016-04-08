package com.hpe.caf.services.audit.api.generated.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-04-07T13:28:48.626Z")
public class NewTenant   {
  
  private String tenantId = null;
  private List<String> application = new ArrayList<String>();

  
  /**
   **/
  public NewTenant tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("tenantId")
  public String getTenantId() {
    return tenantId;
  }
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  
  /**
   **/
  public NewTenant application(List<String> application) {
    this.application = application;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("application")
  public List<String> getApplication() {
    return application;
  }
  public void setApplication(List<String> application) {
    this.application = application;
  }

  

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NewTenant newTenant = (NewTenant) o;
    return Objects.equals(tenantId, newTenant.tenantId) &&
        Objects.equals(application, newTenant.application);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantId, application);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NewTenant {\n");
    
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    application: ").append(toIndentedString(application)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

