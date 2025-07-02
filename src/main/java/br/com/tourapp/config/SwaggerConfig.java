package br.com.tourapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:Tour Management API for organizing and managing travel tours}")
    private String appDescription;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI tourApplicationOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .tags(tagList())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    private Info apiInfo() {
        return new Info()
                .title("Tour Application API")
                .description(appDescription)
                .version(appVersion)
                .contact(new Contact()
                        .name("Tour App Development Team")
                        .email("dev@tourapp.com")
                        .url("https://github.com/tourapp/tour-application"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"))
                .termsOfService("https://tourapp.com/terms");
    }

    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Development Server"),
                new Server()
                        .url("https://api.tourapp.com")
                        .description("Production Server"),
                new Server()
                        .url("https://staging-api.tourapp.com")
                        .description("Staging Server")
        );
    }

    private List<Tag> tagList() {
        return List.of(
                new Tag()
                        .name("Tours")
                        .description("Tour management operations - Create, read, update and delete tours"),
                new Tag()
                        .name("Search")
                        .description("Tour search and filtering operations"),
                new Tag()
                        .name("Analytics")
                        .description("Tour analytics and reporting endpoints")
        );
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer Token Authentication"))
                .addSecuritySchemes("apiKey", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                        .description("API Key Authentication"));
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList("bearerAuth")
                .addList("apiKey");
    }
}