package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import net.es.oscars.resv.enums.BuildMode;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.enums.State;

import javax.persistence.*;

@Data
@Entity
@Builder
@AllArgsConstructor(suppressConstructorProperties=true)
@NoArgsConstructor
public class Connection {
    @JsonCreator
    public Connection(@JsonProperty("connectionId") @NonNull String connectionId,
                      @JsonProperty("phase") @NonNull Phase phase,
                      @JsonProperty("mode") @NonNull BuildMode mode,
                      @JsonProperty("state") @NonNull State state,
                      @JsonProperty("username") @NonNull String username,
                      @JsonProperty("description") @NonNull String description,
                      @JsonProperty("reserved") Reserved reserved,
                      @JsonProperty("held") Held held,
                      @JsonProperty("archived") Archived archived) {
        this.connectionId = connectionId;
        this.phase = phase;
        this.mode = mode;
        this.state = state;
        this.username = username;
        this.description = description;
        this.reserved = reserved;
        this.held = held;
        this.archived = archived;
    }


    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NonNull
    @Column(unique = true)
    private String connectionId;

    @NonNull
    private Phase phase;

    @NonNull
    private BuildMode mode;

    @NonNull
    private State state;

    @NonNull
    private String description;

    @NonNull
    private String username;

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Reserved reserved;

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Held held;

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Archived archived;


}
