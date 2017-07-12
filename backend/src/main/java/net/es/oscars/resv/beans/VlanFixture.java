package net.es.oscars.resv.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import net.es.oscars.dto.pss.EthFixtureType;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanFixture
{
    @Id
    @JsonIgnore
    private Long id;

    @NonNull
    private String refId;

    @NonNull
    private String connectionId;

    @NonNull
    private Phase phase;

    @NonNull
    private EthFixtureType fixtureType;

    @NonNull
    private String ifceUrn;

    private String inBandwidthRefId;
    private String egBandwidthRefId;

    private String vlanRefId;

    private Set<String> pssRefIds;

    private String vlanExpression;


}
