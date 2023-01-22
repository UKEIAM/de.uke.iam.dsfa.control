package de.uke.iam.dsfa.control.rest;

import de.uke.iam.dsfa.control.model.ExcelReaderResponse;
import de.uke.iam.dsfa.control.util.ExcelReader;
import io.swagger.annotations.*;
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
@Api(value = "Admin")
@SwaggerDefinition(tags = {@Tag(name = "Admin Service", description = "REST Endpoint Service for Admin")})
public class AdminRestService {

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
