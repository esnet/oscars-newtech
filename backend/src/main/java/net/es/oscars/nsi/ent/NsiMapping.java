package net.es.oscars.nsi.ent;

import lombok.*;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.LifecycleStateEnumType;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.ProvisionStateEnumType;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.ReservationStateEnumType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NsiMapping {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String nsiConnectionId;

    @NonNull
    private String nsiGri;

    @NonNull
    private String oscarsConnectionId;

    @NonNull
    private String nsaId;

    @NonNull
    private Integer dataplaneVersion;
    @NonNull
    private LifecycleStateEnumType lifecycleState;
    @NonNull
    private ReservationStateEnumType reservationState;
    @NonNull
    private ProvisionStateEnumType provisionState;
}
