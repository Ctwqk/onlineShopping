package com.taiwei.reggie.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves the two bundled UIs ({@code /backend/**} admin console, {@code /front/**} mobile front).
 * Implements {@link WebMvcConfigurer} instead of extending {@code WebMvcConfigurationSupport} so that
 * Spring Boot's MVC auto-configuration (Jackson, error handling, springdoc) stays active.
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }
}
