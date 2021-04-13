package net.es.oscars.mig.enums;


public enum MigrationState {
    WAITING("WAITING"),
    COMPLETE("COMPLETE"),
    IN_PROGRESS("IN_PROGRESS");
    final private String value;

    MigrationState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
