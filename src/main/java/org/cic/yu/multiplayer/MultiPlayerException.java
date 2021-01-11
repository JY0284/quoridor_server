package org.cic.yu.multiplayer;

public class MultiPlayerException extends Exception {
    public MultiPlayerException() {
    }

    public MultiPlayerException(String message) {
        super(message);
    }

    public MultiPlayerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultiPlayerException(Throwable cause) {
        super(cause);
    }
}
