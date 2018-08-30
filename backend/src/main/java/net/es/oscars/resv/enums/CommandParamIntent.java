package net.es.oscars.resv.enums;


public enum CommandParamIntent {
    PRIMARY(0),
    PROTECT(1);
    private int value;

    CommandParamIntent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
