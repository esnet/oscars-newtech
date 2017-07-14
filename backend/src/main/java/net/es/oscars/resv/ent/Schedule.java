package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import net.es.oscars.resv.enums.Phase;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties=true)
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "refId")
public class Schedule {

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NonNull
    private Instant beginning;

    @NonNull
    private Instant ending;

    // leave the following empty when requesting
    private String connectionId;

    private Phase phase;

    private String refId;

    public Boolean overlaps(Instant b, Instant e) {
        boolean result = true;
        if (this.getEnding().isBefore(b)) {
            result = false;
        }
        if (this.getBeginning().isAfter(e)) {
            result = false;
        }
        return result;
    }

}
