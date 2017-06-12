package net.es.oscars.dto.pss.params.mx;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MxVpls {

    private Integer vcId;

    private String serviceName;

    private String description;

    private String loopback;

    private String community;

    private String policyName;

    private String statsFilter;

}
