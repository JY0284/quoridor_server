package org.cic.yu.game.quoridor;

import java.nio.ByteBuffer;

public class Point extends java.awt.Point {
    public enum AXIS {
        X,
        Y,
        BOTH
    }

    public Point() {
    }

    public Point(java.awt.Point p) {
        super(p);
    }

    public Point(int x, int y) {
        super(x, y);
    }

    boolean isLeftOf(Point p) {
        return getY() == p.getY() && getX() == p.getX() - 1;
    }

    boolean isRightOf(Point p) {
        return getY() == p.getY() && getX() == p.getX() + 1;
    }

    boolean isUpOf(Point p) {
        return getX() == p.getX() && getY() == p.getY() + 1;
    }

    boolean isDownOf(Point p) {
        return getX() == p.getX() && getY() == p.getY() - 1;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    Point moveTo(Point p) {
        this.x = p.x;
        this.y = p.y;

        return this;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[8];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.putInt(x).putInt(y);

        return bytes;
    }

}
