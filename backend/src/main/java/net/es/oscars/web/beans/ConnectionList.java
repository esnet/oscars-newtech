package net.es.oscars.web.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.resv.ent.Connection;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionList {

    private int page;
    private int sizePerPage;
    private int totalSize;
    private List<Connection> connections;


}
