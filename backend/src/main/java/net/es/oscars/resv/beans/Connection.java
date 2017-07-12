package net.es.oscars.resv.beans;

import lombok.*;
import net.es.oscars.resv.ent.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Connection {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String connectionId;

    private Schedule schedule;

    @NonNull
    @ElementCollection(targetClass=Date.class)
    private List<Date> reservedSchedule;

    @OneToOne (cascade = CascadeType.ALL)
    private SpecificationE specification;

    @OneToOne (cascade = CascadeType.ALL)
    private ReservedBlueprintE reserved;

    @OneToOne (cascade = CascadeType.ALL)
    private ArchivedBlueprintE archivedResv;
}
