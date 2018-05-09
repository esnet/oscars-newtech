package net.es.oscars.migration.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InCmp {
    protected List<InHop> pipe;
    protected List<InFixture> fixtures = new ArrayList<>();
    protected List<String> junctions;
}
