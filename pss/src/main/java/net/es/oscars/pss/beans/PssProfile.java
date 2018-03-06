package net.es.oscars.pss.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.st.ControlPlaneStatus;
import net.es.oscars.pss.prop.GetConfigProps;
import net.es.oscars.pss.prop.PssProps;
import net.es.oscars.pss.prop.RancidProps;
import net.es.oscars.pss.prop.UrnMappingProps;

import java.util.List;
import java.util.NoSuchElementException;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PssProfile {
    private String profile;
    private RancidProps rancid;
    private GetConfigProps getConfig;
    private UrnMappingProps urnMapping;

    public static PssProfile find(PssProps props, String profile) throws NoSuchElementException {
        for (PssProfile pssProfile : props.getProfiles()) {
            if (pssProfile.getProfile().equals(profile)) {
                return pssProfile;
            }
        }
        throw new NoSuchElementException("No profile found for "+profile);
    }



}
