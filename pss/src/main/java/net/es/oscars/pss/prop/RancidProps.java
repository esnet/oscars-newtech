package net.es.oscars.pss.prop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@ConfigurationProperties(prefix = "rancid")
@Data
@Component
@NoArgsConstructor
public class RancidProps {


    @NonNull
    private Boolean execute;

    @NonNull
    private String dir;

    @NonNull
    private String host;

    @NonNull
    private String cloginrc;


    private String identityFile;

    private String username;

    private ArrayList<String> sshOptions = new ArrayList<>();

    @NonNull
    private String controlPlaneAddressesFile;


}


