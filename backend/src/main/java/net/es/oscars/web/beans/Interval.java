package net.es.oscars.web.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties=true)
public class Interval {
    private Instant beginning;
    private Instant ending;
}
