package net.es.oscars.help;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.pss.params.alu.AluParams;
import net.es.oscars.pss.params.ex.ExParams;
import net.es.oscars.pss.params.mx.MxParams;
import net.es.oscars.dto.topo.DeviceModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouterTestSpec {
    private String filename;
    private String device;
    private DeviceModel model;
    private String profile;
    private Boolean shouldFail;
    private AluParams aluParams;
    private ExParams exParams;
    private MxParams mxParams;

}
