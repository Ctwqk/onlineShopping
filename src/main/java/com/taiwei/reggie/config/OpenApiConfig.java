package com.taiwei.reggie.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** API documentation: Swagger UI at /swagger-ui.html, Knife4j UI at /doc.html. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI onlineShoppingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("onlineShopping")
                .version("1.0")
                .description("REST API behind the admin console (/backend) and the mobile customer front (/front)."));
    }
}
