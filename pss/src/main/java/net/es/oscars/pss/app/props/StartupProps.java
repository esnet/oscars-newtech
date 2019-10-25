package net.es.oscars.pss.app.props;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties(StartupProps.class)
@ConfigurationProperties(prefix = "startup")
@NoArgsConstructor
public class StartupProps {
    @NonNull
    private Boolean exit = false;
    private String banner = "";
}



