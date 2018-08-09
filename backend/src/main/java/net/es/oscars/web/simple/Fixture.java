package net.es.oscars.web.simple;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    protected Boolean strict;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Integer mbps;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Integer inMbps;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Integer outMbps;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Integer svcId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Validity validity;


}
