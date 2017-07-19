package net.es.oscars.security.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.security.db.UserRepository;
import net.es.oscars.security.ent.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@Slf4j
public class UserService implements UserDetailsService {

    private UserRepository userRepo;

    @Autowired
    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;

    }


    public List<String> getInstitutions() {
        List<User> users = userRepo.findAll();
        List<String> result = new ArrayList<>();

        for (User userUser : users) {
            String inst = userUser.getInstitution();
            if (inst != null && ! inst.equals("")) {
                result.add(inst);
            }
        }
        return result;
    }

    public boolean usersExist() {
        return userRepo.findAll().size() > 0;
    }

    public User save(User user) {
        return userRepo.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }
}
