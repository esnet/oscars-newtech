package net.es.oscars.dto.pss.params.mx;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.pss.params.Lsp;
import net.es.oscars.dto.pss.params.MplsPath;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MxParams {


    private List<MxLsp> lsps;

    private List<MplsPath> paths;


    private List<MxIfce> ifces;

    private List<MxQos> qos;

    private MxVpls mxVpls;



}
