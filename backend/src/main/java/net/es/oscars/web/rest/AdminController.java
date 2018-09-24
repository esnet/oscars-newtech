package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.security.db.UserRepository;
import net.es.oscars.security.ent.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Controller
public class AdminController {

    @Autowired
    public AdminController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    private UserRepository userRepo;
    @ExceptionHandler(StartupException.class)

    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public void handleStartup(StartupException ex) {
        log.warn("Still in startup");
    }


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.info("user data conflicts ", ex);
    }


    @RequestMapping(value = "/admin/users", method = RequestMethod.GET)
    @ResponseBody
    public List<User> admin_users_all() {

        List<User> users = userRepo.findAll();
        users.sort(Comparator.comparing(User::getUsername));
        return users;
    }

    @RequestMapping(value = "/admin/users/{username}", method = RequestMethod.GET)
    @ResponseBody
    public User admin_users_one(@PathVariable String username) {
        return userRepo.findByUsername(username).orElseThrow(NoSuchElementException::new);
    }

    @RequestMapping(value = "/admin/users/{username}", method = RequestMethod.POST)
    @ResponseBody
    public User admin_users_add_update(@PathVariable String username, @RequestBody User inUser) {
        Optional<User> maybeDbUser = userRepo.findByUsername(username);

        if (maybeDbUser.isPresent()) {
            User dbUser = maybeDbUser.get();
            dbUser.setEmail(inUser.getEmail());
            dbUser.setFullName(inUser.getFullName());
            dbUser.setInstitution(inUser.getInstitution());
            dbUser.setPermissions(inUser.getPermissions());
            userRepo.save(dbUser);
            return dbUser;
        } else {
            userRepo.save(inUser);
            return inUser;
        }
    }

    @RequestMapping(value = "/admin/users/{username}/password", method = RequestMethod.POST)
    @ResponseBody
    public void admin_user_password(@PathVariable String username, @RequestBody String newPassword) {
        User dbUser = userRepo.findByUsername(username).orElseThrow(NoSuchElementException::new);
        String encodedPassword = new BCryptPasswordEncoder().encode(newPassword);
        dbUser.setPassword(encodedPassword);
        userRepo.save(dbUser);
    }


    @RequestMapping(value = "/admin/users/{username}", method = RequestMethod.DELETE)
    @ResponseBody
    public void admin_users_delete(@PathVariable String username) {
        userRepo.delete(userRepo.findByUsername(username).orElseThrow(NoSuchElementException::new));
    }


}