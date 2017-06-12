package net.es.oscars.authnz.svc;

import net.es.oscars.authnz.dao.UserRepository;
import net.es.oscars.authnz.ent.UserE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class UserService {

    private UserRepository userRepo;

    @Autowired
    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;

    }

    public Optional<UserE> matchUsernameAndEncoded(String username, String encoded) {
        Optional<UserE> maybeUser = userRepo.findByUsername(username);
        if (maybeUser.isPresent()) {
            UserE user = maybeUser.get();
            if (user.getPassword().equals(encoded)) {
                return maybeUser;
            } else {
                maybeUser = Optional.empty();
            }
        }
        return maybeUser;
    }

    public List<String> getInstitutions() {
        List<UserE> userES = userRepo.findAll();
        List<String> result = new ArrayList<>();

        for (UserE userUserE : userES) {
            String inst = userUserE.getInstitution();
            if (inst != null && ! inst.equals("")) {
                result.add(inst);
            }
        }
        return result;
    }

    public boolean usersExist() {
        return userRepo.findAll().size() > 0;
    }

    public UserE save(UserE user) {
        return userRepo.save(user);
    }

}
