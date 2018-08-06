package net.es.oscars.web.simple;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Validity validity;

}
