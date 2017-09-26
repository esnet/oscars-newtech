package net.es.oscars.topo.ent;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Version {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private Boolean valid;

    @NonNull
    private Instant updated;
}
