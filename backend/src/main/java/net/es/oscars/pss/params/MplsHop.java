package net.es.oscars.pss.params;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MplsHop {

    private Integer order;

    private String address;

}
