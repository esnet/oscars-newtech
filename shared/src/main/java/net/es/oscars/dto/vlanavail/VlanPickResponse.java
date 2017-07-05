package net.es.oscars.dto.vlanavail;


import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanPickResponse
{
    @NonNull
    private Integer vlanId;

    @NonNull
    private Date heldUntil;

}
