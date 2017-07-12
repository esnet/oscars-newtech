package net.es.oscars.resv.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

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
    private Long id;

    @NonNull
    private EroHopType type;

    @NonNull
    private String urn;


}
