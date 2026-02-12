package ca.etsmtl.taf.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "TAF - Test Automation Framework API",
        version = "1.0",
        description = "API pour le framework d'automatisation de tests (TAF). "
            + "Utilisez /api/auth/signin pour obtenir un token JWT, "
            + "puis cliquez sur 'Authorize' et entrez: Bearer <votre_token>"
    ),
    servers = {
        @Server(url = "http://localhost:8084", description = "Local Docker (team2)"),
        @Server(url = "http://localhost:8080", description = "Via Gateway"),
        @Server(url = "/", description = "Relative (current host)")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Entrez le token JWT obtenu via /api/auth/signin"
)
public class OpenApiConfig {
}
