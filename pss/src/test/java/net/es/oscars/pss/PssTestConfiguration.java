package net.es.oscars.pss;


import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"net.es.oscars.rest", "net.es.oscars.pss"})
@EnableAutoConfiguration
public class PssTestConfiguration {
}