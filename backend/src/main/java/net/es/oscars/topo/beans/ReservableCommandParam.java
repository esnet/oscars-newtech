package net.es.oscars.topo.beans;


import lombok.*;
import net.es.oscars.topo.enums.CommandParamType;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservableCommandParam {

    @NonNull
    private CommandParamType type;

    private Set<IntRange> reservableRanges;

}
