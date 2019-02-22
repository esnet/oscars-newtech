package net.es.oscars.topo.beans;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
public class Location {
    private Integer locationId;
    private String location;
    private Double latitude;
    private Double longitude;
}
