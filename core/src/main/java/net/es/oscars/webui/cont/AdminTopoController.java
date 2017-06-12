package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;


@Controller
@Slf4j
public class AdminTopoController {

    private final String oscarsUrl = "https://localhost:8000";

    @Autowired
    private RestTemplate restTemplate;


    @RequestMapping(value = "/admin/group_list", method = RequestMethod.GET)
    public String admin_group_list(Model model) {

        String restPath = oscarsUrl + "/grp/";
        String[] groups = restTemplate.getForObject(restPath, String[].class);

        model.addAttribute("groups", groups);
        return "admin_group_list";
    }
}