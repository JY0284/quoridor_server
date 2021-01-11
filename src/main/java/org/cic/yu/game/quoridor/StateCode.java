package org.cic.yu.game.quoridor;

import org.cic.yu.Application;

public enum StateCode {
    EMPTY(56, "Placeholder state."),
    OK(0, "Well done."),
    WIN(1, "Victory!"),
    LOST(2, "Defeat!"),
    TIME_OUT(3, "Time out!"),
    ENEMY_LEFT(4, "The other player has left the game."),
    RULES_BREAKER(5, String.format("Rules breaker! You have broken the rules for %d times."
            , Application.RULES_BREAKER_COUNT)),
    INSUFFICIENT_BLOCK(6, "You don't have enough blocks."),
    ILLEGAL_MOVE(7, "Illegal move!");

    private int value;
    private String info;

    StateCode(int value, String info) {
        this.value = value;
        this.info = info;
    }

    public int getValue() {
        return value;
    }

    public boolean isEndState() {
        switch (this) {
            case WIN:
            case LOST:
            case ENEMY_LEFT:
            case RULES_BREAKER:
            case TIME_OUT:
                return true;
            default:
                return false;
        }
    }

    public boolean isCausedByIllegalMove() {
        return this == ILLEGAL_MOVE || this == INSUFFICIENT_BLOCK;
    }

    @Override
    public String toString() {
        return "[" + value + "]" + " " + info;
    }
}
