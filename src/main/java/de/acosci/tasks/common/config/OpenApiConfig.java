package de.acosci.tasks.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Cards API")
                        .version("v1")
                        .description("REST-API für Tasks, Projekte und Benutzerverwaltung"));
    }
}