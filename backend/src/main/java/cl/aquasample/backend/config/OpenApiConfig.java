package cl.aquasample.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aquasampleOpenApi() {
        return new OpenAPI().info(new Info()
                .title("AquaSample API")
                .version("1.0.0")
                .description("API REST para el registro, consulta y validación de muestras de choritos de ALDEMAR SpA.")
                .license(new License().name("Uso interno")));
    }
}
