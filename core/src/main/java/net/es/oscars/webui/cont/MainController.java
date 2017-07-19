package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class MainController {


    @RequestMapping({"/", "/react/*"})
    public String home(Model model) {
        return "react";
    }

    @RequestMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }



}