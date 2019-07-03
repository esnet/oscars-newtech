package net.es.oscars.pss.params.mx;

import lombok.*;

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
