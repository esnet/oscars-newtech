package net.es.oscars.topo.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.viz.DevicePositions;
import net.es.oscars.topo.prop.TopoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
@Data
public class UIPopulator {
    private TopoProperties topoProperties;

    @Autowired
    public UIPopulator(TopoProperties topoProperties) {
        this.topoProperties = topoProperties;
    }

    private DevicePositions positions;
    private Boolean started = false;

    public void startup() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String filename = "./config/topo/"+topoProperties.getPrefix()+"-positions.json";
        File jsonFile = new File(filename);

        positions = mapper.readValue(jsonFile, DevicePositions.class);
        log.info("positions imported for devices: " + positions.getPositions().size());
        started = true;

    }


}
