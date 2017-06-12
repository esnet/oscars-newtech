package net.es.oscars.webui;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.authnz.ent.UserE;
import net.es.oscars.authnz.svc.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service(value = "restAuthProvider")
public class DatabaseAuthProvider implements AuthenticationProvider {
    private UserService userService;

    @Autowired
    public DatabaseAuthProvider(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String submittedPwd = authentication.getCredentials().toString();
        String encoded = passwordEncoder().encode(submittedPwd);
        log.info("username: " + username);
        log.info("encoded: " + encoded);

        UserE user;
        // no users exist; weird! in that case ask for a restart
        if (!userService.usersExist()) {
            throw new BadCredentialsException("No users exist! Restart OSCARS to get a default admin user");

        } else {
            Optional<UserE> maybeUser = userService.matchUsernameAndEncoded(username, encoded);
            if (maybeUser.isPresent()) {
                user = maybeUser.get();
            } else {
                throw new BadCredentialsException("Invalid username / password combination");
            }
        }

        log.info("matched passwords for " + username);
        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("USER"));
        if (user.getPermissions().isAdminAllowed()) {
            grantedAuths.add(new SimpleGrantedAuthority("ADMIN"));
        }
        return new UsernamePasswordAuthenticationToken(username, submittedPwd, grantedAuths);
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }


}
