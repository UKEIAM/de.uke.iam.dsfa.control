package de.uke.iam.dsfa.control.rest;

import de.uke.iam.dsfa.control.model.ExcelReaderResponse;
import de.uke.iam.dsfa.control.util.ExcelReader;
import io.swagger.annotations.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;

@Path("/admin")
@Api(value = "admin")
@SwaggerDefinition(tags = {@Tag(name = "Admin Service", description = "REST Endpoint Service for Admin")})
public class AdminRestService {

    private String[] basicAuthDecoder(String authHeader){
        byte[] decodedBytes = Base64.getDecoder().decode(authHeader.split(" ")[1]);
        String pair = new String(decodedBytes, StandardCharsets.UTF_8);
        return pair.split(":");
    }

    @Path("/login")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(consumes=MediaType.APPLICATION_JSON, value = "Check if the user is admin", httpMethod="PUT", response = Response.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = Response.class, message = "Successful operation"),
        @ApiResponse(code = 401, message = "Unauthorized - The provided credentials are unknown"),
    })
    public Response checkIfAdmin(@Context HttpHeaders headers) throws ConfigurationException {
        // get username and password from config file
        Configurations configs = new Configurations();
        XMLConfiguration config = configs.xml("dsfa.control.config.xml");
        String adminName = config.getString("admin.username");
        String adminPassword = config.getString("admin.password");
        // get username and password from header
        String authHeader = headers.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String[] credentials = basicAuthDecoder(authHeader);
        String username = credentials[0];
        String password = credentials[1];
        // compare
        if(adminName.equals(username) && adminPassword.equals(password)){
            return Response.ok().build();
        }
        return Response.status(Status.UNAUTHORIZED).build();
    }

    @Path("/uploadexcel")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(consumes="multipart/form-data", value = "Upload an Excel file and update the Database with the new " +
            "Information from this file", httpMethod="PUT", response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Response.class, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Bad Request - The structure of the excel file is not as expected"),
            @ApiResponse(code = 404, message = "Server Not found"),
            @ApiResponse(code = 415, message = "Unsupported Media Type - Please send an excel file")
    })
    public Response uploadExcel(@FormDataParam("file") InputStream fileInputStream,
                                @FormDataParam("file") FormDataContentDisposition fileFormDataContentDisposition) {
        try {
            ExcelReader.readFile(fileInputStream);
            List<String> errors = ExcelReader.getErrorLines();
            if(errors.isEmpty()){
                return Response.status(Response.Status.OK).entity("Excel file uploaded without errors").build();
            } else {
                ExcelReaderResponse response = new ExcelReaderResponse();
                response.setComments(errors);
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }
        } catch (IOException | NotOfficeXmlFileException e) {
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity("Error by reading file").build();
        }
    }

}
