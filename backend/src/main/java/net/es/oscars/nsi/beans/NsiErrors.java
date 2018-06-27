package net.es.oscars.nsi.beans;

public enum NsiErrors {
    NRM_ERROR ("00501");

    private String code;
    private NsiErrors(String code) {
        this.code = code;
    }
    public String toString() {
        return code;
    }
}
