package net.es.oscars.pss.svc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.beans.UrnMapping;
import net.es.oscars.pss.beans.UrnMappingException;
import net.es.oscars.pss.prop.UrnMappingProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public class UrnMappingService {
    private UrnMappingProps properties;
    private UrnMapping mapping;

    @Autowired
    public UrnMappingService(UrnMappingProps properties) {
        this.properties = properties;
        this.mapping = new UrnMapping();
    }

    public String getRouterAddress(String deviceUrn) throws UrnMappingException {
        switch (properties.getMethod()) {
            case IP_FROM_CONFIG:
                if (mapping.getEntryMap().keySet().contains(deviceUrn)) {
                    if (mapping.getEntryMap().get(deviceUrn).getIpv4Address().length() > 0) {
                        return mapping.getEntryMap().get(deviceUrn).getIpv4Address();
                    }
                }
                throw new UrnMappingException("IP for device urn "+deviceUrn+" not found in file!");
            case DNS_FROM_CONFIG:
                if (mapping.getEntryMap().keySet().contains(deviceUrn)) {
                    if (mapping.getEntryMap().get(deviceUrn).getDns().length() > 0) {
                        return mapping.getEntryMap().get(deviceUrn).getDns();
                    }
                }
                throw new UrnMappingException("DNS name for device urn "+deviceUrn+" not found in file!");
            case URN_IS_HOSTNAME:
                if (properties.getDnsSuffix() == null || properties.getDnsSuffix().length() == 0) {
                    return deviceUrn;
                }
                return deviceUrn+properties.getDnsSuffix();
        }
        throw new UrnMappingException("Invalid URN mapping method");
    }

    public UrnMapping getMapping() {
        return this.mapping;
    }

    public void startup() throws UrnMappingException, IOException {
        log.info("initializing control plane settings");

        switch (properties.getMethod()) {
            case IP_FROM_CONFIG:
            case DNS_FROM_CONFIG:
                String addrsFilename = "./config/" + properties.getAddressesFile();
                this.loadFrom(addrsFilename);
                break;
            case URN_IS_HOSTNAME:
                log.info("control plane mapping: urn is the hostname");
                if (properties.getDnsSuffix() == null) {
                    throw new UrnMappingException("no DNS suffix set");
                }
                break;
        }
    }

    public void loadFrom(String filename) throws IOException {
        File jsonFile = new File(filename);
        ObjectMapper mapper = new ObjectMapper();
        mapping = mapper.readValue(jsonFile, UrnMapping.class);
    }


}
