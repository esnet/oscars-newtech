package net.es.oscars.resv.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Vlan
{
    @Id
    @JsonIgnore
    private Long id;

    @NonNull
    private String refId;

    @NonNull
    private String scheduleId;

    @NonNull
    private String urn;

    @NonNull
    private Phase phase;


    private Integer vlan;

}
