package net.es.oscars.soap.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleRequest extends GenericRequest {
    protected String connectionId;
    protected SimpleRequestType requestType;

}
