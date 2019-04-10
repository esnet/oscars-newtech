package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventLog {
    @JsonCreator
    public EventLog(@JsonProperty("connectionId") @NonNull String connectionId,
                    @JsonProperty("created") @NonNull Instant created,
                    @JsonProperty("archived") @NonNull Instant archived,
                    @JsonProperty("events") @NonNull List<Event> events) {
        this.connectionId = connectionId;
        this.created = created;
        this.archived = archived;
        this.events = events;
    }

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NonNull
    private String connectionId;

    @NonNull
    private Instant created;

    private Instant archived;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Event> events;


}
