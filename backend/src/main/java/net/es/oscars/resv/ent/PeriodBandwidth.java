package net.es.oscars.resv.ent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodBandwidth {
    Instant beginning;
    Instant ending;
    Integer bandwidth;
}