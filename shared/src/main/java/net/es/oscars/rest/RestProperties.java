package net.es.oscars.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "rest")
@Component
@NoArgsConstructor
public class RestProperties {
    @NonNull
    private String internalUsername;
    @NonNull
    private String internalPassword;
    @NonNull
    private String internalTruststorePath;
}
