package org.cic.yu.game.quoridor;


import org.cic.yu.Application;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActorMovement extends QuoridorMovement {
    public ActorMovement(int x, int y) {
        setP1(new Point(x, y));
    }

    private boolean isOutOfRange() {
        return !(getNewX() <= 9 && getNewX() >= 1 && getNewY() <= 9 && getNewY() >= 1);
    }

    void onMove(QuoridorPlayer player, Point otherActorLoc) throws QuoridorException {
        Point preLoc = player.getLoc();

        // TODO: 2020/4/22  Test player debug output
        if (Application.DEBUG && !player.getName().equals("INTERNAL_TEST_PLAYER")) {
            Logger.getLogger(getTAG()).log(Level.INFO,
                    player + "---" + "[MOVE] " + preLoc + " -> " + getLoc());
        }

        if(QuoridorMovement.getMovingAxis(preLoc, getLoc()) == Point.AXIS.BOTH) {
            throw new QuoridorException("You can only go left/right/up/down.",
                    StateCode.ILLEGAL_MOVE);
        }

        if (isOutOfRange()) {
            throw new QuoridorException("Out of range.", StateCode.ILLEGAL_MOVE);
        }

        if(getLoc().equals(preLoc)) {
            throw new QuoridorException("Nothing to do at all.", StateCode.ILLEGAL_MOVE);
        }

        if (Math.abs(getNewY() - preLoc.getY()) > 2 ||
                Math.abs(getNewX() - preLoc.getX()) > 2) {
            throw new QuoridorException("Your actor is flying too fast.", StateCode.ILLEGAL_MOVE);
        }

        // jump case
        // x1 - x2 = {1,2} / y1 - y2 = {1,2}
        if (Math.abs(getNewY() - preLoc.getY()) > 1) {
            // case: dy = 2
            // up or down
            if(preLoc.isUpOf(otherActorLoc) && getNewY() - preLoc.getY() == -2) {
                if(Application.DEBUG && !player.getName().equals("INTERNAL_TEST_PLAYER")) {
                    Logger.getLogger(getTAG()).
                            log(Level.INFO, buildJumpInfo("down", otherActorLoc));
                }
            } else if(preLoc.isDownOf(otherActorLoc) && getNewY() - preLoc.getY() == 2) {
                if(Application.DEBUG && !player.getName().equals("INTERNAL_TEST_PLAYER")) {
                    Logger.getLogger(getTAG()).
                            log(Level.INFO, buildJumpInfo("up", otherActorLoc));
                }
            } else {
                throw new QuoridorException("You may take a wrong jump move.",
                        StateCode.ILLEGAL_MOVE);
            }
        } else if (Math.abs(getNewX() - preLoc.getX()) > 1) {
            // case: dx = 2
            // left or right
            if (preLoc.isLeftOf(otherActorLoc) && getNewX() - preLoc.getX() == 2) {
                if(Application.DEBUG && !player.getName().equals("INTERNAL_TEST_PLAYER")) {
                    Logger.getLogger(getTAG()).
                            log(Level.INFO, buildJumpInfo("right", otherActorLoc));
                }
            } else if (preLoc.isRightOf(otherActorLoc) && getNewX() - preLoc.getX() == -2) {
                if(Application.DEBUG && !player.getName().equals("INTERNAL_TEST_PLAYER")) {
                    Logger.getLogger(getTAG()).
                            log(Level.INFO, buildJumpInfo("left", otherActorLoc));
                }
            } else {
                throw new QuoridorException("You may take a wrong jump move.",
                        StateCode.ILLEGAL_MOVE);
            }
        } else {
            // case dx = 1 or dy = 1 and not occupied
        }

        if(getP1().equals(otherActorLoc)) {
            throw new QuoridorException("Already an actor there.", StateCode.ILLEGAL_MOVE);
        }

        // without involving blocks, this movement is right.
    }

    private String buildJumpInfo(String direction, Point otherPoint) {
        return "Jump to " + direction + " of " + otherPoint + " -> " + getP1();
    }

    int getNewX() {
        return getP1().x;
    }

    int getNewY() {
        return getP1().y;
    }

    public Point getLoc() {
        return getP1();
    }

    @Override
    public ByteBuffer putBytes(ByteBuffer buffer) {
        return putP1Bytes(buffer);
    }

    @Override
    public String toString() {
        return "ActorMovement{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                '}';
    }
}
