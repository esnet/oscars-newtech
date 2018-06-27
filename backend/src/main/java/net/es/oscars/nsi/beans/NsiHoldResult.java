package net.es.oscars.nsi.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NsiHoldResult {
    private Boolean success;
    private String errorMessage;
    private NsiErrors errorCode;
}
