package net.es.oscars.dto.pss.params.mx;

import lombok.*;
import net.es.oscars.dto.pss.params.MplsPath;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MxVpls {

    private Integer vcId;

    private Integer protectVcId;
    private Boolean protectEnabled;

    private String serviceName;

    private String description;

    private String loopback;

    private String policyName;
    private String communityName;
    private Integer communityId;

    private String statsFilter;

    private Integer mtu;
}
