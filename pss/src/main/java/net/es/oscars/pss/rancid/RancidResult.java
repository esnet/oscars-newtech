package net.es.oscars.pss.rancid;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RancidResult {
    private String commandline;
    private String output;
    private Integer exitCode;

}
