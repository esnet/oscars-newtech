package net.es.oscars.web.beans;

import lombok.*;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleModifyResponse {
    @NonNull
    protected Boolean success;
    @NonNull
    protected List<String> overlapping;
    @NonNull
    protected String reason;
    @NonNull
    protected Integer begin;
    @NonNull
    protected Integer end;
}
