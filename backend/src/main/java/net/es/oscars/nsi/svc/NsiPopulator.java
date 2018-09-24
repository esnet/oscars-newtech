package net.es.oscars.nsi.svc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.NsiException;
import net.es.oscars.nsi.beans.NsiErrors;
import net.es.oscars.nsi.beans.NsiPeering;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@Data
public class NsiPopulator {

    private List<NsiPeering> peerings;
    private List<String> filter;
    private Map<String, NsiPeering> plusPorts = new HashMap<>();
    private List<NsiPeering> notPlusPorts = new ArrayList<>();
    private boolean loaded = false;

    public void loadNsiConfig(File peeringsFile, File filterFile) throws NsiException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        peerings = Arrays.asList(mapper.readValue(peeringsFile, NsiPeering[].class));

        log.info("peerings imported for nsi: " + peerings.size());
        filter = Arrays.asList(mapper.readValue(filterFile, String[].class));

        log.info("filter imported for nsi: " + filter.size());
        this.plusPorts = new HashMap<>();
        this.notPlusPorts = new ArrayList<>();

        for (NsiPeering p : peerings) {
            String[] parts = p.getIn().getLocal().split(":");
            // device:port:id:in|out
            if (parts.length != 4) {
                throw new NsiException("invalid peering config for "+p.getIn(), NsiErrors.NRM_ERROR);
            }
            if (parts[2].equals("+")) {
                String portUrn = parts[0]+":"+parts[1];
                plusPorts.put(portUrn, p);
                log.info("adding a plus peering for "+portUrn);
            } else {
                notPlusPorts.add(p);
            }
        }
        this.loaded = true;

    }


}
