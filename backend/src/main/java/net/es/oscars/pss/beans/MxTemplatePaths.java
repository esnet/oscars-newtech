package net.es.oscars.pss.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MxTemplatePaths {
    private String ifces;
    private String lsp;
    private String path;
    private String qos;
    private String sdp;
    private String vpls;

}
