package net.es.oscars.pss.prop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.es.oscars.pss.beans.PssProfile;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "pss")
@Data
@Component
@NoArgsConstructor
public class PssProps {

    @NonNull
    private List<PssProfile> profiles = new ArrayList<>();

}


