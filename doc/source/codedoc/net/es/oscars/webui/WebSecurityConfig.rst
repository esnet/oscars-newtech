.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.context.annotation Configuration

.. java:import:: org.springframework.security.config.annotation.authentication.builders AuthenticationManagerBuilder

.. java:import:: org.springframework.security.config.annotation.web.builders HttpSecurity

.. java:import:: org.springframework.security.config.annotation.web.configuration EnableWebSecurity

.. java:import:: org.springframework.security.config.annotation.web.configuration WebSecurityConfigurerAdapter

.. java:import:: org.springframework.security.web.util.matcher AntPathRequestMatcher

WebSecurityConfig
=================

.. java:package:: net.es.oscars.webui
   :noindex:

.. java:type:: @Configuration @EnableWebSecurity @Slf4j public class WebSecurityConfig extends WebSecurityConfigurerAdapter

Constructors
------------
WebSecurityConfig
^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public WebSecurityConfig(DatabaseAuthProvider databaseAuthProvider)
   :outertype: WebSecurityConfig

Methods
-------
configure
^^^^^^^^^

.. java:method:: @Override protected void configure(HttpSecurity http) throws Exception
   :outertype: WebSecurityConfig

configureGlobal
^^^^^^^^^^^^^^^

.. java:method:: @Autowired public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
   :outertype: WebSecurityConfig

