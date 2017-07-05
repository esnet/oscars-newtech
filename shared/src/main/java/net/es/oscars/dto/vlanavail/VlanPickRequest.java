package net.es.oscars.dto.vlanavail;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanPickRequest
{
    @NonNull
    private String connectionId;

    @NonNull
    private String port;

    @NonNull
    private String vlanExpression;

}
