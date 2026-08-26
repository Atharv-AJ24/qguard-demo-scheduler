package com.trevasq.qguard.config;
import org.springframework.context.annotation.Configuration; import org.springframework.web.servlet.config.annotation.*;
@Configuration public class WebConfig implements WebMvcConfigurer { private final AppProperties properties; public WebConfig(AppProperties properties){this.properties=properties;} @Override public void addCorsMappings(CorsRegistry r){r.addMapping("/api/**").allowedOrigins(properties.publicBaseUrl()).allowedMethods("GET","POST","OPTIONS").allowedHeaders("Content-Type");} }
