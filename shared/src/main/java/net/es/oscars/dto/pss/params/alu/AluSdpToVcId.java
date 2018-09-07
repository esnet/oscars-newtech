package net.es.oscars.dto.pss.params.alu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AluSdpToVcId {
    private Integer sdpId;
    private Integer vcId;
    private boolean primary;
    private boolean besteffort;
}
