package net.es.oscars.pss.app;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.prop.PssProps;
import net.es.oscars.pss.svc.CommandQueuer;
import net.es.oscars.pss.svc.UrnMappingService;
import net.es.oscars.pss.svc.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Startup {

    private CommandQueuer queuer;
    private PssProps props;
    private HealthService healthService;
    private UrnMappingService urnMappingService;

    @Autowired
    public Startup(CommandQueuer queuer, HealthService healthService,
                   UrnMappingService urnMappingService, PssProps props) {
        this.queuer = queuer;
        this.props = props;
        this.healthService = healthService;
        this.urnMappingService = urnMappingService;
    }


}
