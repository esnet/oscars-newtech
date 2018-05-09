package net.es.oscars.web.simple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fixture {
    protected String junction;
    protected String port;

    protected Integer vlan;

    protected Integer mbps;
    protected Integer inMbps;
    protected Integer outMbps;


}
