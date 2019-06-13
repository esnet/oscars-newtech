package net.es.oscars.pss.params.alu;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AluSdp {

    private Integer sdpId;

    private String lspName;

    private String description;

    private String farEnd;
}
