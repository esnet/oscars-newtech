package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.app.Startup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
@ComponentScan({"net.es.oscars.pss","net.es.oscars.rest"})
@Slf4j
public class PssApp {
    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext app = SpringApplication.run(PssApp.class, args);
        Startup startup = (Startup) app.getBean("startup");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                startup.setInStartup(false);
                startup.setInShutdown(true);
            }
        });

        try {
            startup.onStart();
        } catch (Exception ex) {
            log.error("PSS startup error!", ex);
            System.exit(1);
        }
    }

    @Configuration
    @Profile("test")
    @ComponentScan(lazyInit = true)
    static class LocalConfig {
    }
}
