package org.cic.yu.multiplayer.quoridor;

import org.cic.yu.Application;
import org.cic.yu.game.Player;
import org.cic.yu.game.quoridor.QuoridorGame;
import org.cic.yu.multiplayer.MultiPlayerException;
import org.cic.yu.multiplayer.quoridor.data.ClientRoundRecord;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class GameTable {
    private QuoridorGame game;
    private List<Seat> seats;
    private Dealer dealer;
    private ClientRoundRecord record;
    private GameResult result;

    public GameTable() {
        init();
    }

    private void init() {
        this.game = new QuoridorGame();
        this.dealer = Dealer.build(this.game);
    }

    void addSeat(Seat seat, int order) {
        dealer.serve(seat, order);
    }

    void startGame() {
        try {
            dealer.run();
        } catch (MultiPlayerException e) {
            e.printStackTrace();
        }

        this.result = dealer.getGameResult();
        this.record = this.game.getStepLog();
    }

    private boolean playersPrepared() {
        for(Seat seat:seats) {
            if(!seat.isReady()) {
                return false;
            }
        }

        return true;
    }

    void shutdown() {
        Waiter.getINSTANCE().dismiss();
        for (Seat s: seats) {
            try {
                s.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    /**
//     * network debugging.
//     * @param time repeat times for every seat.
//     */
//    public void testNetwork(int time) {
//        for (int i = 0; i < time; i++) {
//            System.out.println("*********************************");
//            System.out.println("*********************************");
//            System.out.println("**************" + i + "***************");
//            System.out.println("*********************************");
//            System.out.println("*********************************");
//            for (Seat seat:seats) {
//                seat.notifyChange(ChangeResponse.newChangeResponse()
//                        .state(AuthResponse.State.OK)
//                        .stateCode(StateCode.OK)
//                        .actorLoc(Board.ACTOR1_INITIAL_LOC.clonePoint())
//                        .theOtherActorNewLoc(Board.ACTOR2_INITIAL_LOC.clonePoint())
//                        .build());
//            }
//        }
//    }

    ClientRoundRecord getGameStepLog() {
        return this.record;
    }

    public GameResult getGameResult() {
        return this.result;
    }

    public static class GameResult {
        Player winner;
        Player looser;
        int totalStep;

        public GameResult(Player winner, Player looser, int totalStep) {
            this.winner = winner;
            this.looser = looser;
            this.totalStep = totalStep;
        }

        public Player getWinner() {
            return winner;
        }

        public Player getLoser() {
            return looser;
        }

        public int getTotalStep() {
            return totalStep;
        }

        @Override
        public String toString() {
            return "GameResult{" +
                    "winner=" + winner.getName() +
                    ", looser=" + looser.getName() +
                    '}';
        }
    }

    public static void main(String[] args) {
        GameTable table = new GameTable();
        if (Application.DEBUG) {Logger.getAnonymousLogger().info("before_server");}
//        table.startServe();
        if (Application.DEBUG) {Logger.getAnonymousLogger().info("after_serve");}
        table.shutdown();
    }
}

