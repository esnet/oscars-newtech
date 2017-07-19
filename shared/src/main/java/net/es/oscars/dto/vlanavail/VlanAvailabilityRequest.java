package net.es.oscars.dto.vlanavail;

import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanAvailabilityRequest
{
    @NonNull
    private List<String> urns;

    @NonNull
    private Date startDate;

    @NonNull
    private Date endDate;
}
