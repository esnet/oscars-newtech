package net.es.oscars.dto.pss.cmd;


import lombok.*;
import net.es.oscars.dto.pss.params.alu.AluParams;
import net.es.oscars.dto.pss.params.ex.ExParams;
import net.es.oscars.dto.pss.params.mx.MxParams;
import net.es.oscars.dto.topo.enums.DeviceModel;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceConfigRequest {
    @NonNull
    private String device;
    @NonNull
    private DeviceModel model;

    @NonNull
    private String profile;


}
