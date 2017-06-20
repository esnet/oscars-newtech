package net.es.oscars.pss.beans;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrnMapping {
    Map<String, UrnMappingEntry> entryMap = new HashMap<>();
}
