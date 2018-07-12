package net.es.oscars.soap;


import lombok.extern.slf4j.Slf4j;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort;
import net.es.oscars.app.props.NsiProperties;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ClientUtil {

    @Autowired
    private NsiProperties props;

    final public static String DEFAULT_REQUESTER = "urn:oscars:nsa:client";
    final public static String DEFAULT_PROVIDER = DEFAULT_REQUESTER;


    HashMap<String, ConnectionRequesterPort> requesterPorts = new HashMap<String, ConnectionRequesterPort>();


    /**
     * Creates a client for interacting with an NSA requester
     *
     * @param url the URL of the requester to contact
     * @return the ConnectionRequesterPort that you can use at the client
     */
    public ConnectionRequesterPort createRequesterClient(String url) {
        prepareBus(url);

        JaxWsProxyFactoryBean fb = new JaxWsProxyFactoryBean();
        LoggingFeature lf = new LoggingFeature();
        lf.setPrettyLogging(true);
        fb.getFeatures().add(lf);

        Map<String, Object> props = fb.getProperties();
        if (props == null) {
            props = new HashMap<>();
        }
        props.put("jaxb.additionalContextClasses",
                new Class[]{
                        net.es.nsi.lib.soap.gen.nsi_2_0.services.point2point.ObjectFactory.class
                });
        fb.setProperties(props);

        fb.setAddress(url);


        // logging
        /*
        LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
        loggingInInterceptor.setPrettyLogging(true);
        LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
        loggingOutInterceptor.setPrettyLogging(true);

        fb.getInInterceptors().add(loggingInInterceptor);
        fb.getInFaultInterceptors().add(loggingInInterceptor);
        fb.getOutInterceptors().add(loggingOutInterceptor);
        fb.getOutFaultInterceptors().add(loggingOutInterceptor);
        */

        fb.setServiceClass(ConnectionRequesterPort.class);

        return (ConnectionRequesterPort) fb.create();

    }


    /**
     * Configures SSL and other basic client settings
     *
     * @param urlString the URL of the server to contact
     */
    private void prepareBus(String urlString) {

        // TODO: fix these

        // System.setProperty("javax.net.ssl.trustStore", "DoNotUsecacerts");


    }


}
