package net.es.oscars.resv.beans;

import lombok.*;
import net.es.oscars.dto.resv.ResourceType;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PssResource
{
    @Id
    private Long id;

    @NonNull
    private String urn;

    @NonNull
    private String refId;

    @NonNull
    private String scheduleId;

    @NonNull
    private String connectionId;

    @NonNull
    private ResourceType resourceType;

    private Integer resource;

}
