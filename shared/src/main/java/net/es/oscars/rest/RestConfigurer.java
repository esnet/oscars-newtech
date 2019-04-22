package net.es.oscars.rest;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.naming.ConfigurationException;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Configuration
public class RestConfigurer {

    @Bean
    public ClientHttpRequestFactory getRestConfig(RestProperties restProperties) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, ConfigurationException, KeyManagementException {
        if (restProperties == null) {
            throw new ConfigurationException("no rest properties set!");
        }
        if (restProperties.getInternalUsername() == null) {
            throw new ConfigurationException("no rest.internal-username property set ");
        }
        if (restProperties.getInternalPassword() == null) {
            throw new ConfigurationException("no rest.internal-password property set ");
        }
        if (restProperties.getInternalTruststorePath() == null) {
            throw new ConfigurationException("no rest.internal-truststore-path property set ");
        }

        String truststorepath = restProperties.getInternalTruststorePath();


        File truststorefile = new File(truststorepath);

        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(truststorefile).build();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);


        HttpClient httpClient = HttpClientBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);


    }
}