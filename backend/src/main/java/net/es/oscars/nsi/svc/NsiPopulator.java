package net.es.oscars.nsi.svc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.StartupComponent;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.nsi.beans.NsiPeering;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@Data
public class NsiPopulator implements StartupComponent {

    @Value("${nsi.peerings}")
    private File peeringsFile;

    private List<NsiPeering> peerings;
    private Map<String, NsiPeering> plusPorts = new HashMap<>();
    private List<NsiPeering> notPlusPorts = new ArrayList<>();

    public void startup() throws StartupException {

        if (!peeringsFile.exists()) {
            throw new StartupException("Peerings file does not exist");
        }
        ObjectMapper mapper = new ObjectMapper();

        try {
            peerings = Arrays.asList(mapper.readValue(peeringsFile, NsiPeering[].class));
        } catch (IOException e) {
            throw new StartupException(e.getMessage());
        }
        log.info("peerings imported for nsi: " + peerings.size());

        Map<String, NsiPeering> byPort = new HashMap<>();
        for (NsiPeering p : peerings) {
            String[] parts = p.getIn().getLocal().split(":");
            // device:port:id:in|out
            if (parts.length != 4) {
                throw new StartupException("invalid peering config for "+p.getIn());
            }
            if (parts[2].equals("+")) {
                String portUrn = parts[0]+":"+parts[1];
                plusPorts.put(portUrn, p);
                log.info("adding a plus peering for "+portUrn);
            } else {
                notPlusPorts.add(p);
            }
        }

    }


}
