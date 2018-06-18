package net.es.oscars.rest;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.naming.ConfigurationException;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class RestTemplateBuilder {

    public RestTemplate build(RestProperties restProperties) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, ConfigurationException, KeyManagementException {
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

        String sharedusername = restProperties.getInternalUsername();
        String sharedpassword = restProperties.getInternalPassword();
        String truststorepath = restProperties.getInternalTruststorePath();


        File truststorefile = new File(truststorepath);

        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(truststorefile).build();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(sharedusername, sharedpassword));

        HttpClient httpClient = HttpClientBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();

        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(requestFactory);

    }
}
