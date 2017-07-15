package net.es.oscars.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/{path:(?!.*.js|.*.css|.*.jpg).*$}")
    public String index() {
        return "/";
    }
}