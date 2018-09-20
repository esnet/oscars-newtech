package net.es.oscars.web.beans;

public enum ConnChange {
    COMMITTED ("COMMITTED"),
    UNCOMMITTED ("UNCOMMITTED"),
    DELETED ("DELETED"),
    ARCHIVED ("ARCHIVED"),
    RESERVED ("RESERVED");


    private String code;
    private ConnChange(String code) {
        this.code = code;
    }
    public String toString() {
        return code;
    }
}
