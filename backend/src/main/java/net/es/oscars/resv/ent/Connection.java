package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import net.es.oscars.resv.enums.Phase;

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
                      @JsonProperty("username") @NonNull String username,
                      @JsonProperty("reserved") Reserved reserved,
                      @JsonProperty("held") Held held,
                      @JsonProperty("archived") Archived archived) {
        this.connectionId = connectionId;
        this.phase = phase;
        this.username = username;
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
