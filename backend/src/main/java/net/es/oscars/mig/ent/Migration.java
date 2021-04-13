package net.es.oscars.mig.ent;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.mig.enums.MigrationState;
import net.es.oscars.topo.ent.Port;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Migration {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column
    private String description;

    @NonNull
    @Column
    private String shortName;

    @NonNull
    @Column
    private MigrationState state;

    @NonNull
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<PortMove> portMoves = new HashSet<>();

}
