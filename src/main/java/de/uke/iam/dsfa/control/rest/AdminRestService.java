package de.uke.iam.dsfa.control.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uke.iam.dsfa.control.model.ExcelReaderResponse;
import de.uke.iam.dsfa.control.util.ExcelReader;
import io.swagger.annotations.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;

@Path("/admin")
@Api(value = "Admin")
@SwaggerDefinition(tags = {@Tag(name = "Admin Service", description = "REST Endpoint Service for Admin")})
public class AdminRestService {

    @Path("/uploadexcel")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(consumes="multipart/form-data", value = "Upload an Excel file and update the Database with the new " +
            "Information from this file", httpMethod="PUT", response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Response.class, message = "Successful operation"),
            @ApiResponse(code = 404, message = "Server Not found")
    })
    public Response uploadExcel(@FormDataParam("file") InputStream fileInputStream,
                                @FormDataParam("file") FormDataContentDisposition fileFormDataContentDisposition) throws IOException {
        try {
            ExcelReader.readFile(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ExcelReaderResponse excelReaderResponse = ExcelReader.getResponse();
            ObjectMapper mapper = new ObjectMapper();
            String response = mapper.writeValueAsString(excelReaderResponse);
            return Response.status(Response.Status.OK).entity(response).build();
        }

    }

}
