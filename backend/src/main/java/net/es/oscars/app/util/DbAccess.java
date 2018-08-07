package net.es.oscars.app.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@Slf4j
@Data
public class DbAccess {
    private ReadWriteLock connLock = new ReentrantReadWriteLock();
    private ReadWriteLock topoLock = new ReentrantReadWriteLock();

}
