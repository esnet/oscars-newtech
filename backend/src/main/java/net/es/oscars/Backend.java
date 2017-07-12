package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
@Slf4j
public class Backend {
    public static void main(String[] args) {
        ConfigurableApplicationContext app = SpringApplication.run(Backend.class, args);
        Startup startup = (Startup) app.getBean("startup");

        try {
            startup.onStart();
        } catch (Exception ex) {
            log.error("startup error!", ex);
            System.exit(1);
        }
    }
}
