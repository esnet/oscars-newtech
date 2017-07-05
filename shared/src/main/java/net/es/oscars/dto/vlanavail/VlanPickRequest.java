package net.es.oscars.dto.vlanavail;

import lombok.*;

import java.util.Date;


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

    @NonNull
    private Date startDate;

    @NonNull
    private Date endDate;


}
