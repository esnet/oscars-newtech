package net.es.oscars.pss.prop;

import lombok.*;
import net.es.oscars.pss.beans.UrnMappingEntry;
import net.es.oscars.pss.beans.UrnMappingMethod;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrnMappingProps {

    @NonNull
    private UrnMappingMethod method;

    private String suffix;

    private List<UrnMappingEntry> match = new ArrayList<>();


}


