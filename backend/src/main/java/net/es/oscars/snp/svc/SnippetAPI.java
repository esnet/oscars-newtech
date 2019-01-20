package net.es.oscars.snp.svc;

import javafx.util.Pair;
import net.es.oscars.app.exc.SnippetException;
import net.es.oscars.snp.beans.CmpDelta;
import net.es.oscars.snp.ent.ConfigSnippet;
import net.es.oscars.snp.ent.DeviceConfigNode;
import net.es.oscars.snp.ent.DeviceConfigState;
import net.es.oscars.snp.ent.Modify;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SnippetAPI {
    Pair<List<Modify>, List<DeviceConfigNode>> decide(CmpDelta delta) throws SnippetException;

    Set<ConfigSnippet> generateSnippets(List<Modify> modifyList) throws SnippetException;

    void commitModifications(List<DeviceConfigNode> rootNodes, List<Modify> modifyList) throws SnippetException;
    void setDeviceConfigState(String connId, DeviceConfigState dcs);

    boolean validateModifications(List<Modify> modifyList) throws SnippetException;
    boolean commitChanges(Set<ConfigSnippet> configList) throws SnippetException;
}