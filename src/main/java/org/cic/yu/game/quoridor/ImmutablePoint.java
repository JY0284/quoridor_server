package org.cic.yu.game.quoridor;

public class ImmutablePoint {
    private Point point;

    ImmutablePoint(int x, int y) {
        this.point = new Point(x, y);
    }

    public Point clonePoint() {
        return new Point(point);
    }

    int getX() {
        return point.x;
    }

    int getY() {
        return point.y;
    }
}
