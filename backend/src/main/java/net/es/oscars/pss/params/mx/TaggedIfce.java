package net.es.oscars.pss.params.mx;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaggedIfce {

    private String port;

    private Integer vlan;

    private String description;

    private boolean vlanSwap;

}
