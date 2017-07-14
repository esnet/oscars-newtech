package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.Blueprint;
import net.es.oscars.resv.db.BlueprintRepository;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.svc.ResvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;


@RestController
@Slf4j
public class ResvController {
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private BlueprintRepository blueprintRepository;

    @Autowired
    private ResvService resvService;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }
    @RequestMapping(value = "/api/design", method = RequestMethod.POST)
    @ResponseBody
    public Blueprint design(@RequestBody Blueprint newDesign) {
        List<Blueprint> bps = blueprintRepository.findByConnectionId(newDesign.getConnectionId());
        // delete existing if it's there
        for (Blueprint bp : bps) {
            if (bp.getSchedule().getPhase().equals(Phase.DESIGN)) {
                log.info("found an existing design; deleting");
                blueprintRepository.delete(bp);
            }
        }
        newDesign.getSchedule().setPhase(Phase.DESIGN);

        newDesign = blueprintRepository.save(newDesign);

        return newDesign;
    }


    @RequestMapping(value = "/api/hold/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public Blueprint hold(@PathVariable String connectionId) {
        List<Blueprint> bps = blueprintRepository.findByConnectionId(connectionId);
        Blueprint design = null;
        for (Blueprint bp : bps) {
            if (bp.getSchedule().getPhase().equals(Phase.DESIGN)) {
                design = bp;
                break;
            }
        }
        if (design == null) {
            throw new NoSuchElementException("could not find design for "+connectionId);
        }
        // copy design data & populate
        Blueprint held = resvService.designToHeld(design);
        blueprintRepository.save(held);
        return held;
    }



}