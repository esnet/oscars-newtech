package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Connection {

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NonNull
    @Column(unique = true)
    private String connectionId;

    @ManyToOne(cascade = CascadeType.ALL)
    private Blueprint requested;

    @ManyToOne(cascade = CascadeType.ALL)
    private Blueprint reserved;

    @ManyToOne(cascade = CascadeType.ALL)
    private Blueprint archived;

    private String state;

}
