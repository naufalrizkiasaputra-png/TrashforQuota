package com.trashforquota.ProjectPBO.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Memetakan URL /img/uploads/** ke folder fisik di project
        registry.addResourceHandler("/img/uploads/**")
                .addResourceLocations("file:src/main/resources/static/img/uploads/");
    }
}