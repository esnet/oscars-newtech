package net.es.oscars.app.props;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "pss")
@Data
@Component
@NoArgsConstructor
public class PssProperties {

    @NonNull
    private String url;

    @NonNull
    private String community;

    @NonNull
    private String vcidRange;

    @NonNull
    private String aluSvcidRange;

    @NonNull
    private String aluSdpidRange;

    @NonNull
    private String aluQosidRange;

    @NonNull
    private String loopbackRange;

    @NonNull
    private Integer controlPlaneCheckRandom;

    @NonNull
    private Boolean controlPlaneCheckOnStart;

}