package net.es.oscars.app.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
@Data
public class DbAccess {
    private ReentrantLock connLock = new ReentrantLock();
    private ReentrantLock topoLock = new ReentrantLock();

}
