package net.es.oscars.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/frontend/" };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // set caching
        if (!registry.hasMappingForPattern("/webjars/**")) {
            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/")
                    .setCachePeriod(60);
        }
        if (!registry.hasMappingForPattern("/frontend/**")) {
            registry.addResourceHandler("/frontend/**")
                    .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS)
                    .setCachePeriod(3600);

        }

        registry.addResourceHandler("/documentation/swagger-ui.html**")
                .addResourceLocations("classpath:/META-INF/resources/swagger-ui.html");
        registry.addResourceHandler("/documentation/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

    }
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/frontend/index.html");
        registry.addViewController("/pages/**").setViewName("forward:/frontend/index.html");

        registry.addRedirectViewController("/documentation/v2/api-docs", "/v2/api-docs");
        registry.addRedirectViewController("/documentation/swagger-resources/configuration/ui","/swagger-resources/configuration/ui");
        registry.addRedirectViewController("/documentation/swagger-resources/configuration/security","/swagger-resources/configuration/security");
        registry.addRedirectViewController("/documentation/swagger-resources", "/swagger-resources");
    }
}