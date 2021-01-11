package org.cic.yu.game.quoridor;


import org.cic.yu.Application;
import org.cic.yu.game.Player;

public class QuoridorPlayer extends Player {
    private String key;
    private int actorId;
    private int blocksRemain;
    private Point loc;
    private String name;


    public QuoridorPlayer() {
       resetGameData();
    }

    void resetGameData() {
        this.actorId = -1;
        this.loc = null;
        this.blocksRemain = Application.PLAYER_BOARDS_TOTAL_NUMBER;
    }

    /**
     * The actor id indeed is the player order.
     * @param actorId player order, from 0.
     */
    void setActorId(int actorId) {
        this.actorId = actorId;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    int getActorId() {
        return actorId;
    }

    Point getLoc() {
        return loc;
    }

    void setLoc(Point loc) {
        this.loc = loc;
    }

    boolean consumeOneBlock() {
        if (blocksRemain == 0) {
            return false;
        }
        blocksRemain--;

        return true;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "QuoridorPlayer{" +
                "name='" + name + '\'' +
                '}';
    }

    void giveBackOneBlock() {
        blocksRemain++;
    }
}
