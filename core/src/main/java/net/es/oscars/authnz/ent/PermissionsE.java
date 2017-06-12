package net.es.oscars.authnz.ent;


import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class PermissionsE {
    public PermissionsE() {

    }

    private long maxMbpsPerReservation = 0;

    private long maxMinutesPerReservation = 0;

    private boolean callSoapAllowed = false;

    private boolean pathSpecAllowed = false;

    private boolean adminAllowed = false;


}
