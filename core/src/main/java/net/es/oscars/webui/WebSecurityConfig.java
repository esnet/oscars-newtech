package net.es.oscars.webui;

import lombok.extern.slf4j.Slf4j;
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

    @Autowired
    public WebSecurityConfig(DatabaseAuthProvider databaseAuthProvider) {
        this.databaseAuthProvider = databaseAuthProvider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                // development!
                .antMatchers("/resv/**").permitAll()
                .antMatchers("/topology/**").permitAll()
                .antMatchers("/react/**").permitAll()
                .antMatchers("/webpack-dev-server/**").permitAll()

                // index page and REST endpoints:
                .antMatchers("/").permitAll()
                .antMatchers("/viz/**").permitAll()
                .antMatchers("/info/**").permitAll()

                // various static / webjar resources
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/st/**").permitAll()

                // only admins for this one
                .antMatchers("/admin/**").hasAuthority("ADMIN")

                //built
                .antMatchers("/built/**").permitAll()

                // only admins
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login")
                .deleteCookies("remember-me")
                .and()
                .rememberMe()
                //TODO: Remove the following statements when done with webpack-dev-server development
                .and()
                .csrf().ignoringAntMatchers("/**");
        //TODO: Remove the following statement when done with webpack-dev-server development
        http.headers().httpStrictTransportSecurity().disable();
    }


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(databaseAuthProvider);
    }

}