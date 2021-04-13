package net.es.oscars.mig.ent;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Slf4j
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortMove {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String srcDevice;

    @NonNull
    private String srcPort;

    @NonNull
    private String dstDevice;

    @NonNull
    private String dstPort;

}
