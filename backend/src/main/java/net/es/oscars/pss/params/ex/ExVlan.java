package net.es.oscars.pss.params.ex;

import lombok.*;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExVlan {

    private Integer vlanId;

    private String description;

    private String name;

    private List<ExIfce> ifces;


}
