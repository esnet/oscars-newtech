package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.beans.PssProfile;
import net.es.oscars.pss.beans.UrnMappingEntry;
import net.es.oscars.pss.beans.UrnMappingException;
import net.es.oscars.pss.prop.PssProps;
import net.es.oscars.pss.prop.UrnMappingProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class UrnMappingService {
    private PssProps properties;

    @Autowired
    public UrnMappingService(PssProps properties) {
        this.properties = properties;
    }

    public String getRouterAddress(String deviceUrn) throws UrnMappingException {
        PssProfile profile = PssProfile.profileFor(properties, deviceUrn);
        UrnMappingProps props = profile.getUrnMapping();

        switch (props.getMethod()) {
            case MATCH:
                for (UrnMappingEntry entry: props.getMatch()) {
                    if (entry.getUrn().equals(deviceUrn)) {
                        if (entry.getAddress().length() > 0) {
                            return entry.getAddress();
                        }
                    }
                }
                throw new UrnMappingException("IP for device urn "+deviceUrn+" not found, profile: "+profile.getProfile());
            case APPEND_SUFFIX:
                if (props.getSuffix() == null || props.getSuffix().length() == 0) {
                    throw new UrnMappingException("Empty suffix in config for "+deviceUrn+" profile: "+profile.getProfile());
                }
                return deviceUrn+props.getSuffix();

            case IDENTITY:
                return deviceUrn;
        }
        throw new UrnMappingException("Invalid URN mapping method");
    }



}
