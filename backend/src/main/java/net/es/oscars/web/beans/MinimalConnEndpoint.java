package net.es.oscars.web.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MinimalConnEndpoint {
    private String router;
    private String port;
    private Integer vlan;
}
