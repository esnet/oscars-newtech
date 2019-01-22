package net.es.oscars.snp.svc;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.SnippetException;
import net.es.oscars.dto.pss.st.ConfigStatus;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.resv.ent.VlanFixture;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.snp.beans.CmpDelta;
import net.es.oscars.snp.beans.ConfigNodeModifyType;
import net.es.oscars.snp.beans.ConfigNodeType;
import net.es.oscars.snp.beans.SnippetStatus;
import net.es.oscars.snp.ent.ConfigSnippet;
import net.es.oscars.snp.ent.DeviceConfigNode;
import net.es.oscars.snp.ent.DeviceConfigState;
import net.es.oscars.snp.ent.Modify;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class SnippetSvc implements SnippetAPI {

    Map<String, DeviceConfigState> configStateStore = new HashMap<>();

    public void setDeviceConfigState(String connId, DeviceConfigState dcs) {
        this.configStateStore.put(connId, dcs);
        log.info("configStateStore is ");
        log.info(String.valueOf(this.configStateStore));
    }

    public Pair<List<Modify>, List<DeviceConfigNode>> decide(CmpDelta delta) {
        List<Modify> modifyList = new ArrayList<>();
        List<DeviceConfigNode> rootNodes = new ArrayList<>();

        // ACTION 1: adding junctions
        Map<String, VlanJunction> addedJunctions = delta.getJunctionDelta().getAdded();

        for (String deviceUrn : addedJunctions.keySet()) {
            VlanJunction vj = addedJunctions.get(deviceUrn);
            String connId = vj.getConnectionId();
            DeviceConfigNode connConfigRootNode = this.getRootConfigNode(connId, deviceUrn);

            rootNodes.add(connConfigRootNode);

            // TODO: decide the model from the topology
            DeviceModel m = DeviceModel.ALCATEL_SR7750;

            if (m.equals(DeviceModel.ALCATEL_SR7750)) {
                // ALU case
                // find the VPLS node; error out if not there
                boolean hasVPLS = false;
                for (DeviceConfigNode n : connConfigRootNode.getDownstream()) {
                    if (n.getType().equals(ConfigNodeType.ALU_VPLS_SVC)) {
                        hasVPLS = true;
                    }
                }

                if (!hasVPLS) {
                    Set<String> upstreamNodeIds = new HashSet<>();
                    upstreamNodeIds.add(connConfigRootNode.getNodeId());

                    Set<DeviceConfigNode> upstreams = new HashSet<>();
                    upstreams.add(connConfigRootNode);

                    DeviceConfigNode vplsNode = DeviceConfigNode.builder()
                            .nodeId(UUID.randomUUID().toString())
                            .type(ConfigNodeType.ALU_VPLS_SVC)
                            .downstream(new HashSet<>())
                            .upstream(upstreams)
                            .build();

                    Modify addVplsAction = Modify.builder()
                            .configState(ConfigStatus.NONE)
                            .node(vplsNode)
                            .upstreamNodeIds(upstreamNodeIds)
                            .type(ConfigNodeModifyType.ADD)
                            .mutate("")
                            .build();
                    modifyList.add(addVplsAction);
                } else {
                    //handle error. VPLS already exists
                    String error = "Invalid request. VPLS node already exists";
                }
            } else if (m.equals(DeviceModel.JUNIPER_MX)) {

            }
        }

        // TODO: ACTION 2 : removing junctions

        // TODO: ACTION 3 : modifying junctions

        // ACTION 1: adding fixtures
        Map<String, VlanFixture> addedFixtures = delta.getFixtureDelta().getAdded();

        for (String deviceUrn : addedFixtures.keySet()) {
            // TODO: decide the model from the topology
            DeviceModel m = DeviceModel.ALCATEL_SR7750;
            VlanFixture vj = addedFixtures.get(deviceUrn);
            String connId = vj.getConnectionId();

            DeviceConfigState cs = this.configStateStore.get(deviceUrn);
            DeviceConfigNode connConfigRootNode = this.getRootConfigNode(connId, deviceUrn);

            rootNodes.add(connConfigRootNode);

            if (m.equals(DeviceModel.ALCATEL_SR7750)) {
                // ALU case

                // First find the VPLS node
                // Then, when that is found, add SAP fixture to VPLS

                DeviceConfigNode vpls = null;
                for (DeviceConfigNode n : connConfigRootNode.getDownstream()) {
                    if (n.getType().equals(ConfigNodeType.ALU_VPLS_SVC)) {
                        vpls = n;
                    }
                }

                // If VPLS found
                if (vpls != null) {
                    Set<String> upstreamNodeIds = new HashSet<>();
                    upstreamNodeIds.add(vpls.getNodeId());

                    Set<DeviceConfigNode> upstreams = new HashSet<>();
                    upstreams.add(vpls);

                    DeviceConfigNode sapNode = DeviceConfigNode.builder()
                            .nodeId(UUID.randomUUID().toString())
                            .type(ConfigNodeType.ALU_SAP)
                            .downstream(new HashSet<>())
                            .upstream(upstreams)
                            .build();

                    Modify addSAPAction = Modify.builder()
                            .configState(ConfigStatus.NONE)
                            .node(sapNode)
                            .upstreamNodeIds(upstreamNodeIds)
                            .type(ConfigNodeModifyType.ADD)
                            .mutate("")
                            .build();

                    modifyList.add(addSAPAction);
                } else {
                    // handle error. No VPLS exists
                }
            } else if (m.equals(DeviceModel.JUNIPER_MX)) {

            }
        }

        // TODO: delete / modify fixtures

        // TODO: add / delete / modify pipes
        // TODO: add / delete IP addresses

        return new Pair<>(modifyList, rootNodes);
    }


    private DeviceConfigNode getRootConfigNode(String connId, String deviceUrn) {

        DeviceConfigState cs = this.configStateStore.get(deviceUrn);
        if (cs == null) {
            cs = DeviceConfigState.builder()
                    .urn(deviceUrn)
                    .model(DeviceModel.ALCATEL_SR7750)
                    .connectionConfigNodes(new HashMap<>())
                    .build();
            this.configStateStore.put(deviceUrn, cs);
        }

        if (cs.getConnectionConfigNodes().containsKey(connId)) {
            return cs.getConnectionConfigNodes().get(connId);
        } else {

            DeviceConfigNode connConfigRootNode = DeviceConfigNode.builder()
                    .nodeId(UUID.randomUUID().toString())
                    .type(ConfigNodeType.ROOT)
                    .downstream(new HashSet<>())
                    .upstream(new HashSet<>())
                    .build();
            cs.getConnectionConfigNodes().put(connId, connConfigRootNode);
            return connConfigRootNode;
        }

    }

    public void commitModifications(List<DeviceConfigNode> rootNodes, List<Modify> modifyList) {
//        log.info(String.valueOf(rootNodes));
//        log.info(String.valueOf(modifyList));

        int count = 0;
        for (Modify m : modifyList) {
            if (m.getType().equals(ConfigNodeModifyType.ADD)) {
                DeviceConfigNode node = m.getNode();
                DeviceConfigNode rootNode;
                rootNode = rootNodes.get(count);
                Set<String> upstreamNodeIds = m.getUpstreamNodeIds();
                Set<DeviceConfigNode> upstreamNodes = this.lookupConfigNodes(rootNode, upstreamNodeIds);
                for (DeviceConfigNode upstreamNode : upstreamNodes) {
                    upstreamNode.getDownstream().add(node);
                }
            } else if (m.getType().equals(ConfigNodeModifyType.DELETE)) {
                DeviceConfigNode node = m.getNode();
                DeviceConfigNode rootNode = rootNodes.get(count);
                Set<String> upstreamNodeIds = m.getUpstreamNodeIds();
                Set<DeviceConfigNode> upstreamNodes = this.lookupConfigNodes(rootNode, upstreamNodeIds);
                for (DeviceConfigNode upstreamNode : upstreamNodes) {
                    upstreamNode.getDownstream().remove(node);
                }
            } else if (m.getType().equals(ConfigNodeModifyType.MUTATE)) {
                // TODO
            }
            count++;
        }
    }

    public Set<DeviceConfigNode> lookupConfigNodes(DeviceConfigNode rootNode, Set<String> upstreamNodeIds) {
        log.info(String.valueOf(rootNode));
        log.info(String.valueOf(upstreamNodeIds));

        Set<DeviceConfigNode> upstreamNodes = new HashSet<>();
        Map<String, Boolean> visited = new HashMap<>();
        LinkedList<DeviceConfigNode> queue = new LinkedList<>();

        // Create a queue for BFS
        visited.put(rootNode.getNodeId(), true);
        queue.add(rootNode);

        while (queue.size() != 0)
        {
            // Dequeue a vertex from queue and print it
            DeviceConfigNode s = queue.poll();

            if (upstreamNodeIds.contains(s.getNodeId())) {
                upstreamNodes.add(s);
            }

            // Get all adjacent vertices of the dequeue vertex s
            // If a adjacent has not been visited, then mark it
            // visited and enqueue it
            for (DeviceConfigNode node : s.getDownstream()) {
                if(!visited.containsKey(node.getNodeId())) {
                    visited.put(node.getNodeId(), true);
                    queue.add(node);
                }
            }
        }
        return upstreamNodes;
    }

    public Set<ConfigSnippet> generateSnippets(List<Modify> modifyList) throws SnippetException {
        Set<ConfigSnippet> result = new HashSet<>();
        // this.validateModifications(modifyList);
        return result;
    }

    public boolean validateModifications(List<Modify> modifyList) throws SnippetException {
        boolean foundError = false;
        for (Modify m : modifyList) {
            if (m.getType().equals(ConfigNodeModifyType.ADD)) {
                // check that nodeId exists
            } else if (m.getType().equals(ConfigNodeModifyType.DELETE)) {
                // check that nodeId exists
            } else if (m.getType().equals(ConfigNodeModifyType.MUTATE)) {
                // check that nodeId does NOT exist or it is null & i assign it
            }
        }
        return foundError;
    }

    public boolean commitChanges(Set<ConfigSnippet> configList) throws SnippetException {
        return true;
    }

    public void updateSnippetStatus(Set<ConfigSnippet> snippets) {
        // verify consistency & do DB write
    }

    public Set<ConfigSnippet> generateNeededSnippets(String deviceUrn, CmpDelta delta) throws SnippetException {
        // Validate Deltas
        // this.validateDeltas(delta);

        Set<ConfigSnippet> result = new HashSet<>();

        // Add, Remove and Modify Junctions

        Map<String, VlanJunction> addedJunctions = delta.getJunctionDelta().getAdded();
        Set<ConfigSnippet> addJunctionSnippets = this.addingJunctions(addedJunctions);
        result.addAll(addJunctionSnippets);

//        Map<String, VlanJunction> removedJunctions = delta.getJunctionDelta().getRemoved();
//        Set<ConfigSnippet> removeJunctionSnippets = this.removingJunctions(removedJunctions);
//        result.addAll(removeJunctionSnippets);

//        Map<String, VlanJunction> modifiedJunctions = delta.getJunctionDelta().getModified();
//        Set<ConfigSnippet> modifyJunctionSnippets = this.removingJunctions(modifiedJunctions);
//        result.addAll(modifyJunctionSnippets);

        // similar for pipes & fixtures

        return result;
    }

    // Similar for removing, modifying Junctions / Fixtures / Pipes
    private Set<ConfigSnippet> addingJunctions(Map<String, VlanJunction> addedJunctions) {
        Set<ConfigSnippet> result = new HashSet<>();
        for (String jKey : addedJunctions.keySet()) {
            VlanJunction j = addedJunctions.get(jKey);

            ConfigSnippet userConfigSnippet = ConfigSnippet.builder()
                    .buildConfigText("set user XYZ password ABC")
                    .dismantleConfigText("no user XYZ")
                    .dependOnThis(new HashSet<>())
                    .dependsOn(new HashSet<>())
                    .status(SnippetStatus.GENERATED)
                    .build();

            String snippetKey = j.getConnectionId() + ":" + j.getDeviceUrn();
//            this.snippetStore.put(snippetKey, userConfigSnippet);
            result.add(userConfigSnippet);
        }

        return result;
    }
}
