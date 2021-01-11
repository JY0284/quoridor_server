package org.cic.yu.game.quoridor;

public class QuoridorError extends Error {
    public QuoridorError(String message, Throwable cause) {
        super(message, cause);
    }

    public QuoridorError(String message) {
        super(message);
    }
}
