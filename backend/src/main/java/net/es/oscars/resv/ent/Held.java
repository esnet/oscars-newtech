package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Builder
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
public class Held {
    @JsonCreator
    public Held(@JsonProperty("connectionId") @NonNull String connectionId,
                @JsonProperty("cmp") @NonNull Components cmp,
                @JsonProperty("expiration") @NonNull Instant expiration,
                @JsonProperty("schedule") @NonNull Schedule schedule) {
        this.connectionId = connectionId;
        this.cmp = cmp;
        this.expiration = expiration;
        this.schedule = schedule;
    }

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NonNull
    @Column(unique = true)
    private String connectionId;

    @NonNull
    private Instant expiration;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval=true)
    private Components cmp;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    private Schedule schedule;


}
