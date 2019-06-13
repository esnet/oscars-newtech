package net.es.oscars.pss.params.ex;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExParams {

    private List<ExVlan> vlans;


}
