package net.es.oscars.nsi.ent;

import lombok.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NsiRequesterNSA {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String nsaId;


    @NonNull
    private String callbackUrl;


}
