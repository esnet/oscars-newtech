package net.es.oscars.authnz.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.authnz.dao.UserRepository;
import net.es.oscars.authnz.ent.PermissionsE;
import net.es.oscars.authnz.ent.UserE;
import net.es.oscars.authnz.prop.AuthnzProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AuthnzPopulator {
    @Autowired
    public AuthnzPopulator(UserRepository userRepo, AuthnzProperties properties) {
        this.userRepo = userRepo;
        this.properties = properties;
    }
    private UserRepository userRepo;

    private AuthnzProperties properties;

    public void startup() {

        List<UserE> users = userRepo.findAll();

        if (users.isEmpty()) {
            log.info("No users in database; adding an admin user from admin.username / .password properties.");
            if (properties == null) {
                log.info("No 'admin.username / .password' application properties set!");
                return;
            }

            String username = properties.getUsername();
            String password = properties.getPassword();
            if (username == null) {
                log.info("Null admin.username application property!");
                return;
            }
            if (password == null) {
                log.info("Null admin.password application property!");
                return;
            }

            String encoded = new BCryptPasswordEncoder().encode(password);
            UserE admin = UserE.builder()
                    .username(username)
                    .password(encoded)
                    .permissions(new PermissionsE())
                    .build();
            admin.getPermissions().setAdminAllowed(true);
            userRepo.save(admin);

        } else {
            log.debug("User db not empty; no action needed");

        }
    }





}
