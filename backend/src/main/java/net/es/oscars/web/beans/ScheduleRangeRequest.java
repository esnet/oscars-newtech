package net.es.oscars.web.beans;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRangeRequest {
    @NonNull
    protected String connectionId;
    @NonNull
    protected ScheduleModifyType type;

}
