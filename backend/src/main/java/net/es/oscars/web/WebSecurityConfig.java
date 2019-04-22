package net.es.oscars.web;


import net.es.oscars.security.jwt.JwtAuthenticationTokenFilter;
import net.es.oscars.security.svc.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter  {


/*    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
*/

    @Autowired
    private UserService userService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilterBean() {
        return new JwtAuthenticationTokenFilter();
    }


    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder)
            throws Exception {
        authenticationManagerBuilder
                .userDetailsService(this.userService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Override
    public void configure(WebSecurity web) {

        // don't apply the JWT filter or any kind of security for swagger
        // or statics
        web.ignoring().antMatchers(
                HttpMethod.GET,
                "/",
                "/webjars/**",
                "/*.html",
                "/favicon.ico",
                "/**/*.html",
                "/**/*.css",
                "/**/*.js",
                "/services/**",
                "/documentation/**",
                "/swagger-ui.html/**",
                "/swagger-resources/**",
                "/null/swagger-resources/**",
                "/v2/api-docs",
                "/configuration/ui",
                "/configuration/security");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // no need for CSRF or sessions
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

                .authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/error").permitAll()
                // allow everyone to public API
                .antMatchers("/api/**").permitAll()

                // allow everyone to web services (for now..)
                .antMatchers("/services/**").permitAll()
                // allow everyone to pages
                .antMatchers("/pages/**").permitAll()

                .anyRequest().authenticated()
                // only allow authenticated users to get /protected pages or API endpoints
                .antMatchers("/protected/**").authenticated()

                // only allow admins to anything under /admin
                .antMatchers("/admin/**").hasAuthority("ADMIN")
                .and()

                .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);


    }

}