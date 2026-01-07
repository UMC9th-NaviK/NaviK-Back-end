package navik.global.config;

import navik.auth.handler.AuthUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthUserArgumentResolver authUserArgumentResolver;

    /**
     * Registers custom argument resolvers with the given list so Spring MVC can resolve controller method arguments.
     *
     * Adds the injected AuthUserArgumentResolver to the provided list of HandlerMethodArgumentResolver instances.
     *
     * @param resolvers the mutable list of argument resolvers to register custom resolvers with
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserArgumentResolver);
    }
}