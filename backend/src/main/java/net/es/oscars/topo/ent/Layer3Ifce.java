package net.es.oscars.topo.ent;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.ArrayList;

@Data
@Entity
@Builder
@AllArgsConstructor(suppressConstructorProperties=true)
@NoArgsConstructor
@EqualsAndHashCode(exclude={"port", "tags", "id"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "urn")

public class Layer3Ifce {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Version version;

    @Basic
    @Column(length = 65535)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ArrayList<String> tags;

    @NonNull
    @NaturalId
    @Column(unique = true)
    private String urn;

    @NonNull
    @ManyToOne
    @JsonBackReference(value="port")
    private Port port;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String ifce;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String ipv4Address;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String ipv6Address;

}
