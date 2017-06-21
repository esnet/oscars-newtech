package net.es.oscars.dto.pss.params.alu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.pss.params.Lsp;
import net.es.oscars.dto.pss.params.MplsPath;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AluParams {

    private AluVpls aluVpls;

    private List<AluSdp> sdps;

    private List<AluQos> qoses;

    private List<Lsp> lsps;

    private List<MplsPath> paths;

    private String loopbackInterface;
    private String loopbackAddress;

    private Boolean applyQos;


}
