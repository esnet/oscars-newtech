package net.es.oscars.webui.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvancedRequest {
    private Map<String, MinimalJunction> junctions;
    private Map<String, AdvancedPipe> pipes;
    private Integer startAt;
    private Integer endAt;
    private String description;
    private String connectionId;
}
