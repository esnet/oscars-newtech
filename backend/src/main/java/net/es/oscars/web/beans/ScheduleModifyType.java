package net.es.oscars.web.beans;

public enum ScheduleModifyType {
    BEGIN ("BEGIN"),
    END ("END");


    private String code;
    private ScheduleModifyType(String code) {
        this.code = code;
    }
    public String toString() {
        return code;
    }
}
