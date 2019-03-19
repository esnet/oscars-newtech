package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import net.es.oscars.resv.enums.Phase;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "refId")
public class Schedule {
    @JsonCreator
    public Schedule(@JsonProperty("connectionId") String connectionId,
                    @JsonProperty("beginning") @NonNull Instant beginning,
                    @JsonProperty("ending") @NonNull Instant ending,
                    @JsonProperty("releasing") @NonNull Instant releasing,
                    @JsonProperty("phase") Phase phase,
                    @JsonProperty("refId") String refId) {
        this.connectionId = connectionId;
        this.beginning = beginning;
        this.ending = ending;
        this.releasing = releasing;
        this.phase = phase;
        this.refId = refId;
    }


    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NonNull
    private Instant beginning;

    @NonNull
    private Instant ending;

    @NonNull
    private Instant releasing;

    // these will be populated by the system when designing
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String connectionId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Phase phase;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String refId;


    public Boolean overlaps(Instant b, Instant e) {
        boolean result = true;
        if (this.getReleasing().isBefore(b)) {
            result = false;
        }
        if (this.getBeginning().isAfter(e)) {
            result = false;
        }
        return result;
    }

}
