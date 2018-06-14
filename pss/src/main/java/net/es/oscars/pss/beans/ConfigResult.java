package net.es.oscars.pss.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.pss.st.ConfigStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigResult {
    private String commands;
    private String output;
    private ConfigStatus status;
}
