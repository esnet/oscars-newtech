package net.es.oscars.pss.params;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lsp {

    private String name;

    private String pathName;

    private Integer setupPriority;

    private Integer holdPriority;

    private Integer metric;

    private String to;
}
