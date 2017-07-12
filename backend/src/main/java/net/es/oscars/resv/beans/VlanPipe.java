package net.es.oscars.resv.beans;

import lombok.*;
import net.es.oscars.dto.pss.MplsPipeType;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.resv.ent.ReservedPssResourceE;
import net.es.oscars.resv.ent.ReservedVlanJunctionE;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanPipe {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String refId;

    @NonNull
    private String connectionId;

    @NonNull
    private Phase phase;

    private String aJunctionRefId;
    private String zJunctionRefId;

    private String azBandwidthRefId;
    private String zaBandwidthRefId;

    @ElementCollection
    private Set<String> pssRefIds;

    @ElementCollection
    private List<String> azERO;

    @ElementCollection
    private List<String> zaERO;

    @ElementCollection
    private Set<String> urnBlacklist;


    private PalindromicType eroPalindromic;

    private SurvivabilityType eroSurvivability;


}
