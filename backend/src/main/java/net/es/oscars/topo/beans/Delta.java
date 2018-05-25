package net.es.oscars.topo.beans;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Data
@Builder
public class Delta<T> {
    Map<String, T> added;
    Map<String, T> removed;
    Map<String, T> modified;
    Map<String, T> unchanged;

}
