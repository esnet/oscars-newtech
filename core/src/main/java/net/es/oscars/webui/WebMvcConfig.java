package net.es.oscars.webui;

import net.es.oscars.webui.prop.WebuiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Autowired
    WebuiProperties props;

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/frontend/" };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        if (props.getDevMode()) {

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
        } else {

            if (!registry.hasMappingForPattern("/webjars/**")) {
                registry.addResourceHandler("/webjars/**")
                        .addResourceLocations("classpath:/META-INF/resources/webjars/")
                        .setCachePeriod(3600);
            }
            if (!registry.hasMappingForPattern("/frontend/**")) {
                registry.addResourceHandler("/frontend/**")
                        .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS)
                        .setCachePeriod(3600);

            }
        }

    }
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // forward requests to /admin and /user to their index.html
        registry.addViewController("/frontend/").setViewName("forward:/frontend/index.html");
    }
}