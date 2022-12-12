package de.uke.iam.dsfa.control.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST class to respond to all /api/version URI calls
 */
@Path("/version")
@Api(value = "Version")
@SwaggerDefinition(tags = {@Tag(name = "Version Service", description = "REST Endpoint for Version Service")})
public class VersionRestService {

  private String getVersionNumber() {
    return getClass().getPackage().getImplementationVersion();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(produces="application/json", value = "Gets the version of this application", httpMethod="GET", response = Map.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, response = Map.class, message = "Successful operation"),
      @ApiResponse(code = 404, message = "DD not found")
  })
  public Map<String, String> versionJSON() {
    Map<String, String> versionMap = new HashMap<>();
    versionMap.put("version", getVersionNumber());
    return versionMap;
  }
}
