package net.es.oscars.webui;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.webui.prop.WebuiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private DatabaseAuthProvider databaseAuthProvider;
    private WebuiProperties webuiProperties;

    @Autowired
    public WebSecurityConfig(DatabaseAuthProvider databaseAuthProvider, WebuiProperties webuiProperties) {
        this.webuiProperties = webuiProperties;
        this.databaseAuthProvider = databaseAuthProvider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (webuiProperties.getDevMode()) {
            log.info("NON-SECURE web ui");
            http
                    .csrf()
                    .ignoringAntMatchers("/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/**").permitAll();

        } else {
            log.info("SECURE mode web ui");
            http
                    // /public has APIs for the general public,
                    // /protected is only for authenticated users

                    .csrf()
                    .ignoringAntMatchers("/protected/**")
                    .and()
                    .csrf()
                    .ignoringAntMatchers("/public/**")
                    .and()

                    .authorizeRequests()
                    // allow everyone to hit the first page and any APIs under /public
                    .antMatchers("/").permitAll()
                    .antMatchers("/public/**").permitAll()

                    // allow everyone to grab files from webjars, webpack, and static resources
                    .antMatchers("/webjars/**").permitAll()
                    .antMatchers("/built/**").permitAll()
                    .antMatchers("/st/**").permitAll()

                    // only allow authenticated users to get //protected API endpoints
                    .antMatchers("/protected/**").authenticated()
                    // only allow admins to get anything under /admin
                    .antMatchers("/admin/**").hasAuthority("ADMIN")

                    // other requests: must be an authenticated user
                    .anyRequest().authenticated()


                    .and()
                    .formLogin().loginPage("/login").permitAll()

                    .and().logout().permitAll()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/login")

                    .deleteCookies("remember-me")
                    .and()
                    .rememberMe();

        }


    }


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(databaseAuthProvider);
    }

}