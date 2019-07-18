package net.es.oscars.pss.params.mx;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.pss.params.MplsPath;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MxParams {


    private List<MxLsp> lsps;

    private List<MplsPath> paths;


    private List<TaggedIfce> ifces;

    private List<MxQos> qos;

    private MxVpls mxVpls;



}
