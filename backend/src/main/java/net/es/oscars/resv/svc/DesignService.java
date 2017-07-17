package net.es.oscars.resv.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.beans.DesignResponse;
import net.es.oscars.resv.ent.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;


@Component
@Slf4j
public class DesignService {

    public DesignResponse verifyDesign(Design design) {
        DesignResponse dv = DesignResponse.builder()
                .valid(true)
                .errors(new ArrayList<>())
                .design(design)
                .build();

        return dv;
    }



}
