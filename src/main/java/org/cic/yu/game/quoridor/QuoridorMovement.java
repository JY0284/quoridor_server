package org.cic.yu.game.quoridor;


import org.cic.yu.game.Movement;

import java.nio.ByteBuffer;

public class QuoridorMovement extends Movement {
    private static final String TAG = "Movement";

    public static final Point EMPTY_LOC = new Point(-1, -1);

    protected static String getTAG() {
        return TAG;
    }

    @Override
    public ByteBuffer putBytes(ByteBuffer buffer) {
        return putP2Bytes(putP1Bytes(buffer));
    }

    protected final ByteBuffer putP1Bytes(ByteBuffer buffer) {
        return buffer.put(getP1().toBytes());
    }

    protected final ByteBuffer putP2Bytes(ByteBuffer buffer) {
        return buffer.put(getP2().toBytes());
    }

    public enum MoveType {
        ADD_BLOCK,
        MOVE_ACTOR;
    }

    protected MoveType type;
    protected Point p1;
    protected Point p2;

    MoveType getType() {
        return type;
    }

    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }

    protected void setP1(Point p1) {
        this.p1 = p1;
    }

    protected void setP2(Point p2) {
        this.p2 = p2;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[16];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put(getP1().toBytes());
        buffer.put(getP2().toBytes());

        return bytes;
    }

    static Point.AXIS getMovingAxis(Point a, Point b) {
        if (a.x != b.x && a.y != b.y) {
            return Point.AXIS.BOTH;
        } else if(a.x == b.x) {
            return Point.AXIS.Y;
        } else {
            return Point.AXIS.X;
        }
    }
}
