package net.es.oscars.webui.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MinimalRequest {
    private Map<String, MinimalJunction> junctions;
    private Map<String, MinimalPipe> pipes;
    private Integer startAt;
    private Integer endAt;
    private String description;
    private String connectionId;
}
