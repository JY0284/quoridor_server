package org.cic.yu.multiplayer.quoridor.data;

//import com.opencsv.bean.CsvBindByName;
import org.cic.yu.game.quoridor.*;

/**
 * The step model for game log.
 *
 * Sample csv content:
 * player,newLoc_x,newLoc_y,newBar_start_x,newBar_start_y,newBar_stop_x,newBar_stop_y,status
 * Me,5,2,-1,-1,-1,-1,0
 * Enemy,5,8,4,7,6,7,0
 */
public class ClientStep {
    private static final String[] header = new String[] {
            "player","newLoc_x","newLoc_y","newBar_start_x","newBar_start_y","newBar_stop_x","newBar_stop_y","status"
    };

//    @CsvBindByName (required = true)
    private String player;

//    @CsvBindByName (column = "newLoc_x", required = true)
    private int actorX;

//    @CsvBindByName (column = "newLoc_y", required = true)
    private int actorY;

//    @CsvBindByName (column = "newBar_start_x", required = true)
    private int barX1;

//    @CsvBindByName (column = "newBar_start_y", required = true)
    private int barY1;

//    @CsvBindByName (column = "newBar_stop_x", required = true)
    private int barX2;

//    @CsvBindByName (column = "newBar_stop_y", required = true)
    private int barY2;

//    @CsvBindByName (column = "status", required = true)
    private int stateCode;

    @Override
    public String toString() {
        return "LogStep{" +
                "player='" + player + '\'' +
                ", actorX=" + actorX +
                ", actorY=" + actorY +
                ", barX1=" + barX1 +
                ", barY1=" + barY1 +
                ", barX2=" + barX2 +
                ", barY2=" + barY2 +
                ", stateCode=" + stateCode +
                '}';
    }

    public static ClientStep buildFromMovement(QuoridorPlayer player, QuoridorMovement movement, StateCode stateCode) {
        ClientStep clientStep = new ClientStep();
        clientStep.player = player.getName();

        if (movement instanceof BlockMovement) {
            clientStep.actorX = -1;
            clientStep.actorY = -1;
            clientStep.barX1 = movement.getP1().x;
            clientStep.barY1 = movement.getP1().y;
            clientStep.barX2 = movement.getP2().x;
            clientStep.barY2 = movement.getP2().y;
        }
        if (movement instanceof ActorMovement) {
            clientStep.actorX = ((ActorMovement) movement).getLoc().x;
            clientStep.actorY = ((ActorMovement) movement).getLoc().y;
            clientStep.barX1 = -1;
            clientStep.barY1 = -1;
            clientStep.barX2 = -1;
            clientStep.barY2 = -1;
        }

        clientStep.stateCode = stateCode.getValue();

        return clientStep;
    }

    static String[] header() {
        return header;
    }

    static String headerStr() {
        StringBuilder sb = new StringBuilder();
        for (String colName:header) {
            sb.append(colName).append(',');
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    String toEntry() {
        return player + "," +
                actorX + "," +
                actorY + "," +
                barX1 + "," +
                barY1 + "," +
                barX2 + "," +
                barY2 + "," +
                stateCode;
    }
}
