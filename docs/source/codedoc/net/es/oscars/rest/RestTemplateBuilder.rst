.. java:import:: org.apache.http.auth AuthScope

.. java:import:: org.apache.http.auth UsernamePasswordCredentials

.. java:import:: org.apache.http.client HttpClient

.. java:import:: org.apache.http.conn.ssl SSLConnectionSocketFactory

.. java:import:: org.apache.http.impl.client BasicCredentialsProvider

.. java:import:: org.apache.http.impl.client HttpClientBuilder

.. java:import:: org.apache.http.ssl SSLContexts

.. java:import:: org.springframework.http.client ClientHttpRequestFactory

.. java:import:: org.springframework.http.client HttpComponentsClientHttpRequestFactory

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: javax.naming ConfigurationException

.. java:import:: javax.net.ssl SSLContext

.. java:import:: java.io File

.. java:import:: java.io IOException

.. java:import:: java.security KeyManagementException

.. java:import:: java.security KeyStoreException

.. java:import:: java.security NoSuchAlgorithmException

.. java:import:: java.security.cert CertificateException

RestTemplateBuilder
===================

.. java:package:: net.es.oscars.rest
   :noindex:

.. java:type:: public class RestTemplateBuilder

Methods
-------
build
^^^^^

.. java:method:: public RestTemplate build(RestProperties restProperties) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, ConfigurationException, KeyManagementException
   :outertype: RestTemplateBuilder

