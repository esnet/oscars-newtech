package net.es.oscars.pss.svc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.pss.beans.PssState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@Data
public class PssQueuer {
    private Map<String, PssState> waiting = new HashMap<>();
    private Map<String, PssState> working = new HashMap<>();
    private Map<String, List<PssState>> recent = new HashMap<>();


    private void buildOrDismantle(String connectionId, CommandType commandType) {
        // first case: we are working to build / dismantle and
        // are asked to do it again
        if (working.containsKey(connectionId)) {
            PssState inprogress = working.get(connectionId);
            if (commandType.equals(inprogress.getCommandType())) {
                log.info(connectionId+": ignoring duplicate command ");
            }
        }
        if (waiting.containsKey(connectionId)) {
            PssState waitAction = waiting.get(connectionId);

            if (waitAction.getCommandType().equals(CommandType.BUILD) && commandType.equals(CommandType.DISMANTLE)) {

            }
        }

    }


}
