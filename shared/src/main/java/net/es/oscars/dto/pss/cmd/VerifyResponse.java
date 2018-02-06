package net.es.oscars.dto.pss.cmd;


import lombok.*;
import net.es.oscars.dto.topo.enums.DeviceModel;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
    private Map<String, Object> present;

    @NonNull
    private Instant lastUpdated;


}
