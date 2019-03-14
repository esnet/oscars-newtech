package net.es.oscars.web.beans;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescriptionModifyRequest {
    @NonNull
    protected String connectionId;
    @NonNull
    protected String description;

}
