package org.cic.yu.game.quoridor;

/*
     棋子坐标如下（更改y轴方向，使得己方是数值较小的坐标）:

    x 1 2 3 4 5 6 7 8 9 \
     ┌-┬-┬-┬-┬-┬-┬-┬-┬-┐ \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖9 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖8 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖7 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖6 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖5 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖4 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖3 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖2 \
     └-┴-┴-┴-┴-┴-┴-┴-┴-┘↖1 y

     挡板坐标结构如下：
     边框不可放置->横向时，y∈[1,8]；纵向时，x∈[1,8]。

    x0 1 2 3 4 5 6 7 8 9 \
     ┌-┬-┬-┬-┬-┬-┬-┬-┬-┐9 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤8 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤7 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤6 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤5 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤4 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤3 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤2 \
     ├-┼-┼-┼-┼-┼-┼-┼-┼-┤1 \
     └-┴-┴-┴-┴-┴-┴-┴-┴-┘0 y
*/


import org.cic.yu.Application;

import java.util.logging.Logger;

public class Board {

    private final static int WIDTH = 9;

    //todo: The board only support 2 actors.
    private final static int ACTORS_COUNT = 2;

    /**
     * 坐标均为从较小值递增存储。
     * x坐标从左侧递增，如Actor(1,1)上方的Block坐标为(0,1)->(1,1)，
     * 实际在blocks数组中存储在左侧坐标(0,1)处，代表此处的Block实体。
     * y坐标从下侧递增，同理。
     */
    private boolean[][] blocksXAxis;
    private boolean[][] blocksYAxis;
    private Point[] actors;
    private int actorIdxCount;

    public final static ImmutablePoint ACTOR1_INITIAL_LOC = new ImmutablePoint(5, 1);
    public final static ImmutablePoint ACTOR2_INITIAL_LOC = new ImmutablePoint(5, 9);

    Board() {
        blocksXAxis = new boolean[WIDTH + 1][WIDTH + 1];
        blocksYAxis = new boolean[WIDTH + 1][WIDTH + 1];
        actors = new Point[ACTORS_COUNT];
        actors[0] = ACTOR1_INITIAL_LOC.clonePoint();
        actors[1] = ACTOR2_INITIAL_LOC.clonePoint();
    }

    /**
     * Notice that this method will set default game order for player.
     * @param player
     * @throws QuoridorException
     */
    void preparePlayer(QuoridorPlayer player)  {
        actorIdxCount += 1;

        if (actorIdxCount > ACTORS_COUNT) {
            throw new QuoridorError("The board could only support a game of 2 players.");
        }

        player.resetGameData();
    }

    void setPlayerOrder(QuoridorPlayer player, int order) {
        player.setActorId(order);
        player.setLoc(actors[order]);
    }

    private Point getBlockedPoint(QuoridorPlayer player, ActorMovement movement) {
        Point preLoc = player.getLoc();
        Point newLoc = movement.getLoc();
        Point.AXIS movingAxis = QuoridorMovement.getMovingAxis(preLoc, newLoc);

        assert movingAxis != Point.AXIS.BOTH;

        return getBlockedPoint(preLoc, newLoc, movingAxis);
    }

    private Point getBlockedPoint(Point preLoc, Point newLoc, Point.AXIS movingAxis) {
        if (movingAxis == Point.AXIS.X) {
            int delta = newLoc.x - preLoc.x;
            if (delta > 0) {
                for (int d = 1; d <= delta; d++) {
                    int involvedBlockX = preLoc.x + d - 1;
                    if (blocksYAxis[involvedBlockX][newLoc.y - 1]) {
                        return new Point(involvedBlockX, newLoc.y - 1);
                    }
                }
            } else {
                // delta < 0
                for (int d = delta; d <= -1; d++) {
                    int involvedBlockPos = preLoc.x + d;
                    if (blocksYAxis[involvedBlockPos][newLoc.y - 1]) {
                        return new Point(involvedBlockPos, newLoc.y - 1);
                    }
                }
            }
        } else {
            // axis = Y
            int delta = newLoc.y - preLoc.y;
            if (delta > 0) {
                for (int d = 1; d <= delta; d++) {
                    int involvedBlockY = preLoc.y + d - 1;
                    if (blocksXAxis[newLoc.x - 1][involvedBlockY]) {
                        return new Point(newLoc.x - 1, involvedBlockY);
                    }
                }
            } else {
                // delta < 0
                for (int d = delta; d <= -1; d++) {
                    int involvedBlockY = preLoc.y + d;
                    if (blocksXAxis[newLoc.x - 1][involvedBlockY]) {
                        return new Point(newLoc.x - 1, involvedBlockY);
                    }
                }
            }
        }

        return null;
    }

    StateCode moveActor(QuoridorPlayer player, ActorMovement movement) throws QuoridorException {
        Point theOtherActorPoint = actors[theOtherPlayerActorID(player)];

        movement.onMove(player, theOtherActorPoint);

        Point blockedPoint = getBlockedPoint(player, movement);
        if (blockedPoint != null) {
            throw new QuoridorException("The way is blocked by block at " + blockedPoint,
                    StateCode.ILLEGAL_MOVE,
                    player,
                    movement);
        }

        player.getLoc().moveTo(movement.getLoc());

        StateCode stateCode = StateCode.OK;

        if ((player.getActorId() == 0 && player.getLoc().y == ACTOR2_INITIAL_LOC.getY())
            || (player.getActorId() == 1 && player.getLoc().y == ACTOR1_INITIAL_LOC.getY())) {
            stateCode = StateCode.WIN;
        }

        return stateCode;
    }

    private int theOtherPlayerActorID(QuoridorPlayer player) {
        return 1 - player.getActorId();
    }

    StateCode addBlock(QuoridorPlayer player, BlockMovement movement) throws QuoridorException {
        if (Application.DEBUG) {
            Logger.getLogger("Board").info("[ADD BLOCK] Trying adding at " +
                    movement.getP1() + " " + movement.getP2());
        }

        if (player.consumeOneBlock()) {
            Point p1 = movement.getP1();
            Point p2 = movement.getP2();
            Point.AXIS axis = movement.getAxis();

            // check point range
            try {
                movement.isOutOfRange();
            } catch (Exception e) {
                player.giveBackOneBlock();
                throw e;
            }

            if (axis == Point.AXIS.BOTH) {
                player.giveBackOneBlock();
                throw new QuoridorException("Blocks can only be added within a row or a line in " +
                        "one Block-Adding operation.", StateCode.ILLEGAL_MOVE, player, movement);
            }

            if (p1.x > p2.x || p1.y > p2.y) {
                player.giveBackOneBlock();
                throw new QuoridorException("Block points must be sorted by ascending order.",
                        StateCode.ILLEGAL_MOVE, player, movement);
            }

            if ((axis == Point.AXIS.X && p2.x - p1.x != 2) ||
                    (axis == Point.AXIS.Y && p2.y - p1.y != 2)) {
                player.giveBackOneBlock();
                throw new QuoridorException("The length of block is exactly 2, neither 1 nor 0.",
                        StateCode.ILLEGAL_MOVE, player, movement);
            }

            // check if occupied
            // 下方减一操作的取舍取决于block move的入参是开区间点还是闭区间点。
            // Point blockBeginPoint = new Point(p1);
            Point blockEndPoint =  new Point(p2);
            if (axis == Point.AXIS.X) {
                blockEndPoint.move(p2.x - 1, p2.y);
            } else {
                blockEndPoint.move(p2.x, p2.y - 1);
            }

            if (isBlocked(p1, axis) || isBlocked(blockEndPoint, axis)) {
                player.giveBackOneBlock();
                throw new QuoridorException("Already a block.", StateCode.ILLEGAL_MOVE,
                        player, movement);
            } else {
                setBlocked(p1, axis);
                setBlocked(blockEndPoint, axis);

                if (Application.DEBUG) {
                    Logger.getLogger("Board").info(player + "---[MOVE]Add block at " + p1);
                    Logger.getLogger("Board").info(player + "---[MOVE]Add block at " + blockEndPoint);
                }
            }

            // check if there will be no way to win
            if (!leaveWayForTheOtherPlayerToWin(player)) {
                removeBlock(p1, axis);
                removeBlock(blockEndPoint, axis);

                player.giveBackOneBlock();
                throw new QuoridorException("You are going to remove the victory chance of your enemy.",
                        StateCode.ILLEGAL_MOVE);
            }
        } else {
            throw new QuoridorException("Insufficient blocks.",
                    StateCode.INSUFFICIENT_BLOCK, player, movement);
        }

        return StateCode.OK;
    }

    private boolean leaveWayForTheOtherPlayerToWin(QuoridorPlayer player) {
        QuoridorPlayer testPlayer = new QuoridorPlayer();
        testPlayer.setName("INTERNAL_TEST_PLAYER");

        // Remember to use new Point()
        testPlayer.setLoc(new Point(actors[theOtherPlayerActorID(player)]));
        testPlayer.setActorId(theOtherPlayerActorID(player));

        return hasChanceToWin(testPlayer);
    }

    private boolean dfs(QuoridorPlayer player, int targetY, boolean[][] visited) {
        // dfs to edge.

        Point playerLoc = player.getLoc();
        int x = playerLoc.x;
        int y = playerLoc.y;

        if (visited[x - 1][y - 1]) {
            return false;
        }

        if (playerLoc.y == targetY) {
//            System.out.println(ccc++);
            return true;
        } else {
            visited[x - 1][y - 1] = true;

            // up
            ActorMovement upMovement = new ActorMovement(playerLoc.x, playerLoc.y + 1);
            try {
                this.moveActor(player, upMovement);
                if (dfs(player, targetY, visited))
                    return true;

                // backtrace
                player.getLoc().y = y;
            } catch (QuoridorException ignored) {

            }

            // down
            ActorMovement downMovement = new ActorMovement(playerLoc.x, playerLoc.y - 1);
            try {
                this.moveActor(player, downMovement);
                if (dfs(player, targetY, visited))
                    return true;

                // backtrace
                player.getLoc().y = y;
            } catch (QuoridorException ignored) {

            }


            // left
            ActorMovement leftMovement = new ActorMovement(playerLoc.x - 1, playerLoc.y);
            try {
                this.moveActor(player, leftMovement);
                if (dfs(player, targetY, visited))
                    return true;

                // backtrace
                player.getLoc().x = x;
            } catch (QuoridorException ignored) {

            }

            // right
            ActorMovement rightMovement = new ActorMovement(playerLoc.x + 1, playerLoc.y);
            try {
                this.moveActor(player, rightMovement);
                if (dfs(player, targetY, visited))
                    return true;

                // backtrace
                player.getLoc().x = x;
            } catch (QuoridorException ignored) {

            }

            // up
            ActorMovement upJumpMovement = new ActorMovement(playerLoc.x, playerLoc.y + 2);
            try {
                this.moveActor(player, upJumpMovement);
                if (dfs(player, targetY, visited))
                    return true;

                // backtrace
                player.getLoc().y = y;
            } catch (QuoridorException ignored) {

            }

            // down
            ActorMovement downJumpMovement = new ActorMovement(playerLoc.x, playerLoc.y - 2);
            try {
                this.moveActor(player, downJumpMovement);
                if (dfs(player, targetY, visited))
                    return true;

                // backtrace
                player.getLoc().y = y;
            } catch (QuoridorException ignored) {

            }


            // left
            ActorMovement leftJumpMovement = new ActorMovement(playerLoc.x - 2, playerLoc.y);
            try {
                this.moveActor(player, leftJumpMovement);
                if (dfs(player, targetY, visited))
                    return true;

                // backtrace
                player.getLoc().x = x;
            } catch (QuoridorException ignored) {

            }

            // right
            ActorMovement rightJumpMovement = new ActorMovement(playerLoc.x + 2, playerLoc.y);
            try {
                this.moveActor(player, rightJumpMovement);
                if (dfs(player, targetY, visited))
                    return true;

                // backtrace
                player.getLoc().x = x;
            } catch (QuoridorException ignored) {

            }

//            visited[playerLoc.x - 1][playerLoc.y - 1] = false;
        }

        return false;
    }

    private boolean hasChanceToWin(QuoridorPlayer player) {
        int targetY = -1;
        switch (player.getActorId()) {
            case 0:
                targetY = ACTOR2_INITIAL_LOC.getY();
                break;
            case 1:
                targetY = ACTOR1_INITIAL_LOC.getY();
                break;
        }

        final Point playerLoc = new Point(player.getLoc());
        boolean[][] visited = new boolean[WIDTH][WIDTH];

        boolean ret =  dfs(player, targetY, visited);
        player.setLoc(playerLoc);

        return ret;
    }

    private void setBlocked(Point p, Point.AXIS axis) {
        assert axis != Point.AXIS.BOTH;

        if (axis == Point.AXIS.X) {
            blocksXAxis[p.x][p.y] = true;
        } else {
            blocksYAxis[p.x][p.y] = true;
        }
    }

    private void removeBlock(Point p, Point.AXIS axis) {
        assert axis != Point.AXIS.BOTH;

        if (axis == Point.AXIS.X) {
            blocksXAxis[p.x][p.y] = false;
        } else {
            blocksYAxis[p.x][p.y] = false;
        }
    }

    private boolean isBlocked(Point p, Point.AXIS axis) {
        assert axis != Point.AXIS.BOTH;

        if (axis == Point.AXIS.X) {
            return blocksXAxis[p.x][p.y];
        } else {
            return blocksYAxis[p.x][p.y];
        }
    }

    public Point getActorPoint(int actorOrder) {
        return actors[actorOrder];
    }

    public static void main(String[] args) {
        QuoridorGame game = new QuoridorGame();
        QuoridorPlayer p1 = new QuoridorPlayer();
        QuoridorPlayer p2 = new QuoridorPlayer();
        game.addPlayer(p1);
        game.addPlayer(p2);
        p2.setName("bot2");
        p1.setName("INTERNAL_TEST_PLAYER");
        try {
            game.asPlayer(p2).move(new BlockMovement(0, 1, 2, 1));
            game.asPlayer(p2).move(new BlockMovement(2, 1, 4, 1));
            game.asPlayer(p2).move(new BlockMovement(4, 1, 6, 1));
            game.asPlayer(p2).move(new BlockMovement(6, 1, 8, 1));
            game.asPlayer(p2).move(new BlockMovement(8, 1, 9, 1));
        } catch (QuoridorException e) {
            e.printStackTrace();
        }
        game.getStepLog().toCsv("test_block_win_dfs.csv");
        System.out.println(game.getBoard().hasChanceToWin(p1));

        game.getBoard().setBlocked(new Point(8, 1), Point.AXIS.X);
        System.out.println(game.getBoard().hasChanceToWin(p1));
    }
}
