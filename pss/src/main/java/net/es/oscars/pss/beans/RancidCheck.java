package net.es.oscars.pss.beans;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RancidCheck {
    @NonNull
    private List<DeviceEntry> devices = new ArrayList<>();
    @NonNull
    private Boolean perform;
}
