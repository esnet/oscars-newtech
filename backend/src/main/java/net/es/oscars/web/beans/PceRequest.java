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
public class PceRequest {
    private Interval interval;
    private String a;
    private String z;
    private Integer azBw;
    private Integer zaBw;

}
