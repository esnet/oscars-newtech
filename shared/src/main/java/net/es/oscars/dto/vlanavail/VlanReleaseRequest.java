package net.es.oscars.dto.vlanavail;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanReleaseRequest
{
    @NonNull
    private String connectionId;

    @NonNull
    private String port;

    @NonNull
    private Integer vlanId;

}
