package net.es.oscars.security.ent;


import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class Permissions {
    public Permissions() {

    }


    private boolean adminAllowed = false;


}
