package com.commerceos.iam.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
    info =
        @Info(
            title = "CommerceOS API",
            version = "0.1.0",
            description =
                "AI-assisted commerce operations platform. Manages inventory, "
                    + "suppliers, purchase recommendations, procurement workflows, "
                    + "and analytics.",
            contact = @Contact(name = "CommerceOS Team", email = "dev@commerceos.com"),
            license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")),
    servers = {
      @Server(url = "http://localhost:8080", description = "Local development"),
    },
    security = {@SecurityRequirement(name = "bearer-jwt")})
@SecurityScheme(
    name = "bearer-jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "JWT access token received from POST /api/v1/auth/login")
public class OpenApiConfig {}
