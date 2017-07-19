package net.es.oscars.app.ser;

import com.xebia.jacksonlombok.JacksonLombokAnnotationIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@Slf4j
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addLombokIntrospection() {
        return new Jackson2ObjectMapperBuilderCustomizer() {
            @Override
            public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
                log.info("customizing jackson");
                jacksonObjectMapperBuilder.annotationIntrospector(new JacksonLombokAnnotationIntrospector());
                jacksonObjectMapperBuilder.failOnUnknownProperties(true);
                jacksonObjectMapperBuilder.modules(new JavaTimeModule());
            }

        };
    }
}
