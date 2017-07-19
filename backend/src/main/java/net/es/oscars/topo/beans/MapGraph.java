package net.es.oscars.topo.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MapGraph {
    List<MapNode> nodes;
    List<MapEdge> edges;
}
