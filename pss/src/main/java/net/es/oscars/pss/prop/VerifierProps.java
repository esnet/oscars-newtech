package net.es.oscars.pss.prop;

import lombok.*;
import net.es.oscars.pss.beans.UrnMappingEntry;
import net.es.oscars.pss.beans.UrnMappingMethod;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "verify")
@Component
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifierProps {


    private String aparsePath;
    private Integer cacheLifetime;


}


