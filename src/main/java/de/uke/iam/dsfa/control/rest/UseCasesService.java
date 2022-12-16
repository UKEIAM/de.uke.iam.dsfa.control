package de.uke.iam.dsfa.control.rest;

import de.uke.iam.dsfa.control.db.DatabaseUtil;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.UseCase;
import de.uke.iam.dsfa.control.db.DatabaseConfiguration;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/usecases")
public class UseCasesService {
  private Logger logger = LoggerFactory.getLogger(UseCasesService.class);


  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(produces="application/json", value="Get all UseCases", httpMethod="GET", response=UseCase.class)
  @ApiResponses(value = {
      @ApiResponse(code=200, response=UseCase.class, message="Successful operation"),
      @ApiResponse(code=404, message="UseCases not found")
  })
  public Response get(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
    DSLContext dsl = DatabaseConfiguration.get().getDsl();

    List<UseCase> listOfUseCases = DatabaseUtil.selectAllUseCases(dsl);
    logger.debug("UseCases: {}", listOfUseCases);

    return Response.ok(listOfUseCases).build();
  }
}
