package net.es.oscars.dto.pss.cmd;


import lombok.*;
import net.es.oscars.dto.topo.enums.DeviceModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResponse {
    @NonNull
    private String device;
    @NonNull
    private DeviceModel model;

    @NonNull
    private String config;

    @NonNull
    private String error;


}
