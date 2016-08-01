package net.es.oscars.topo;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.RepoEntityBuilder;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.VertexType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Jeremy on 7/8/2016.
 */
@Slf4j
@Component
public class AsymmTopologyBuilder
{
    @Autowired
    private RepoEntityBuilder testBuilder;


    public void buildAsymmTopo1()
    {
        log.info("Building Asymmetric Test Topology 1");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> portBWs = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeN = new TopoVertex("nodeN", VertexType.SWITCH);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portN1 = new TopoVertex("nodeN:1", VertexType.PORT);
        TopoVertex portN2 = new TopoVertex("nodeN:2", VertexType.PORT);

        // Asymmetric Bandwidth Capacity  (Ingress - Egress) //
        List<Integer> bwPortA = Arrays.asList(100, 100);
        List<Integer> bwPortZ = Arrays.asList(100, 100);
        List<Integer> bwPortK1 = Arrays.asList(20, 60);
        List<Integer> bwPortL1 = Arrays.asList(60, 20);
        List<Integer> bwPortL2 = Arrays.asList(20, 60);
        List<Integer> bwPortL3 = Arrays.asList(20, 60);
        List<Integer> bwPortM1 = Arrays.asList(60, 20);
        List<Integer> bwPortM2 = Arrays.asList(60, 20);
        List<Integer> bwPortN1 = Arrays.asList(60, 20);
        List<Integer> bwPortN2 = Arrays.asList(20, 60);
        portBWs.put(portA, bwPortA);
        portBWs.put(portZ, bwPortZ);
        portBWs.put(portK1, bwPortK1);
        portBWs.put(portL1, bwPortL1);
        portBWs.put(portL2, bwPortL2);
        portBWs.put(portL3, bwPortL3);
        portBWs.put(portM1, bwPortM1);
        portBWs.put(portM2, bwPortM2);
        portBWs.put(portN1, bwPortN1);
        portBWs.put(portN2, bwPortN2);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_M = new TopoEdge(portZ, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_Z = new TopoEdge(nodeM, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N1_N = new TopoEdge(portN1, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N2_N = new TopoEdge(portN2, nodeN, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N1 = new TopoEdge(nodeN, portN1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N2 = new TopoEdge(nodeN, portN2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L2_M1 = new TopoEdge(portL2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L3_N1 = new TopoEdge(portL3, portN1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_L2 = new TopoEdge(portM1, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_N2 = new TopoEdge(portM2, portN2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N1_L3 = new TopoEdge(portN1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N2_M2 = new TopoEdge(portN2, portM2, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeN);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portN1);
        topoNodes.add(portN2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_M);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_N1_N);
        topoLinks.add(edgeInt_N2_N);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_M_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_N_N1);
        topoLinks.add(edgeInt_N_N2);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_L1_K1);
        topoLinks.add(edgeEth_L2_M1);
        topoLinks.add(edgeEth_L3_N1);
        topoLinks.add(edgeEth_M1_L2);
        topoLinks.add(edgeEth_M2_N2);
        topoLinks.add(edgeEth_N1_L3);
        topoLinks.add(edgeEth_N2_M2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, portBWs);
    }

    public void buildAsymmTopo2()
    {
        log.info("Building Asymmetric Test Topology 2");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> portBWs = new HashMap<>();

        // Devices //
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portQ3 = new TopoVertex("nodeQ:3", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);
        TopoVertex portS2 = new TopoVertex("nodeS:2", VertexType.PORT);

        // Asymmetric Bandwidth Capacity  (Ingress - Egress) //
        List<Integer> bwPortA = Arrays.asList(100, 100);
        List<Integer> bwPortZ = Arrays.asList(100, 100);
        List<Integer> bwPortP1 = Arrays.asList(20, 60);
        List<Integer> bwPortQ1 = Arrays.asList(60, 20);
        List<Integer> bwPortQ2 = Arrays.asList(20, 60);
        List<Integer> bwPortQ3 = Arrays.asList(20, 60);
        List<Integer> bwPortR1 = Arrays.asList(60, 20);
        List<Integer> bwPortR2 = Arrays.asList(60, 20);
        List<Integer> bwPortS1 = Arrays.asList(60, 20);
        List<Integer> bwPortS2 = Arrays.asList(20, 60);
        portBWs.put(portA, bwPortA);
        portBWs.put(portZ, bwPortZ);
        portBWs.put(portP1, bwPortP1);
        portBWs.put(portQ1, bwPortQ1);
        portBWs.put(portQ2, bwPortQ2);
        portBWs.put(portQ3, bwPortQ3);
        portBWs.put(portR1, bwPortR1);
        portBWs.put(portR2, bwPortR2);
        portBWs.put(portS1, bwPortS1);
        portBWs.put(portS2, bwPortS2);

        // End-Port Links //
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_S = new TopoEdge(portZ, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_Z = new TopoEdge(nodeS, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S2_S = new TopoEdge(portS2, nodeS, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S2 = new TopoEdge(nodeS, portS2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_P1_Q1 = new TopoEdge(portP1, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P1 = new TopoEdge(portQ1, portP1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_S1 = new TopoEdge(portQ2, portS1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q3_R1 = new TopoEdge(portQ3, portR1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R1_Q3 = new TopoEdge(portR1, portQ3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R2_S2 = new TopoEdge(portR2, portS2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S1_Q2 = new TopoEdge(portS1, portQ2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S2_R2 = new TopoEdge(portS2, portR2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portP1);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portQ3);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portS1);
        topoNodes.add(portS2);

        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_S);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_S1_S);
        topoLinks.add(edgeInt_S2_S);

        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_S_Z);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_S_S1);
        topoLinks.add(edgeInt_S_S2);

        topoLinks.add(edgeMpls_P1_Q1);
        topoLinks.add(edgeMpls_Q1_P1);
        topoLinks.add(edgeMpls_Q3_R1);
        topoLinks.add(edgeMpls_Q2_S1);
        topoLinks.add(edgeMpls_R1_Q3);
        topoLinks.add(edgeMpls_R2_S2);
        topoLinks.add(edgeMpls_S1_Q2);
        topoLinks.add(edgeMpls_S2_R2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, portBWs);
    }

    public void buildAsymmTopo3()
    {
        log.info("Building Asymmetric Test Topology 3");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> portBWs = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);
        TopoVertex portS2 = new TopoVertex("nodeS:2", VertexType.PORT);

        // Asymmetric Bandwidth Capacity  (Ingress - Egress) //
        List<Integer> bwPortA = Arrays.asList(100, 100);
        List<Integer> bwPortZ = Arrays.asList(100, 100);
        List<Integer> bwPortK1 = Arrays.asList(20, 60);
        List<Integer> bwPortK2 = Arrays.asList(30, 40);
        List<Integer> bwPortQ1 = Arrays.asList(60, 20);
        List<Integer> bwPortQ2 = Arrays.asList(20, 60);
        List<Integer> bwPortR1 = Arrays.asList(40, 30);
        List<Integer> bwPortR2 = Arrays.asList(30, 40);
        List<Integer> bwPortS1 = Arrays.asList(60, 20);
        List<Integer> bwPortS2 = Arrays.asList(40, 30);
        portBWs.put(portA, bwPortA);
        portBWs.put(portZ, bwPortZ);
        portBWs.put(portK1, bwPortK1);
        portBWs.put(portK2, bwPortK2);
        portBWs.put(portQ1, bwPortQ1);
        portBWs.put(portQ2, bwPortQ2);
        portBWs.put(portR1, bwPortR1);
        portBWs.put(portR2, bwPortR2);
        portBWs.put(portS1, bwPortS1);
        portBWs.put(portS2, bwPortS2);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_S = new TopoEdge(portZ, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_Z = new TopoEdge(nodeS, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S2_S = new TopoEdge(portS2, nodeS, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S2 = new TopoEdge(nodeS, portS2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_Q1 = new TopoEdge(portK1, portQ1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_K2_R1 = new TopoEdge(portK2, portR1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q1_K1 = new TopoEdge(portQ1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_Q2_S1 = new TopoEdge(portQ2, portS1, 100L, Layer.MPLS);
        TopoEdge edgeEth_R1_K2 = new TopoEdge(portR1, portK2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_R2_S2 = new TopoEdge(portR2, portS2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S1_Q2 = new TopoEdge(portS1, portQ2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S2_R2 = new TopoEdge(portS2, portR2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portS1);
        topoNodes.add(portS2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_S);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_S1_S);
        topoLinks.add(edgeInt_S2_S);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_S_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_S_S1);
        topoLinks.add(edgeInt_S_S2);

        topoLinks.add(edgeEth_K1_Q1);
        topoLinks.add(edgeEth_K2_R1);
        topoLinks.add(edgeEth_Q1_K1);
        topoLinks.add(edgeMpls_Q2_S1);
        topoLinks.add(edgeEth_R1_K2);
        topoLinks.add(edgeMpls_R2_S2);
        topoLinks.add(edgeMpls_S1_Q2);
        topoLinks.add(edgeMpls_S2_R2);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, portBWs);
    }

    public void buildAsymmTopo4()
    {
        log.info("Building Asymmetric Test Topology 4");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> portBWs = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeN = new TopoVertex("nodeN", VertexType.SWITCH);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portN1 = new TopoVertex("nodeN:1", VertexType.PORT);
        TopoVertex portN2 = new TopoVertex("nodeN:2", VertexType.PORT);

        // Asymmetric Bandwidth Capacity  (Ingress - Egress) //
        List<Integer> bwPortA = Arrays.asList(100, 100);
        List<Integer> bwPortZ = Arrays.asList(100, 100);
        List<Integer> bwPortK1 = Arrays.asList(100, 100);
        List<Integer> bwPortL1 = Arrays.asList(100, 100);
        List<Integer> bwPortL2 = Arrays.asList(20, 60);
        List<Integer> bwPortL3 = Arrays.asList(30, 40);
        List<Integer> bwPortM1 = Arrays.asList(60, 20);
        List<Integer> bwPortM2 = Arrays.asList(40, 30);
        List<Integer> bwPortN1 = Arrays.asList(40, 30);
        List<Integer> bwPortN2 = Arrays.asList(30, 40);
        portBWs.put(portA, bwPortA);
        portBWs.put(portZ, bwPortZ);
        portBWs.put(portK1, bwPortK1);
        portBWs.put(portL1, bwPortL1);
        portBWs.put(portL2, bwPortL2);
        portBWs.put(portL3, bwPortL3);
        portBWs.put(portM1, bwPortM1);
        portBWs.put(portM2, bwPortM2);
        portBWs.put(portN1, bwPortN1);
        portBWs.put(portN2, bwPortN2);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_M = new TopoEdge(portZ, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_Z = new TopoEdge(nodeM, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N1_N = new TopoEdge(portN1, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N2_N = new TopoEdge(portN2, nodeN, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N1 = new TopoEdge(nodeN, portN1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N2 = new TopoEdge(nodeN, portN2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L2_M1 = new TopoEdge(portL2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L3_N1 = new TopoEdge(portL3, portN1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_L2 = new TopoEdge(portM1, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_N2 = new TopoEdge(portM2, portN2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N1_L3 = new TopoEdge(portN1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N2_M2 = new TopoEdge(portN2, portM2, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeN);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portN1);
        topoNodes.add(portN2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_M);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_N1_N);
        topoLinks.add(edgeInt_N2_N);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_M_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_N_N1);
        topoLinks.add(edgeInt_N_N2);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_L1_K1);
        topoLinks.add(edgeEth_L2_M1);
        topoLinks.add(edgeEth_L3_N1);
        topoLinks.add(edgeEth_M1_L2);
        topoLinks.add(edgeEth_M2_N2);
        topoLinks.add(edgeEth_N1_L3);
        topoLinks.add(edgeEth_N2_M2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, portBWs);
    }

    public void buildAsymmTopo5()
    {
        log.info("Building Asymmetric Test Topology 5");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> portBWs = new HashMap<>();

        // Devices //
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portQ3 = new TopoVertex("nodeQ:3", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);
        TopoVertex portS2 = new TopoVertex("nodeS:2", VertexType.PORT);

        // Asymmetric Bandwidth Capacity  (Ingress - Egress) //
        List<Integer> bwPortA = Arrays.asList(100, 100);
        List<Integer> bwPortZ = Arrays.asList(100, 100);
        List<Integer> bwPortP1 = Arrays.asList(100, 100);
        List<Integer> bwPortQ1 = Arrays.asList(100, 100);
        List<Integer> bwPortQ2 = Arrays.asList(20, 60);
        List<Integer> bwPortQ3 = Arrays.asList(30, 40);
        List<Integer> bwPortR1 = Arrays.asList(40, 30);
        List<Integer> bwPortR2 = Arrays.asList(30, 40);
        List<Integer> bwPortS1 = Arrays.asList(60, 20);
        List<Integer> bwPortS2 = Arrays.asList(40, 30);
        portBWs.put(portA, bwPortA);
        portBWs.put(portZ, bwPortZ);
        portBWs.put(portP1, bwPortP1);
        portBWs.put(portQ1, bwPortQ1);
        portBWs.put(portQ2, bwPortQ2);
        portBWs.put(portQ3, bwPortQ3);
        portBWs.put(portR1, bwPortR1);
        portBWs.put(portR2, bwPortR2);
        portBWs.put(portS1, bwPortS1);
        portBWs.put(portS2, bwPortS2);

        // End-Port Links //
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_S = new TopoEdge(portZ, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_Z = new TopoEdge(nodeS, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S2_S = new TopoEdge(portS2, nodeS, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S2 = new TopoEdge(nodeS, portS2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_P1_Q1 = new TopoEdge(portP1, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P1 = new TopoEdge(portQ1, portP1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_S1 = new TopoEdge(portQ2, portS1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q3_R1 = new TopoEdge(portQ3, portR1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R1_Q3 = new TopoEdge(portR1, portQ3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R2_S2 = new TopoEdge(portR2, portS2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S1_Q2 = new TopoEdge(portS1, portQ2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S2_R2 = new TopoEdge(portS2, portR2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portP1);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portQ3);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portS1);
        topoNodes.add(portS2);

        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_S);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_S1_S);
        topoLinks.add(edgeInt_S2_S);

        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_S_Z);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_S_S1);
        topoLinks.add(edgeInt_S_S2);

        topoLinks.add(edgeMpls_P1_Q1);
        topoLinks.add(edgeMpls_Q1_P1);
        topoLinks.add(edgeMpls_Q3_R1);
        topoLinks.add(edgeMpls_Q2_S1);
        topoLinks.add(edgeMpls_R1_Q3);
        topoLinks.add(edgeMpls_R2_S2);
        topoLinks.add(edgeMpls_S1_Q2);
        topoLinks.add(edgeMpls_S2_R2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, portBWs);
    }

    public void buildAsymmTopo6()
    {
        log.info("Building Asymmetric Test Topology 6");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> portBWs = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portQ3 = new TopoVertex("nodeQ:3", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);
        TopoVertex portS2 = new TopoVertex("nodeS:2", VertexType.PORT);

        // Asymmetric Bandwidth Capacity  (Ingress - Egress) //
        List<Integer> bwPortA = Arrays.asList(100, 100);
        List<Integer> bwPortZ = Arrays.asList(100, 100);
        List<Integer> bwPortK1 = Arrays.asList(100, 100);
        List<Integer> bwPortQ1 = Arrays.asList(100, 100);
        List<Integer> bwPortQ2 = Arrays.asList(20, 60);
        List<Integer> bwPortQ3 = Arrays.asList(30, 40);
        List<Integer> bwPortR1 = Arrays.asList(40, 30);
        List<Integer> bwPortR2 = Arrays.asList(30, 40);
        List<Integer> bwPortS1 = Arrays.asList(60, 20);
        List<Integer> bwPortS2 = Arrays.asList(40, 30);
        portBWs.put(portA, bwPortA);
        portBWs.put(portZ, bwPortZ);
        portBWs.put(portK1, bwPortK1);
        portBWs.put(portQ1, bwPortQ1);
        portBWs.put(portQ2, bwPortQ2);
        portBWs.put(portQ3, bwPortQ3);
        portBWs.put(portR1, bwPortR1);
        portBWs.put(portR2, bwPortR2);
        portBWs.put(portS1, bwPortS1);
        portBWs.put(portS2, bwPortS2);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_S = new TopoEdge(portZ, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_Z = new TopoEdge(nodeS, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S2_S = new TopoEdge(portS2, nodeS, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S2 = new TopoEdge(nodeS, portS2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_Q1 = new TopoEdge(portK1, portQ1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q1_K1 = new TopoEdge(portQ1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_Q2_S1 = new TopoEdge(portQ2, portS1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q3_R1 = new TopoEdge(portQ3, portR1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R1_Q3 = new TopoEdge(portR1, portQ3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R2_S2 = new TopoEdge(portR2, portS2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S1_Q2 = new TopoEdge(portS1, portQ2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S2_R2 = new TopoEdge(portS2, portR2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portQ3);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portS1);
        topoNodes.add(portS2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_S);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_S1_S);
        topoLinks.add(edgeInt_S2_S);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_S_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_S_S1);
        topoLinks.add(edgeInt_S_S2);

        topoLinks.add(edgeEth_K1_Q1);
        topoLinks.add(edgeEth_Q1_K1);
        topoLinks.add(edgeMpls_Q2_S1);
        topoLinks.add(edgeMpls_Q3_R1);
        topoLinks.add(edgeMpls_R1_Q3);
        topoLinks.add(edgeMpls_R2_S2);
        topoLinks.add(edgeMpls_S1_Q2);
        topoLinks.add(edgeMpls_S2_R2);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, portBWs);
    }

    public void buildAsymmTopo13()
    {
        log.info("Building Asymmetric Test Topology 13");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> portBWs = new HashMap<>();
        Map<TopoVertex, List<Integer>> floorMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> ceilingMap = new HashMap<>();

        // Devices //
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);
        TopoVertex portS2 = new TopoVertex("nodeS:2", VertexType.PORT);

        // Asymmetric Bandwidth Capacity  (Ingress - Egress) //
        List<Integer> bwPortA = Arrays.asList(500, 500);
        List<Integer> bwPortZ = Arrays.asList(500, 500);
        List<Integer> bwPortP1 = Arrays.asList(100, 100);
        List<Integer> bwPortP2 = Arrays.asList(100, 100);
        List<Integer> bwPortQ1 = Arrays.asList(100, 100);
        List<Integer> bwPortQ2 = Arrays.asList(100, 100);
        List<Integer> bwPortR1 = Arrays.asList(100, 100);
        List<Integer> bwPortR2 = Arrays.asList(100, 100);
        List<Integer> bwPortS1 = Arrays.asList(100, 100);
        List<Integer> bwPortS2 = Arrays.asList(100, 100);
        portBWs.put(portA, bwPortA);
        portBWs.put(portZ, bwPortZ);
        portBWs.put(portP1, bwPortP1);
        portBWs.put(portP2, bwPortP2);
        portBWs.put(portQ1, bwPortQ1);
        portBWs.put(portQ2, bwPortQ2);
        portBWs.put(portR1, bwPortR1);
        portBWs.put(portR2, bwPortR2);
        portBWs.put(portS1, bwPortS1);
        portBWs.put(portS2, bwPortS2);

        // End-Port Links //
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_R = new TopoEdge(portZ, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_Z = new TopoEdge(nodeR, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S2_S = new TopoEdge(portS2, nodeS, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S2 = new TopoEdge(nodeS, portS2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_P1_Q1 = new TopoEdge(portP1, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P2_S1 = new TopoEdge(portP2, portS1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P1 = new TopoEdge(portQ1, portP1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R1 = new TopoEdge(portQ2, portR1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R1_Q2 = new TopoEdge(portR1, portQ2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R2_S2 = new TopoEdge(portR2, portS2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S1_P2 = new TopoEdge(portS1, portP2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S2_R2 = new TopoEdge(portS2, portR2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();
        List<Integer> floors = Arrays.asList(1);
        List<Integer> ceilings = Arrays.asList(5);

        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portS1);
        topoNodes.add(portS2);

        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_R);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_S1_S);
        topoLinks.add(edgeInt_S2_S);

        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_R_Z);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_S_S1);
        topoLinks.add(edgeInt_S_S2);

        topoLinks.add(edgeMpls_P1_Q1);
        topoLinks.add(edgeMpls_P2_S1);
        topoLinks.add(edgeMpls_Q1_P1);
        topoLinks.add(edgeMpls_Q2_R1);
        topoLinks.add(edgeMpls_R1_Q2);
        topoLinks.add(edgeMpls_R2_S2);
        topoLinks.add(edgeMpls_S1_P2);
        topoLinks.add(edgeMpls_S2_R2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        for(TopoVertex oneVert : topoNodes)
        {
            if(oneVert.getVertexType().equals(VertexType.PORT))
            {
                floorMap.put(oneVert, floors);
                ceilingMap.put(oneVert, ceilings);
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, portBWs, floorMap, ceilingMap);
    }

    public void buildAsymmTopo13_2()
    {
        log.info("Building Asymmetric Test Topology 13.2");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> portBWs = new HashMap<>();
        Map<TopoVertex, List<Integer>> floorMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> ceilingMap = new HashMap<>();

        // Devices //
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);
        TopoVertex portS2 = new TopoVertex("nodeS:2", VertexType.PORT);

        // Asymmetric Bandwidth Capacity  (Ingress - Egress) //
        List<Integer> bwPortA = Arrays.asList(500, 500);
        List<Integer> bwPortZ = Arrays.asList(500, 500);
        List<Integer> bwPortP1 = Arrays.asList(100, 120);
        List<Integer> bwPortP2 = Arrays.asList(50, 1);
        List<Integer> bwPortQ1 = Arrays.asList(120, 100);
        List<Integer> bwPortQ2 = Arrays.asList(100, 120);
        List<Integer> bwPortR1 = Arrays.asList(120, 100);
        List<Integer> bwPortR2 = Arrays.asList(1, 50);
        List<Integer> bwPortS1 = Arrays.asList(1, 50);
        List<Integer> bwPortS2 = Arrays.asList(50, 1);
        portBWs.put(portA, bwPortA);
        portBWs.put(portZ, bwPortZ);
        portBWs.put(portP1, bwPortP1);
        portBWs.put(portP2, bwPortP2);
        portBWs.put(portQ1, bwPortQ1);
        portBWs.put(portQ2, bwPortQ2);
        portBWs.put(portR1, bwPortR1);
        portBWs.put(portR2, bwPortR2);
        portBWs.put(portS1, bwPortS1);
        portBWs.put(portS2, bwPortS2);

        // End-Port Links //
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_R = new TopoEdge(portZ, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_Z = new TopoEdge(nodeR, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S2_S = new TopoEdge(portS2, nodeS, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S2 = new TopoEdge(nodeS, portS2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_P1_Q1 = new TopoEdge(portP1, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P2_S1 = new TopoEdge(portP2, portS1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P1 = new TopoEdge(portQ1, portP1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R1 = new TopoEdge(portQ2, portR1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R1_Q2 = new TopoEdge(portR1, portQ2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R2_S2 = new TopoEdge(portR2, portS2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S1_P2 = new TopoEdge(portS1, portP2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S2_R2 = new TopoEdge(portS2, portR2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();
        List<Integer> floors = Arrays.asList(1);
        List<Integer> ceilings = Arrays.asList(5);

        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portS1);
        topoNodes.add(portS2);

        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_R);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_S1_S);
        topoLinks.add(edgeInt_S2_S);

        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_R_Z);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_S_S1);
        topoLinks.add(edgeInt_S_S2);

        topoLinks.add(edgeMpls_P1_Q1);
        topoLinks.add(edgeMpls_P2_S1);
        topoLinks.add(edgeMpls_Q1_P1);
        topoLinks.add(edgeMpls_Q2_R1);
        topoLinks.add(edgeMpls_R1_Q2);
        topoLinks.add(edgeMpls_R2_S2);
        topoLinks.add(edgeMpls_S1_P2);
        topoLinks.add(edgeMpls_S2_R2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        for(TopoVertex oneVert : topoNodes)
        {
            if(oneVert.getVertexType().equals(VertexType.PORT))
            {
                floorMap.put(oneVert, floors);
                ceilingMap.put(oneVert, ceilings);
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, portBWs, floorMap, ceilingMap);
    }

    public void buildMultiMplsTopo2()
    {
        log.info("Building Multi-MPLS Segment Test Topology 2");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> portBWs = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.ROUTER);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.ROUTER);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.ROUTER);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.SWITCH);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.SWITCH);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.SWITCH);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.ROUTER);
        TopoVertex nodeT = new TopoVertex("nodeT", VertexType.ROUTER);
        TopoVertex nodeU = new TopoVertex("nodeU", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portP3 = new TopoVertex("nodeP:3", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portQ3 = new TopoVertex("nodeQ:3", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);
        TopoVertex portS2 = new TopoVertex("nodeS:2", VertexType.PORT);
        TopoVertex portS3 = new TopoVertex("nodeS:3", VertexType.PORT);
        TopoVertex portT1 = new TopoVertex("nodeT:1", VertexType.PORT);
        TopoVertex portT2 = new TopoVertex("nodeT:2", VertexType.PORT);
        TopoVertex portU1 = new TopoVertex("nodeU:1", VertexType.PORT);
        TopoVertex portU2 = new TopoVertex("nodeU:2", VertexType.PORT);

        // Asymmetric Bandwidth Capacity  (Ingress - Egress) //
        List<Integer> bwPortA = Arrays.asList(500, 500);
        List<Integer> bwPortZ = Arrays.asList(500, 500);
        List<Integer> bwPortSame = Arrays.asList(100, 100);
        List<Integer> bwPortK1 = Arrays.asList(20, 100);
        List<Integer> bwPortL1 = Arrays.asList(100, 20);
        List<Integer> bwPortS2 = Arrays.asList(20, 100);
        List<Integer> bwPortT1 = Arrays.asList(100, 20);
        portBWs.put(portA, bwPortA);
        portBWs.put(portZ, bwPortZ);
        portBWs.put(portK1, bwPortK1);
        portBWs.put(portK2, bwPortSame);
        portBWs.put(portL1, bwPortL1);
        portBWs.put(portL2, bwPortSame);
        portBWs.put(portL3, bwPortSame);
        portBWs.put(portM1, bwPortSame);
        portBWs.put(portM2, bwPortSame);
        portBWs.put(portP1, bwPortSame);
        portBWs.put(portP2, bwPortSame);
        portBWs.put(portP3, bwPortSame);
        portBWs.put(portQ1, bwPortSame);
        portBWs.put(portQ2, bwPortSame);
        portBWs.put(portQ3, bwPortSame);
        portBWs.put(portR1, bwPortSame);
        portBWs.put(portR2, bwPortSame);
        portBWs.put(portS1, bwPortSame);
        portBWs.put(portS2, bwPortS2);
        portBWs.put(portS3, bwPortSame);
        portBWs.put(portT1, bwPortT1);
        portBWs.put(portT2, bwPortSame);
        portBWs.put(portU1, bwPortSame);
        portBWs.put(portU2, bwPortSame);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_T = new TopoEdge(portZ, nodeT, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_T_Z = new TopoEdge(nodeT, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S2_S = new TopoEdge(portS2, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S3_S = new TopoEdge(portS3, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_T1_T = new TopoEdge(portT1, nodeT, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_T2_T = new TopoEdge(portT2, nodeT, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_U1_U = new TopoEdge(portU1, nodeU, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_U2_U = new TopoEdge(portU2, nodeU, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S2 = new TopoEdge(nodeS, portS2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S3 = new TopoEdge(nodeS, portS3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_T_T1 = new TopoEdge(nodeT, portT1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_T_T2 = new TopoEdge(nodeT, portT2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_U_U1 = new TopoEdge(nodeU, portU1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_U_U2 = new TopoEdge(nodeU, portU2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_K2_M1 = new TopoEdge(portK2, portM1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_L2_M2 = new TopoEdge(portL2, portM2, 100L, Layer.MPLS);
        TopoEdge edgeEth_L3_P1 = new TopoEdge(portL3, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_M1_K2 = new TopoEdge(portM1, portK2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_M2_L2 = new TopoEdge(portM2, portL2, 100L, Layer.MPLS);
        TopoEdge edgeEth_P1_L3 = new TopoEdge(portP1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P2_Q1 = new TopoEdge(portP2, portQ1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P3_R1 = new TopoEdge(portP3, portR1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q1_P2 = new TopoEdge(portQ1, portP2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q2_R2 = new TopoEdge(portQ2, portR2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q3_S1 = new TopoEdge(portQ3, portS1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_R1_P3 = new TopoEdge(portR1, portP3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_R2_Q2 = new TopoEdge(portR2, portQ2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_S1_Q3 = new TopoEdge(portS1, portQ3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_S2_T1 = new TopoEdge(portS2, portT1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S3_U1 = new TopoEdge(portS3, portU1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_T1_S2 = new TopoEdge(portT1, portS2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_T2_U2 = new TopoEdge(portT2, portU2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_U1_S3 = new TopoEdge(portU1, portS3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_U2_T2 = new TopoEdge(portU2, portT2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);
        topoNodes.add(nodeT);
        topoNodes.add(nodeU);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portQ3);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portS1);
        topoNodes.add(portS2);
        topoNodes.add(portS3);
        topoNodes.add(portT1);
        topoNodes.add(portT2);
        topoNodes.add(portU1);
        topoNodes.add(portU2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_T);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_S1_S);
        topoLinks.add(edgeInt_S2_S);
        topoLinks.add(edgeInt_S3_S);
        topoLinks.add(edgeInt_T1_T);
        topoLinks.add(edgeInt_T2_T);
        topoLinks.add(edgeInt_U1_U);
        topoLinks.add(edgeInt_U2_U);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_T_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_S_S1);
        topoLinks.add(edgeInt_S_S2);
        topoLinks.add(edgeInt_S_S3);
        topoLinks.add(edgeInt_T_T1);
        topoLinks.add(edgeInt_T_T2);
        topoLinks.add(edgeInt_U_U1);
        topoLinks.add(edgeInt_U_U2);

        topoLinks.add(edgeMpls_K1_L1);
        topoLinks.add(edgeMpls_K2_M1);
        topoLinks.add(edgeMpls_L1_K1);
        topoLinks.add(edgeMpls_L2_M2);
        topoLinks.add(edgeEth_L3_P1);
        topoLinks.add(edgeMpls_M1_K2);
        topoLinks.add(edgeMpls_M2_L2);
        topoLinks.add(edgeEth_P1_L3);
        topoLinks.add(edgeEth_P2_Q1);
        topoLinks.add(edgeEth_P3_R1);
        topoLinks.add(edgeEth_Q1_P2);
        topoLinks.add(edgeEth_Q2_R2);
        topoLinks.add(edgeEth_Q3_S1);
        topoLinks.add(edgeEth_R1_P3);
        topoLinks.add(edgeEth_R2_Q2);
        topoLinks.add(edgeEth_S1_Q3);
        topoLinks.add(edgeMpls_S2_T1);
        topoLinks.add(edgeMpls_S3_U1);
        topoLinks.add(edgeMpls_T1_S2);
        topoLinks.add(edgeMpls_T2_U2);
        topoLinks.add(edgeMpls_U1_S3);
        topoLinks.add(edgeMpls_U2_T2);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, portBWs);
    }
}
