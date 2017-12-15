package net.es.oscars.soap;

import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EndpointConfig {
    @Bean
    public ServletRegistrationBean servletRegistrationBean(ApplicationContext context) {
        return new ServletRegistrationBean(new CXFServlet(), "/services/*");
    }
    @Bean(name= Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public Endpoint endpoint() {

        EndpointImpl provider = new EndpointImpl(springBus(), new NsiProvider());

        Map<String, Object> props = provider.getProperties();
        if (props == null) {
            props = new HashMap<>();
        }
        props.put("jaxb.additionalContextClasses",
                new Class[]{
                        net.es.nsi.lib.soap.gen.nsi_2_0.services.point2point.ObjectFactory.class
                });
        provider.setProperties(props);


        LoggingFeature lf = new LoggingFeature();
        lf.setPrettyLogging(true);
        provider.getFeatures().add(lf);


        provider.publish("/provider");
        return provider;
    }

}