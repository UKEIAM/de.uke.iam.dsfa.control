package de.uke.iam.dsfa.control.rest;

import de.uke.iam.dsfa.control.util.WordWriter;
import io.swagger.annotations.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Path("/dsfa")
@Api(value = "DSFA")
@SwaggerDefinition(tags = {@Tag(name = "DSFA Service", description = "REST Endpoint for DSFA Download Service")})
public class DSAFDownloadRestService {

    @GET
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(produces=MediaType.MULTIPART_FORM_DATA, value = "Gets the version of this application", httpMethod="GET", response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Response.class, message = "Successful operation"),
            @ApiResponse(code = 404, message = "DD not found")
    })
    public Response downloadDSFA(@QueryParam("usecase") final List<Integer> useCaseIDs) throws IOException {

        XWPFDocument dsfaDoc = WordWriter.getWord(useCaseIDs);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        dsfaDoc.write(out);
        out.close();
        dsfaDoc.close();

        byte[] xwpfDocumentBytes = out.toByteArray();

        return Response.ok(xwpfDocumentBytes, MediaType.MULTIPART_FORM_DATA)
                .header("Content-Disposition", "attachment; filename=")
                .build();
    }
}
