package net.es.oscars.pss.beans;

import lombok.Data;
import net.es.oscars.dto.pss.cmd.CommandType;

import java.time.Instant;

@Data
public class PssState {
    private CommandType commandType;
    private PssStatus status;
    private Instant at;
    private Instant expire;

}
