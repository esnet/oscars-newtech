package net.es.oscars.pss.prop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.es.oscars.pss.beans.UrnMappingMethod;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "controlplane")
@Data
@Component
@NoArgsConstructor
public class UrnMappingProps {

    @NonNull
    private UrnMappingMethod method;

    private String addressesFile;

    private String dnsSuffix;

}


