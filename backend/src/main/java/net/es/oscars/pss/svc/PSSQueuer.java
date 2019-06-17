package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;

import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.pss.beans.QueueName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class PSSQueuer {

    public void process() {

    }
    public void clear() {

    }
    public void forceCompletion() {

    }
    public void add(CommandType ct, String connId, String deviceUrn) {

    }
    public List<String> entries(QueueName qn) {
        return new ArrayList<>();
    }

}