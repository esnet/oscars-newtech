package net.es.oscars.authnz.ent;


import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserE implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String username = "default";

    @NonNull
    @Column
    private String password = "";

    @Column
    private String certIssuer;

    @Column
    private String certSubject;

    @Column
    private String fullName;

    @Column
    private String email;

    @Column
    private String institution;

    @Embedded
    private PermissionsE permissions;

}
