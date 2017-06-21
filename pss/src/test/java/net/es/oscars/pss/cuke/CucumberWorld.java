package net.es.oscars.pss.cuke;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CucumberWorld {
    private List<Exception> exceptions = new ArrayList<>();
    private boolean expectException;

    public void expectException() {
        expectException = true;
    }

    public void add(Exception e) throws RuntimeException {
        if (!expectException) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        exceptions.add(e);
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }


}
