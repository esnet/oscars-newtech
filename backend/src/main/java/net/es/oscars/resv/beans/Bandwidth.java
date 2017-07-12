package net.es.oscars.resv.beans;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Bandwidth {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String refId;

    @NonNull
    private String connectionId;

    @NonNull
    private Integer bw;

    @NonNull
    private Phase phase;

    private String scheduleId;

}
