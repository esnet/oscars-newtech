package net.es.oscars.nsi.beans;

public enum NsiErrors {
    OK ("0"),
    MSG_ERROR ("00100"),
    MISSING_PARAM_ERROR ("00101"),
    RESV_ERROR ("00200"),
    TRANS_ERROR ("00201"),
    NO_SCH_ERROR ("00203"),
    SEC_ERROR ("00300"),
    PCE_ERROR ("00400"),
    LOOKUP_ERROR ("00406"),
    NRM_ERROR ("00500"),
    UNAVAIL_ERROR ("00600"),
    SVC_ERROR ("00700");


    private String code;
    private NsiErrors(String code) {
        this.code = code;
    }
    public String toString() {
        return code;
    }
}
