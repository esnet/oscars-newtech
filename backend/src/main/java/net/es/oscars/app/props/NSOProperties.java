package net.es.oscars.app.props;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "nso")
@Data
@Component
@NoArgsConstructor
public class NSOProperties {
    @NonNull
    private String url;

    @NonNull
    private String username;

    @NonNull
    private String password;




}
