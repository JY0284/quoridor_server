package org.cic.yu.game.quoridor;

/**
 * Movement: Adding block on the board.
 */
public class BlockMovement extends QuoridorMovement {
    public static final BlockMovement EMPTY_MOVEMENT = new BlockMovement(-1, -1, -1, -1);

    private Point.AXIS axis;

    public BlockMovement(int x1, int y1, int x2, int y2) {
        Point p1 = new Point(x1, y1);
        Point p2 = new Point(x2, y2);

        this.setP1(p1);
        this.setP2(p2);
        this.axis = QuoridorMovement.getMovingAxis(p1, p2);
    }

    Point.AXIS getAxis() {
        return axis;
    }

    void isOutOfRange() throws QuoridorException {
        if (axis == Point.AXIS.Y) {
            if (isOutOfRange(p1.x) || isOutOfRange(p2.x) || isOutOfBoardRange(p1.y) || isOutOfBoardRange(p2.y)) {
                throw new QuoridorException("Block point is out of range.",
                        StateCode.ILLEGAL_MOVE);
            }
        }

        if (axis == Point.AXIS.X) {
            if (isOutOfRange(p1.y) || isOutOfRange(p2.y) || isOutOfBoardRange(p1.x) || isOutOfBoardRange(p2.x)) {
                throw new QuoridorException("Block point is out of range.",
                        StateCode.ILLEGAL_MOVE);
            }
        }
    }

    private boolean isOutOfRange(int val) {
        return val < 1 || val > 8;
    }

    private boolean isOutOfBoardRange(int val) {
        return val < 0 || val > 9;
    }

    @Override
    public String toString() {
        return "BlockMovement{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                '}';
    }
}
