package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.authnz.svc.UserService;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Slf4j
@Controller
public class MainController {

    private UserService userService;
    private TopoService topoService;

    @Autowired
    public MainController(UserService userService, TopoService topoService) {
        this.userService = userService;
        this.topoService = topoService;
    }

    @RequestMapping({"/", "/react/*"})
    public String home(Model model) {
        return "react";
    }

    @RequestMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }


    @RequestMapping(value = "/info/institutions", method = RequestMethod.GET)
    @ResponseBody
    public List<String> institution_suggestions() {
        return userService.getInstitutions();
    }

    @RequestMapping(value = "/info/vlanEdges", method = RequestMethod.GET)
    @ResponseBody
    public List<String> vlanEdge_suggestions() {
        log.info("getting vlan edges");
        return topoService.edges(Layer.ETHERNET);
    }

    @RequestMapping(value = "/info/device/{device}/vlanEdges", method = RequestMethod.GET)
    @ResponseBody
    public List<String> vlanEdge_device_suggestions(@PathVariable("device") String device) {
        log.info("getting device ETHERNET edges");
        return topoService.edgesWithCapability(device, Layer.ETHERNET);
    }

    @RequestMapping(value = "/info/devices", method = RequestMethod.GET)
    @ResponseBody
    public List<String> device_suggestions() {
        log.info("giving device suggestions");
        log.info("getting devices");
        return topoService.devices();
    }

}