package net.es.oscars.app.ser;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class JacksonModules {
    @Bean
    Module dateTimeModule() {
      return new JavaTimeModule();
    }

}
