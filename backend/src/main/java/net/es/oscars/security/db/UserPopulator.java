package net.es.oscars.security.db;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.StartupComponent;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.security.SecProperties;
import net.es.oscars.security.ent.Permissions;
import net.es.oscars.security.ent.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserPopulator implements StartupComponent {
    @Autowired
    public UserPopulator(UserRepository userRepo, SecProperties properties) {
        this.userRepo = userRepo;
        this.properties = properties;
    }
    private UserRepository userRepo;

    private SecProperties properties;

    public void startup() throws StartupException {

        List<User> users = userRepo.findAll();

        if (users.isEmpty()) {
            log.info("No users in database; adding an admin user from sec.default-admin-username / -password properties.");
            if (properties == null) {
                log.info("No 'sec.default-admin-username / -password' application properties set!");
                return;
            }

            String username = properties.getDefaultAdminUsername();
            String password = properties.getDefaultAdminPassword();
            if (username == null) {
                log.info("Null sec.default-admin-username application property!");
                return;
            }
            if (password == null) {
                log.info("Null sec.default-admin-password application property!");
                return;
            }

            String encoded = new BCryptPasswordEncoder().encode(password);
            User admin = User.builder()
                    .username(username)
                    .password(encoded)
                    .fullName("Default admin user")
                    .email("oscars@localhost")
                    .institution("OSCARS")
                    .permissions(new Permissions())
                    .build();
            admin.getPermissions().setAdminAllowed(true);
            userRepo.save(admin);

        } else {
            log.debug("User db not empty; no action needed");

        }
    }





}
