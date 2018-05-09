package net.es.oscars.web.simple;

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
public class Junction {
    protected String device;
    protected List<Fixture> fixtures = new ArrayList<>();

}
