package net.es.oscars.migration.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InMisc {
    protected Boolean production;
    protected Boolean applyQos;
    protected String policing;
    protected String protection;
    protected String description;
    protected String user;

}
