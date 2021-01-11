package org.cic.yu.game;

public abstract class Game<T extends Player, V extends Movement> {
    public abstract void addPlayer(T player) throws GameException;
    public abstract void start();
    public abstract void end();
    public abstract boolean isEnd();
    protected abstract void onStep(V movement, T player);
}
