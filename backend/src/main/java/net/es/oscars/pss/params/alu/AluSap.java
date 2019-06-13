package net.es.oscars.pss.params.alu;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AluSap {

    private String port;

    private Integer vlan;

    private Integer ingressQosId;

    private Integer egressQosId;

    private String description;



}
