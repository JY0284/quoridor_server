package org.cic.yu.multiplayer.quoridor.data;

import org.cic.yu.game.Player;
import org.cic.yu.multiplayer.quoridor.GameTable;
import org.cic.yu.multiplayer.quoridor.Seat;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ClientRoundRecord extends ArrayList<ClientStep> {
    private GameTable.GameResult result;
    private Seat offensiveSeat;
    private Seat defensiveSeat;

    public GameTable.GameResult getResult() {
        return result;
    }

    public void setResult(GameTable.GameResult result) {
        this.result = result;
    }

    public Player getOffensivePositionPlayer() {
        return offensiveSeat.getPlayer();
    }

    public Player getDefensivePositionPlayer() {
        return defensiveSeat.getPlayer();
    }

    public void setOffensiveSeat(Seat offensiveSeat) {
        this.offensiveSeat = offensiveSeat;
    }

    public void setDefensiveSeat(Seat defensiveSeat) {
        this.defensiveSeat = defensiveSeat;
    }

    public Seat getOffensiveSeat() {
        return offensiveSeat;
    }

    public Seat getDefensiveSeat() {
        return defensiveSeat;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("ClientRecord{" +
                "result=" + result +
                ", offensivePositionPlayer=" + getOffensivePositionPlayer() +
                ", defensivePositionPlayer=" + getDefensivePositionPlayer() +
                '}' + '\n');

        for (ClientStep step : this) {
            stringBuilder.append(step).append("\n");
        }

        return stringBuilder.toString();
    }

    public void toCsv(String path) {
        try {
            FileWriter myWriter = new FileWriter(path);
            myWriter.write(ClientStep.headerStr() + "\n");
            for (ClientStep step : this) {
                myWriter.write(step.toEntry() + "\n");
            }
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
