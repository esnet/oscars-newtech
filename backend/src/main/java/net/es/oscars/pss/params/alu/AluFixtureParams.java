package net.es.oscars.pss.params.alu;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AluFixtureParams {
    private AluQos inQos;
    private AluQos egQos;
    private List<AluSap> saps;
}