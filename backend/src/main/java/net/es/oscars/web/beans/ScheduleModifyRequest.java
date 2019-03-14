package net.es.oscars.web.beans;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleModifyRequest {
    @NonNull
    protected String connectionId;
    @NonNull
    protected ScheduleModifyType type;

    protected Integer timestamp;
}
