package net.es.oscars.topo.beans;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@Builder
public class Delta<T> {
    List<T> added;
    List<T> removed;
    List<T> modified;
    List<T> unchanged;

}
