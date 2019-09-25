package net.es.oscars.app.exc;

public class NotReadyException extends Exception {
    public NotReadyException() {
        super();
    }
    public NotReadyException(String msg) {
        super(msg);
    }
}
