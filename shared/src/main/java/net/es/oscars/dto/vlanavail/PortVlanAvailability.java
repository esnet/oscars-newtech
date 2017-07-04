package net.es.oscars.dto.vlanavail;


import lombok.*;
import net.es.oscars.dto.IntRange;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortVlanAvailability
{
    @NonNull
    private List<IntRange> vlanRanges;
    @NonNull
    private String vlanExpression;
}
