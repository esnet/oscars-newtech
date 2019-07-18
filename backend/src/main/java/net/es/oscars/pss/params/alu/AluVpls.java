package net.es.oscars.pss.params.alu;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AluVpls {

    private Integer svcId;

    public Boolean protectEnabled;
    private Integer protectVcId;

    private List<AluSdpToVcId> sdpToVcIds;

    private List<AluSap> saps;

    private String serviceName;

    private String description;

    private String endpointName;

    private Integer mtu;
}
