package net.es.oscars.pss.beans;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileMatch {
    @NonNull
    private List<String> criteria = new ArrayList<>();
    @NonNull
    private String profile;

}
