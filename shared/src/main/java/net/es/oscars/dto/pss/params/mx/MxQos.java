package net.es.oscars.dto.pss.params.mx;

import lombok.*;
import net.es.oscars.dto.pss.params.Policing;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MxQos {

    private String policerName;

    private String filterName;

    private Integer mbps;

    private MxQosForwarding forwarding;
    private boolean createPolicer;

    private Policing policing;
}
