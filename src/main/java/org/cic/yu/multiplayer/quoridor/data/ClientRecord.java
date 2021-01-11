package org.cic.yu.multiplayer.quoridor.data;

import org.cic.yu.Application;
import org.cic.yu.game.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ClientRecord extends LinkedList<ClientRoundRecord> {
    private Map<Player, Integer> winnerTotalStepMap = new HashMap<>(Application.PLAYERS_IN_A_GAME);
    private Map<Player, Integer> playerWinCountMap = new HashMap<>(Application.PLAYERS_IN_A_GAME);
    private Player winner;
    private Player loser;

    @Override
    public boolean add(ClientRoundRecord roundRecord) {
        boolean ret = super.add(roundRecord);
        if (ret) {
            Player roundWinner = roundRecord.getResult().getWinner();
            Player roundLoser = roundRecord.getResult().getLoser();

            int roundStep = roundRecord.getResult().getTotalStep();
            winnerTotalStepMap.putIfAbsent(roundWinner, 0);
            winnerTotalStepMap.put(roundWinner, winnerTotalStepMap.get(roundWinner) + roundStep);

            playerWinCountMap.putIfAbsent(roundWinner, 0);
            playerWinCountMap.putIfAbsent(roundLoser, 0);

            // TODO: 2020/4/22 平局处理
            // 不会出现回合内的平局？
            playerWinCountMap.put(roundWinner, playerWinCountMap.get(roundWinner) + 1);

            if (playerWinCountMap.get(roundWinner).equals(playerWinCountMap.get(roundLoser))) {
                this.winner = getWinnerSortedByStep();
                this.loser = getLooserSortedByStep();

                if (!hasWinner()) {
                    this.winner = null;
                    this.loser = null;
                }
            } else {
                if (playerWinCountMap.get(roundWinner) > playerWinCountMap.get(roundLoser)) {
                    this.winner = roundWinner;
                    this.loser = roundLoser;
                } else {
                    this.winner = roundLoser;
                    this.loser = roundWinner;
                }
            }
        }

        return ret;
    }

    private Player getWinnerSortedByStep() {
        int max = 0;
        Player key = null;
        for (Map.Entry<Player, Integer> entry : winnerTotalStepMap.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                key = entry.getKey();
            }
        }

        return key;
    }

    private Player getLooserSortedByStep() {
        int min = Integer.MAX_VALUE;
        Player key = null;
        for (Map.Entry<Player, Integer> entry : winnerTotalStepMap.entrySet()) {
            if (entry.getValue() < min) {
                min = entry.getValue();
                key = entry.getKey();
            }
        }

        return key;
    }

    public Player getWinner() {
        return winner;
    }

    public Player getLoser() {
        return loser;
    }

    public int winRounds(Player player) {
        return this.playerWinCountMap.get(player);
    }

//    public Player getPlayerByIdx(int idx) throws NullPointerException {
//        return winnerTotalStepMap.keySet().toArray(new Player[0])[idx];
//    }

    private boolean hasWinner() {
        if (winner == null && loser == null) {
            return false;
        } else if(loser == null) {
            return true;
        } else {
            // loser != null
            return !playerWinCountMap.get(winner).equals(playerWinCountMap.get(loser));
        }
    }

    /**
     * Record to csv with round number as file name prefix.
     * @param path e.g. "directory1/"
     * @param name name of the csv file, end with ".csv
     */
    public void toCsv(String path, String name) {
        for (int i = 0;i < this.size();i++) {
            ClientRoundRecord roundRecord = this.get(i);
            roundRecord.toCsv(path + i + "_" + name);
        }
    }
}
