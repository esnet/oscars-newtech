package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
@Entity
public class Vlan
{
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    // mandatory; must be set even if empty
    @NonNull
    private String vlanExpression;


    // leave the following empty when requesting
    private String connectionId;

    @ManyToOne
    private Schedule schedule;

    private String urn;

    private Integer vlan;

}
