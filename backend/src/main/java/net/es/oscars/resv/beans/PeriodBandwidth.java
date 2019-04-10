package net.es.oscars.resv.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.resv.enums.BwDirection;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodBandwidth {
    private Instant beginning;
    private Instant ending;
    private Integer bandwidth;
}