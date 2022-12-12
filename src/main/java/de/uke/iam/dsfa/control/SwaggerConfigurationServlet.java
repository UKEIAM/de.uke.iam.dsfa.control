package de.uke.iam.dsfa.control;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class SwaggerConfigurationServlet extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    Info info = new Info()
        .title("REST API")
        .version("1.0.0");
    Swagger swagger = new Swagger().info(info);
    swagger.tag(new Tag()
        .name("api")
        .description("Access to REST API"));

    new SwaggerContextService()
        .withBasePath("/api")
        .withServletConfig(config)
        .updateSwagger(swagger);
  }
}