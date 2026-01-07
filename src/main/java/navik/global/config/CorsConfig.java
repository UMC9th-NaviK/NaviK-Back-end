package navik.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// org.springframework.web.cors.reactive 는 WebFlux

import java.util.List;

@Configuration
public class CorsConfig {
    /**
     * Creates a CorsConfigurationSource that enables cross-origin requests from https://your-frontend.com,
     * allows credentials, permits GET/POST/PUT/DELETE methods, and accepts all request headers for all paths.
     *
     * @return a CorsConfigurationSource configured with the described CORS policy and registered for "/**"
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://your-frontend.com"));
        config.setAllowCredentials(true);  // 쿠키 전송 허용
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}