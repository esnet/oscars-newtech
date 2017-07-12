package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.security.db.UserRepository;
import net.es.oscars.security.ent.User;
import net.es.oscars.security.jwt.JwtAuthenticationRequest;
import net.es.oscars.security.jwt.JwtAuthenticationResponse;
import net.es.oscars.security.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;


@Slf4j
@Controller
public class AccountController {

    private AuthenticationManager authenticationManager;

    private JwtTokenUtil jwtTokenUtil;

    private UserDetailsService userDetailsService;

    private UserRepository userRepo;

    @Autowired
    public AccountController(UserRepository userRepo, AuthenticationManager authenticationManager,
                             JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
        this.userRepo = userRepo;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }


    @RequestMapping(value = "/api/account/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest)
            throws AuthenticationException {
        log.info(authenticationRequest.toString());

        // Perform the security
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        boolean isAdmin = false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals("ADMIN")) {
                isAdmin = true;
            }
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password post-security so we can generate token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Return the token
        return ResponseEntity.ok(JwtAuthenticationResponse.builder().admin(isAdmin).token(token).build());
    }


    @RequestMapping(value = "/protected/account", method = RequestMethod.GET)
    @ResponseBody
    public User protected_account(Authentication authentication) {
        String username = authentication.getName();
        return userRepo.findByUsername(username).orElseThrow(NoSuchElementException::new);
    }

    @RequestMapping(value = "/protected/account_password", method = RequestMethod.POST)
    @ResponseBody
    public void protected_account_password(Authentication authentication, @RequestBody String newPassword) {
        String username = authentication.getName();
        User dbUser = userRepo.findByUsername(username).orElseThrow(NoSuchElementException::new);
        String encodedPassword = new BCryptPasswordEncoder().encode(newPassword);
        dbUser.setPassword(encodedPassword);
        userRepo.save(dbUser);
    }

    @RequestMapping(value = "/protected/account", method = RequestMethod.POST)
    @ResponseBody
    public User protected_user_update(Authentication authentication, @RequestBody User inUser) {
        String username = authentication.getName();

        User dbUser = userRepo.findByUsername(username).orElseThrow(NoSuchElementException::new);
        dbUser.setEmail(inUser.getEmail());
        dbUser.setFullName(inUser.getFullName());
        dbUser.setInstitution(inUser.getInstitution());
        log.info(inUser.toString());
        log.info(dbUser.toString());
        // specifically don't let user set their own permissions
        //        dbUser.setPermissions(inUser.getPermissions());
        // there is a dedicated method for updating password
        // String encodedPassword = new BCryptPasswordEncoder().encode(inUser.getPassword());
        // dbUser.setPassword(encodedPassword);
        userRepo.save(dbUser);
        return dbUser;
    }

}

