package net.es.oscars.nsi.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NsiHoldResult {
    private Boolean success;
    private String errorMessage;
    private NsiErrors errorCode;
}
