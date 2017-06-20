package net.es.oscars.pss;

import net.es.oscars.pss.beans.UrnMappingException;
import net.es.oscars.pss.go.Startup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
public class PssApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext app = SpringApplication.run(PssApp.class, args);

        Startup startup = (Startup)app.getBean("startup");
        try {
            startup.onStart();
        } catch (UrnMappingException | IOException ex) {
            System.err.print("Startup error");
            ex.printStackTrace();
            System.exit(1);
        }



    }

}
