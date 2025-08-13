package com.backend.gogreen.common.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@OpenAPIDefinition(
//        servers = {@Server(url = "${swagger.prod-url}", desciption = "운영 서버"),
//                    @Server(url = "${swagger.dev-url}", desciption = "개발 서버")
//        })
@Configuration
public class SwaggerConfig {

    @Value("${jwt.access.header}")
    private String accessTokenHeader;

    @Value("${jwt.refresh.header}")
    private String refreshTokenHeader;

    @Bean
    public OpenAPI openAPI() {

        // Access Token Bearer 인증 스키마 설정
        SecurityScheme accessTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name(accessTokenHeader);

        // Refresh Token Bearer 인증 스키마 설정
        SecurityScheme refreshTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(refreshTokenHeader);

        SecurityRequirement accessTokenRequirement = new SecurityRequirement().addList(accessTokenHeader);
        SecurityRequirement refreshTokenRequirement = new SecurityRequirement().addList(refreshTokenHeader);

        Server server = new Server()
                .url("http://localhost:8080");

        return new OpenAPI()
                .info(new Info()
                        .title("GoGreen")
                        .description("GoGreen REST API Document")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(accessTokenHeader, accessTokenScheme)
                        .addSecuritySchemes(refreshTokenHeader, refreshTokenScheme))
                .addServersItem(server)
                .addSecurityItem(accessTokenRequirement)
                .addSecurityItem(refreshTokenRequirement);
    }
}
