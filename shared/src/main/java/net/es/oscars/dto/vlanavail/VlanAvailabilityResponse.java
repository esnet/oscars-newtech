package net.es.oscars.dto.vlanavail;


import lombok.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanAvailabilityResponse
{
    @NonNull
    private Map<String, PortVlanAvailability> portVlans;
}
