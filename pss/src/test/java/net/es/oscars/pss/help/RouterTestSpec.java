package net.es.oscars.pss.help;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.pss.params.alu.AluParams;
import net.es.oscars.dto.pss.params.ex.ExParams;
import net.es.oscars.dto.pss.params.mx.MxParams;
import net.es.oscars.dto.topo.enums.DeviceModel;

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
