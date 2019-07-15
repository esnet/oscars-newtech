package net.es.oscars.pss.params.mx;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.pss.params.Lsp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MxLsp {

    private String neighbor;
    private Lsp lsp;
    private boolean primary;

    private String policeFilter;


}
