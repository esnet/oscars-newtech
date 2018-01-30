package net.es.oscars.web.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.resv.ent.EroHop;
import net.es.oscars.resv.enums.EroDirection;

import java.util.List;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties=true)
public class PcePath {
    private double cost;

    private List<EroHop> azEro;
    private List<EroHop> zaEro;
    private Integer azAvailable;
    private Integer zaAvailable;

    private Integer azBaseline;
    private Integer zaBaseline;


}
