package net.es.oscars.webui.viz;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HighlightResult {
    List<String > nodeIds;
    List<String > edgeIds;
}
