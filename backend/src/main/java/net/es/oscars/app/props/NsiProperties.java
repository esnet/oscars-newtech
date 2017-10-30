package net.es.oscars.app.props;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "nsi")
@Component
@NoArgsConstructor
public class NsiProperties {
    private String keyStore;
    private String keyStoreAlias;
    private String keyStorePassword;

}

