package net.es.oscars.topo.enums;


public enum CommandParamType {
    VC_ID(0),
    ALU_QOS_POLICY_ID(1),
    ALU_SDP_ID(2),
    ALU_SVC_ID(3),
    VPLS_LOOPBACK(4);

    private int value;

    CommandParamType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
