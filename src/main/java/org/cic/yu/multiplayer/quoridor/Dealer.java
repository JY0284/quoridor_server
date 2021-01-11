package org.cic.yu.multiplayer.quoridor;

import org.cic.yu.Application;
import org.cic.yu.game.quoridor.*;
import org.cic.yu.multiplayer.MultiPlayerException;
import org.cic.yu.multiplayer.quoridor.data.AuthResponse;
import org.cic.yu.multiplayer.quoridor.data.ChangeResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class Dealer {
    private int currentActor;
    private QuoridorGame game;
    private Map<Integer, Seat> seatMap;
    private Point[] actorLocs;
    private int stepCount = 0;
    private Map<Integer, QuoridorMovement> lastValidMoveMap;
    private Map<Integer, StateCode> lastMoveStateMap;
    private GameTable.GameResult result;
    private int[] timeoutCount;
    private final Object stepLock = new Object();

    private Dealer() {
        this.currentActor = 0;
        this.seatMap = new HashMap<>();
        this.actorLocs = new Point[2];
        this.lastValidMoveMap = new HashMap<>();
        this.lastValidMoveMap.put(this.currentActor, null);
        this.lastValidMoveMap.put(theOtherActor(), null);
        this.lastMoveStateMap = new HashMap<>();
        this.lastMoveStateMap.put(this.currentActor, null);
        this.lastMoveStateMap.put(theOtherActor(), null);
        this.timeoutCount = new int[2];
    }

    static Dealer build(QuoridorGame game) {
        Dealer dealer = new Dealer();
        dealer.setGame(game);

        return dealer;
    }

    private void setGame(QuoridorGame game) {
        this.game = game;
    }

    void serve(Seat seat, int playerOrder)  {
        seatMap.put(playerOrder, seat);
        game.addPlayer(seat.getPlayer(), playerOrder);
        this.actorLocs[playerOrder] = this.game.getPlayerLoc(playerOrder);

        assert seatMap.size() <= 2;
    }

    private int nextActor() {
        return theOtherActor();
    }

    private int theOtherActor() {
        return 1 - currentActor;
    }

    private Seat theOtherSeat() {
        return seatMap.get(theOtherActor());
    }

    public GameTable.GameResult getGameResult() {
        return this.result;
    }

    static class Change {

    }

    void run() throws MultiPlayerException {
        while (!game.isEnd()) {
            Seat activeSeat = seatMap.get(currentActor);

            // step start
            stepPrepare(activeSeat);

            if (stepCount == Application.ROUND_MAX_STEP) {
                dealWithMaxStep(activeSeat);

                break;
            }

            // send change
            ChangeResponse change;
            change = buildChangeResponse();

            notifyChange(activeSeat, change);

            // wait response
            QuoridorMovement currentActorMovement = null;
            boolean isTimeout = false;
            if (activeSeat.isReady()) {
                final QuoridorMovement[] receivedMovement = new QuoridorMovement[1];
                Thread thread = buildReceiverThread(activeSeat, receivedMovement);
                thread.start();
                try {
                    synchronized (stepLock) {
                        stepLock.wait(Application.TIMEOUT_MILLISECONDS);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    synchronized (receivedMovement) {
                        if (receivedMovement[0] != null) {
                            currentActorMovement = receivedMovement[0];
                        } else {
                            isTimeout = true;
                            if (Application.DEBUG) {
                                Logger.getAnonymousLogger().info(String.format("%s timed out.",
                                        activeSeat.getPlayer().getName()));
                            }
                        }
                    }
                    thread.interrupt();
                }
            }

            // do change
            doMove(activeSeat, currentActorMovement, isTimeout);

            if (Application.DEBUG) {
                Logger.getAnonymousLogger().info(String.format("Dealer finished step[%d].", stepCount + 1));
            }

            stepCount++;

            currentActor = nextActor();
        }

        if (Application.DEBUG) {Logger.getAnonymousLogger().info("Dealer is leaving after finishing this game.");}
    }

    private Thread buildReceiverThread(Seat activeSeat, QuoridorMovement[] receivedMovement) {
        return new Thread(() -> {
            try {
                QuoridorMovement movement = activeSeat.receiveMovement();
                    if(!Thread.interrupted()) {
                        synchronized (receivedMovement) {
                            receivedMovement[0] = movement;
                            synchronized (stepLock) {
                                stepLock.notify();
                            }
                        }
                    }
            } catch (IOException e) {
                if (!Thread.interrupted()) {
                    Logger.getAnonymousLogger().log(Level.SEVERE,
                            String.format("While trying to receive the step request, " +
                                            "%s has closed his/her connection.",
                                    activeSeat.getPlayer().getName()));
                }
                if (Application.DEBUG) {
                    e.printStackTrace();
                }
                activeSeat.setReady(false);
            }
        });
    }

    private void stepPrepare(Seat activeSeat) {
        if (Application.DEBUG) {
            Logger.getAnonymousLogger().
                    info(String.format("Step[%d]: by %s", stepCount + 1, activeSeat));
        }
    }

    private void notifyChange(Seat activeSeat, ChangeResponse change) {
        try {
            if (activeSeat.isReady()) {
                if (Application.DEBUG) {
                    Logger.getAnonymousLogger().info("Dealer notifying:" + change.toString());
                }
                activeSeat.notifyChange(change);
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE,
                    String.format("%s has closed his/her connection.",
                            activeSeat.getPlayer().getName()));
            activeSeat.setReady(false);

            if (Application.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private ChangeResponse buildChangeResponse() {
        if (Application.DEBUG) {
            Logger.getAnonymousLogger().
                    info("Dealer - notify change to " + seatMap.get(currentActor));
        }

        ChangeResponse change;
        if (stepCount == 0) {
             change = ChangeResponse.newChangeResponse()
                    .actorLoc(actorLocs[currentActor])
                    .theOtherActorNewLoc(actorLocs[theOtherActor()])
                    .theOtherPlayerNewBlockLoc(BlockMovement.EMPTY_MOVEMENT)
                    .state(AuthResponse.State.OK)
                    .stateCode(StateCode.OK)
                    .build();

        } else {
            StateCode stateCode = StateCode.OK;

            // fetch my last movement state
            if (this.lastMoveStateMap.get(currentActor) != null) {
                stateCode = this.lastMoveStateMap.get(currentActor);
            }

            QuoridorMovement lastValidMove = lastValidMoveMap.get(theOtherActor());
            if (lastValidMove != null) {
                change = ChangeResponse.buildFromOpponentMove(lastValidMove,
                        actorLocs[theOtherActor()],
                        stateCode);
                change.setActorLoc(actorLocs[this.currentActor]);
            } else {
                // the opponent's never take any effective movement
                change = ChangeResponse.newChangeResponse()
                        .actorLoc(actorLocs[this.currentActor])
                        .theOtherActorNewLoc(actorLocs[theOtherActor()])
                        .theOtherPlayerNewBlockLoc(BlockMovement.EMPTY_MOVEMENT)
                        .state(AuthResponse.State.OK)
                        .stateCode(stateCode)
                        .build();
            }
        }
        return change;
    }

    private void dealWithMaxStep(Seat activeSeat) {
        if (Application.DEBUG) {
            Logger.getAnonymousLogger()
                    .info("[GAME] Reached max step count. Offensive player win.");
        }

        this.game.setEnd(true);

        activeSeat.notifyFail(StateCode.LOST);
        theOtherSeat().notifySuccess();

        this.result = new GameTable.GameResult(
                theOtherSeat().getPlayer(),
                activeSeat.getPlayer(),
                stepCount);
    }

    private void doMove(Seat activeSeat, QuoridorMovement currentActorMove, boolean isTimeout) throws MultiPlayerException {
        if (Application.DEBUG) {
            Logger.getAnonymousLogger().info("Dealer: " + activeSeat + "-->" + currentActorMove);
        }

        try {
            StateCode stateCode;
            if (currentActorMove != null) {
                try {
                    stateCode = this.game.asPlayer(activeSeat.getPlayer()).move(currentActorMove);
                } catch (Exception e) {
                    if (e instanceof QuoridorException) {
                        throw e;
                    }
                    if (Application.DEBUG) {
                        e.printStackTrace();
                    }
                    stateCode = StateCode.ILLEGAL_MOVE;
                }
            } else {
                stateCode = StateCode.ENEMY_LEFT;
            }

            if (isTimeout) {
                stateCode = StateCode.TIME_OUT;
            }

            this.lastMoveStateMap.put(currentActor, stateCode);

            dealWithStepResult(activeSeat, currentActorMove, stateCode);
        } catch (QuoridorException e) {
            e.printStackTrace();
        }
    }

    private void dealWithStepResult(Seat activeSeat, QuoridorMovement currentActorMove, StateCode stateCode) throws MultiPlayerException {
        switch (stateCode) {
            case WIN:
                activeSeat.notifySuccess();
                theOtherSeat().notifyFail(StateCode.LOST);
                this.result = new GameTable.GameResult(
                        activeSeat.getPlayer(),
                        theOtherSeat().getPlayer(),
                        stepCount);

                break;

            case ENEMY_LEFT:
                // indeed, this is MYSELF_LEFT
                game.setEnd(true);
                activeSeat.setReady(false);
                theOtherSeat().notifySuccess();
                this.result = new GameTable.GameResult(
                        theOtherSeat().getPlayer(),
                        activeSeat.getPlayer(),
                        stepCount);

                break;

            case RULES_BREAKER:
            case LOST:
                activeSeat.notifyFail(stateCode);
                theOtherSeat().notifySuccess();
                this.result = new GameTable.GameResult(
                        theOtherSeat().getPlayer(),
                        activeSeat.getPlayer(),
                        stepCount);

                break;

            case OK:
                this.lastValidMoveMap.put(this.currentActor, currentActorMove);

                break;

            case TIME_OUT:
                this.timeoutCount[currentActor] += 1;
                if (this.timeoutCount[currentActor] == Application.MAX_TIMEOUT_COUNT) {
                    game.setEnd(true);

                    System.out.println(String.format("[INFO] %s has got %d timeouts, " +
                                    "he/she will now directly leave this contest.",
                            activeSeat.getPlayer().getName(),
                            Application.MAX_TIMEOUT_COUNT));

                    activeSeat.notifyCloseConnection();
                    activeSeat.setReady(false);
                    theOtherSeat().notifySuccess();
                    this.result = new GameTable.GameResult(
                            theOtherSeat().getPlayer(),
                            activeSeat.getPlayer(),
                            stepCount);

                    break;
                }
            case ILLEGAL_MOVE:
            case INSUFFICIENT_BLOCK:
//                        activeSeat.notifyRetry();
//                        lastMove = activeSeat.receiveMovement();
//                        doMove(activeSeat);
                // this move will not take effect
                this.lastValidMoveMap.put(this.currentActor, null);

                break;

            default:
                throw new MultiPlayerException("Illegal game state code.");
        }
    }
}
