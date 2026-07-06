package com.codementor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI codeMentorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CodeMentor AI API")
                        .description(
                                "AI-powered Java code evaluation and interview preparation system. " +
                                        "Submit Java code and receive detailed analysis including " +
                                        "time/space complexity, optimization suggestions, best practices, " +
                                        "and interview questions."
                        )
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CodeMentor")
                                .email("support@codementor.dev"))
                        .license(new License()
                                .name("MIT License")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local Development Server")
                ))
                // Add JWT bearer authentication to Swagger UI
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token here")));
    }
}