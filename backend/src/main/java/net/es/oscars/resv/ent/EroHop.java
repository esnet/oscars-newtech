package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import net.es.oscars.resv.enums.EroHopType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EroHop
{
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    private EroHopType type;

    @NonNull
    private String urn;


}
