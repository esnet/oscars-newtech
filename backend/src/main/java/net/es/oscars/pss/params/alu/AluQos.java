package net.es.oscars.pss.params.alu;

import lombok.*;
import net.es.oscars.pss.params.Policing;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AluQos {

    private AluQosType type;

    private Policing policing;

    private Integer mbps;

    private Integer policyId;

    private String policyName;

    private String description;

}
