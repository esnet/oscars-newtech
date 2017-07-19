package net.es.oscars.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sec")
@NoArgsConstructor
public class SecProperties {
    @NonNull
    private String defaultAdminUsername;

    @NonNull
    private String defaultAdminPassword;

    @NonNull
    private String jwtSecret;

    @NonNull
    private Boolean secure = false;

}
