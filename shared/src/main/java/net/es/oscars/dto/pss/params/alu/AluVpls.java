package net.es.oscars.dto.pss.params.alu;

import lombok.*;

import java.util.List;
import java.util.Map;

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

}
