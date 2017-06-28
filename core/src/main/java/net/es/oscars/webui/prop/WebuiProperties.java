package net.es.oscars.webui.prop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "webui")
@NoArgsConstructor
public class WebuiProperties {
    @NonNull
    private Boolean devMode = false;

}
