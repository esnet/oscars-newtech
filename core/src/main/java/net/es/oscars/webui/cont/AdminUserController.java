package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.authnz.dao.UserRepository;
import net.es.oscars.authnz.ent.UserE;
import net.es.oscars.authnz.svc.UserService;
import net.es.oscars.dto.auth.Permissions;
import net.es.oscars.dto.auth.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class AdminUserController {

    @Autowired
    public AdminUserController(UserRepository userRepo, UserService userService) {
        this.userRepo = userRepo;
        this.userService = userService;
    }

    private UserRepository userRepo;
    private UserService userService;

    private ModelMapper modelMapper = new ModelMapper();

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested a user which didn't exist", ex);
    }

    @RequestMapping(value = "/admin/user_list", method = RequestMethod.GET)
    public String admin_user_list(Model model) {

        List<User> users = userRepo.findAll().stream().map(this::convertToDto).collect(Collectors.toList());

        model.addAttribute("users", users);
        return "admin_user_list";

    }

    @RequestMapping(value = "/admin/user_edit/{username}", method = RequestMethod.GET)
    public String admin_user_edit(@PathVariable String username, Model model) {
        User user = convertToDto(userRepo.findByUsername(username).orElseThrow(NoSuchElementException::new));
        model.addAttribute("user", user);
        return "admin_user_edit";
    }


    @RequestMapping(value = "/admin/user_add", method = RequestMethod.GET)
    public String admin_user_add(Model model) {
        model.addAttribute("user", new User());
        return "admin_user_add";
    }


    @RequestMapping(value = "/admin/user_pwd_submit", method = RequestMethod.POST)
    public String admin_user_pwd_submit(@ModelAttribute User updatedUser) {
        String username = updatedUser.getUsername();
        String encodedPassword = new BCryptPasswordEncoder().encode(updatedUser.getPassword());
        log.info("changing pwd for " + username);

        UserE user = userRepo.findByUsername(username).orElseThrow(NoSuchElementException::new);
        user.setPassword(encodedPassword);
        userRepo.save(user);

        return "redirect:/admin/user_edit/" + username;

    }

    @RequestMapping(value = "/admin/user_add_submit", method = RequestMethod.POST)
    public String admin_user_add_submit(@ModelAttribute User addedUser) {
        if (addedUser == null) {
            return "redirect:/admin/user_add";
        }
        UserE userE = convertToEnt(addedUser);

        String username = addedUser.getUsername();
        log.info("adding " + username);
        String encodedPassword = new BCryptPasswordEncoder().encode(addedUser.getPassword());
        addedUser.setPassword(encodedPassword);
        userE.setPassword(encodedPassword);
        userRepo.save(userE);

        log.info("added " + username);

        return "redirect:/admin/user_edit/" + username;
    }

    @RequestMapping(value = "/admin/user_del_submit", method = RequestMethod.POST)
    public String admin_user_del_submit(@ModelAttribute User userToDelete) {
        if (userToDelete == null) {
            return "redirect:/admin/user_list";
        }
        String username = userToDelete.getUsername();
        log.info("deleting " + username);

        userRepo.delete(userRepo.findByUsername(username).orElseThrow(NoSuchElementException::new));

        return "redirect:/admin/user_list";
    }

    @RequestMapping(value = "/admin/user_update_submit", method = RequestMethod.POST)
    public String admin_user_update_submit(@ModelAttribute User updatedUser) {
        String username = updatedUser.getUsername();
        log.info("updating " + username);


        UserE userE = userRepo.findByUsername(username).orElseThrow(NoSuchElementException::new);

        // keep the password and id
        String password = userE.getPassword();
        Long id = userE.getId();

        updatedUser.setPassword(password);

        // save the internal id
        userE = convertToEnt(updatedUser);
        userE.setId(id);
        userRepo.save(userE);

        return "redirect:/admin/user_edit/" + username;
    }


    @RequestMapping(value = "/users/institutions", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getInstitutions() {
        return userService.getInstitutions();

    }


    private UserE convertToEnt(User dtoUser) {
        if (dtoUser.getPermissions() == null) {
            dtoUser.setPermissions(new Permissions());
        }
        return modelMapper.map(dtoUser, UserE.class);
    }

    private User convertToDto(UserE userE) {
        return modelMapper.map(userE, User.class);
    }

}