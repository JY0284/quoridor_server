package org.cic.yu.game.quoridor;

import org.cic.yu.game.GameException;

public class QuoridorException extends GameException {
    private StateCode code;
    private QuoridorMovement movement;
    private QuoridorPlayer player;

    public QuoridorException() {
    }

    public QuoridorException(String message) {
        super(message);
    }

    public QuoridorException(String msg, StateCode code) {
        this(msg, code, null);
    }

    public QuoridorException(String msg, StateCode code, QuoridorPlayer player) {
        this(msg, code, player, null);
    }

    public QuoridorException(String msg, StateCode code,
                             QuoridorPlayer player, QuoridorMovement movement) {
        super(msg);
        this.code = code;
        this.player = player;
        this.movement = movement;

    }

    public QuoridorException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuoridorException(Throwable cause) {
        super(cause);
    }

    public QuoridorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public StateCode getCode() {
        return code;
    }

    void setPlayer(QuoridorPlayer player) {
        this.player = player;
    }
}
