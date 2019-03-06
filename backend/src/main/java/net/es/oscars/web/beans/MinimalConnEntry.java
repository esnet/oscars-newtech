package net.es.oscars.web.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.resv.enums.Phase;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MinimalConnEntry {
    private List<MinimalConnEndpoint> endpoints;
    private Integer start;
    private Integer end;
    private String description;
}
