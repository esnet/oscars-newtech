package net.es.oscars.topo.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.StartupComponent;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.app.props.TopoProperties;
import net.es.oscars.topo.beans.DevicePositions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
@Data
public class UIPopulator implements StartupComponent {
    private TopoProperties topoProperties;

    @Autowired
    public UIPopulator(TopoProperties topoProperties) {
        this.topoProperties = topoProperties;
    }

    private DevicePositions positions;
    private Boolean started = false;

    public void startup() throws StartupException {
        ObjectMapper mapper = new ObjectMapper();

        String filename = "./config/topo/"+topoProperties.getPrefix()+"-positions.json";
        File jsonFile = new File(filename);

        try {
            positions = mapper.readValue(jsonFile, DevicePositions.class);
        } catch (IOException e) {
            throw new StartupException(e.getMessage());
        }
        log.info("positions imported for devices: " + positions.getPositions().size());
        started = true;

    }


}
