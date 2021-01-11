package org.cic.yu.game.quoridor;


import org.cic.yu.Application;
import org.cic.yu.game.Game;
import org.cic.yu.multiplayer.quoridor.data.ClientRoundRecord;
import org.cic.yu.multiplayer.quoridor.data.ClientStep;

import java.util.*;
import java.util.logging.Logger;

public class QuoridorGame extends Game<QuoridorPlayer, QuoridorMovement> {

    public class QuoridorGameWrapper {
        private final QuoridorPlayer player;
        private int rulesBreakCount;

        private QuoridorGameWrapper(QuoridorPlayer player) {
            this.rulesBreakCount = 0;
            this.player = player;
        }

        public StateCode move(QuoridorMovement movement)  {
            StateCode stateCode;
            try {
                if (movement instanceof ActorMovement) {
                    stateCode = move((ActorMovement) movement);
                } else if (movement instanceof BlockMovement) {
                    stateCode = move((BlockMovement) movement);
                } else {
                    throw new QuoridorException("No such movement." + movement);
                }
            } catch (QuoridorException e) {
                if (Application.DEBUG) {
//                    Logger.getAnonymousLogger().info(movement.toString());
                    e.printStackTrace();
                }

                stateCode = e.getCode();
            }

            if (stateCode.isCausedByIllegalMove()) {
                rulesBreakCount += 1;
                if (rulesBreakCount == RULES_BREAKER_COUNT) {
                    stateCode = StateCode.RULES_BREAKER;

                    // RULES_BREAKER state code turns the game to the end.
                    QuoridorGame.this.isEnd = true;
                }
            }

            onStep(movement, player, stateCode);

            if (Application.DEBUG) {Logger.getAnonymousLogger().info("Game: move state code -" + stateCode);}

            return stateCode;
        }

        public StateCode move(int x, int y) {
            return move((QuoridorMovement)new ActorMovement(x, y));
        }

        public StateCode move(int x1, int y1, int x2, int y2) {
            return move((QuoridorMovement)new BlockMovement(x1, y1, x2, y2));
        }

        private StateCode move(ActorMovement movement) throws QuoridorException {
            StateCode stateCode = QuoridorGame.this.move(player, movement);
            if(stateCode.isEndState()) {
                isEnd = true;
            }
            return stateCode;
        }

        private StateCode move(BlockMovement movement) throws QuoridorException {
            StateCode stateCode = QuoridorGame.this.move(player, movement);
            if(stateCode.isEndState()) {
                isEnd = true;
            }
            return stateCode;
        }
    }

    private static final int RULES_BREAKER_COUNT = Application.RULES_BREAKER_COUNT;
    private static final int PLAYERS_NUMBER = Application.PLAYERS_IN_A_GAME;

    private boolean isEnd = false;
    private Date startTime;
    private Date endTime;
    private Board board;
    private List<QuoridorPlayer> players;
//    private List<Round> rounds;
//    private List<Step> steps;
    private QuoridorPlayer currentPlayer;
    private Map<QuoridorPlayer, QuoridorGameWrapper> gameWrappers;
    private ClientRoundRecord record;

    public QuoridorGame() {
        this.players = new ArrayList<>(PLAYERS_NUMBER);
//        this.rounds = new LinkedList<>();
        this.board = new Board();
        this.gameWrappers = new HashMap<>();
//        this.steps = new LinkedList<>();
        this.record = new ClientRoundRecord();
    }

    public void addPlayer(QuoridorPlayer newPlayer, int order)  {
        addPlayer(newPlayer);
        setPlayerOrder(newPlayer, order);
    }


    public void addPlayer(QuoridorPlayer newPlayer) {
        board.preparePlayer(newPlayer);
        board.setPlayerOrder(newPlayer, players.size());

        players.add(newPlayer);
        gameWrappers.put(newPlayer, new QuoridorGameWrapper(newPlayer));

    }

    private void setPlayerOrder(QuoridorPlayer player, int order) {
        this.board.setPlayerOrder(player, order);
    }


    @Override
    public void start() {
        this.startTime = Calendar.getInstance().getTime();
    }

    @Override
    public void end() {
        this.endTime = Calendar.getInstance().getTime();
    }

    @Override
    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    private void onStep(QuoridorMovement movement, QuoridorPlayer player, StateCode stateCode) {
//        this.steps.add(Step.newStep()
//                .movement(movement)
//                .player(player)
//                .startTime(Calendar.getInstance().getTime())
//                .stateCode(stateCode)
//                .build());

        this.record.add(ClientStep
                .buildFromMovement(player, movement, stateCode));

        onStep(movement, player);
    }

    @Override
    protected void onStep(QuoridorMovement movement, QuoridorPlayer player) {

    }

    public QuoridorGameWrapper asPlayer(QuoridorPlayer player) throws QuoridorException {
       if(gameWrappers.get(player) == null) {
           throw new QuoridorException("No such player added.");
       }

        return gameWrappers.get(player);
    }

    private StateCode move(QuoridorPlayer player, ActorMovement movement) throws QuoridorException {
        return board.moveActor(player, movement);
    }

    private StateCode move(QuoridorPlayer player, BlockMovement movement) throws QuoridorException {
        return board.addBlock(player, movement);
    }

    public Point getPlayerLoc(int playerOrder) {
        return this.board.getActorPoint(playerOrder);
    }

    public ClientRoundRecord getStepLog() {
        return this.record;
    }

    Board getBoard() {
        return board;
    }

    //    public void toCsv(String name) {
//        for (Step step: ) {
//
//        }
//    }
}
