package net.es.oscars.migration.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InPssResResource {
    protected String device;
    protected Integer resource;
    protected String what;
}
