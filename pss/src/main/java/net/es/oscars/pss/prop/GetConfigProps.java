package net.es.oscars.pss.prop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class GetConfigProps {

    @NonNull
    private Boolean perform;

    @NonNull
    private String host;

    @NonNull
    private String path;
}


