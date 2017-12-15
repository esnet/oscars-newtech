package net.es.oscars.soap.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.ReserveType;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResvRequest extends GenericRequest {

    protected ReserveType reserveType;



}
