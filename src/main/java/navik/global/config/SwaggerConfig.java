package navik.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Creates the OpenAPI specification configured with API metadata and global JWT bearer authentication.
     *
     * Configures a SecurityScheme named "JWT Authentication" (HTTP bearer, bearerFormat "JWT"), registers it in Components,
     * and applies a SecurityRequirement referencing that scheme to the OpenAPI instance.
     *
     * @return an OpenAPI instance populated with API info, the JWT bearer SecurityRequirement, and the corresponding Components
     */
    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "JWT Authentication";

        // API 요청헤더에 인증정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // SecuritySchemes 등록
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")
                        .bearerFormat("JWT")); // 토큰 형식을 지정하는 임의의 문자열(Optional)

        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    /**
     * Provides the OpenAPI metadata for the application (title, description, and version).
     *
     * @return an Info instance containing the API title "Navik API Documentation", the description "Navik API 명세서입니다.", and the version "1.0.0".
     */
    private Info apiInfo() {
        return new Info()
                .title("Navik API Documentation")
                .description("Navik API 명세서입니다.")
                .version("1.0.0");
    }
}