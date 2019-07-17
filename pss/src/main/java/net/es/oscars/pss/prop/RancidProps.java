package net.es.oscars.pss.prop;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RancidProps {


    @NonNull
    private Boolean perform;

    @NonNull
    private String dir;

    @NonNull
    private String host;

    @NonNull
    private String cloginrc;

    private Integer delay;


    private String identityFile;

    private String username;

    private ArrayList<String> sshOptions = new ArrayList<>();


}


