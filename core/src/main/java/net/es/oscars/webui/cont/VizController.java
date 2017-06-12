package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.viz.VizGraph;
import net.es.oscars.resv.ent.ConnectionE;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.webui.viz.VizExporter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Slf4j
@Controller
public class VizController {

    private VizExporter vizExporter;
    private ResvService resvService;

    @Autowired
    public VizController(VizExporter vizExporter, ResvService resvService) {
        this.vizExporter = vizExporter;
        this.resvService = resvService;
    }

    @RequestMapping(value = "/viz/topology/{classifier}", method = RequestMethod.GET)
    @ResponseBody
    public VizGraph viz_topology(@PathVariable String classifier) {
        if (classifier.equals("multilayer")) {
            return vizExporter.multilayerGraph();

        } else if (classifier.equals("unidirectional")) {
            return vizExporter.multilayerGraphUnidirectional();
        } else {
            throw new NoSuchElementException("bad classifier " + classifier);
        }
    }


    @RequestMapping(value = "/viz/connection/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public VizGraph viz_connection(@PathVariable String connectionId) {
        ModelMapper modelMapper = new ModelMapper();

        ConnectionE connE = resvService.findByConnectionId(connectionId).orElseThrow(NoSuchElementException::new);
        Connection conn = modelMapper.map(connE, Connection.class);

        return vizExporter.connection(conn);
    }

    @RequestMapping(value = "/viz/listPorts", method = RequestMethod.GET)
    @ResponseBody
    public String[] listTopoPorts() {
        Object[] portObjects = vizExporter.listTopologyPorts().toArray();

        return Arrays.copyOf(portObjects, portObjects.length, String[].class);
    }
}