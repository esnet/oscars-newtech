package net.es.oscars.migration.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InPssConfig {
    protected String device;
    protected String phase;
    protected String config;
}
