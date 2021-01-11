package org.cic.yu.multiplayer.quoridor;

import org.cic.yu.Application;
import org.cic.yu.game.quoridor.QuoridorMovement;
import org.cic.yu.game.quoridor.QuoridorPlayer;
import org.cic.yu.game.quoridor.StateCode;
import org.cic.yu.multiplayer.quoridor.data.AuthResponse;
import org.cic.yu.multiplayer.quoridor.data.ChangeResponse;
import org.cic.yu.multiplayer.quoridor.data.StepRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Seat {
    private static int idCount = 0;

    private final int ID;

    private QuoridorPlayer player;
    private boolean ready;
    private Socket socket;
    private InetAddress address;

    public Seat() {
        this.ID = idCount++;
    }

    boolean isReady() {
        return ready;
    }

    void setReady(boolean ready) {
        this.ready = ready;
    }

    public QuoridorPlayer getPlayer() {
        return player;
    }


    Socket getSocket() {
        return socket;
    }

    void setSocket(Socket socket) {
        this.socket = socket;
        this.address = socket.getInetAddress();
    }

    void shutdown() throws IOException {
        if (isReady()) {
            getSocket().close();
            this.setReady(false);
        }
    }

    public void setPlayer(QuoridorPlayer player) {
        this.player = player;
    }

    void notifyChange(ChangeResponse change) throws Exception {
        OutputStream os = null;

        os = socket.getOutputStream();
        os.write(change.toBytes());
    }

    QuoridorMovement receiveMovement() throws IOException {
        InputStream is = getSocket().getInputStream();
        byte[] bytes = new byte[1024];
        int len = is.read(bytes);

        assert len == StepRequest.BYTE_LENGTH : "StepRequest byte length is incorrect:" + len;

//        if (Application.DEBUG) {
//            System.out.println("Received StepRequest:");
//            StepRequest.dump(bytes);
//        }

        StepRequest stepRequest = StepRequest.fromBytes(bytes);

        return stepRequest.getMovement();
    }

    @Override
    public String toString() {
        return "Seat{" +
                "ID=" + ID +
                ", player=" + player +
                ", socket=" + socket +
                '}';
    }

    void notifySuccess() {
        if (Application.DEBUG) {
            Logger.getAnonymousLogger().info("Notify success: " + getPlayer() + "---"
                    + ChangeResponse.buildWinOrLoseResponse(StateCode.WIN));
        }

        OutputStream os = null;
        try {
            os = getSocket().getOutputStream();
            os.write(ChangeResponse.buildWinOrLoseResponse(StateCode.WIN).toBytes());
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, e.toString());
            if (Application.DEBUG) {
                e.printStackTrace();
            }
            this.setReady(false);
        }
    }

    void notifyFail(StateCode failStateCode) {
        if (Application.DEBUG) {
            Logger.getAnonymousLogger().info("Notify fail: " + getPlayer() + "---"
                    + ChangeResponse.buildWinOrLoseResponse(failStateCode));
        }

        try {
            OutputStream os = null;
            os = getSocket().getOutputStream();
            os.write(ChangeResponse.buildWinOrLoseResponse(failStateCode).toBytes());
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, e.toString());
            if (Application.DEBUG) {
                e.printStackTrace();
            }
            this.setReady(false);
        }
    }


    // TODO: 2020/4/22 EXCEPTION HANDLING
    void notifyRetry() throws IOException {
        if (Application.DEBUG) {
            Logger.getAnonymousLogger().info("Notify retry: " + getPlayer() + "---"
                    + ChangeResponse.newChangeResponse()
                    .stateCode(StateCode.ILLEGAL_MOVE)
                    .build());
        }

        OutputStream os = getSocket().getOutputStream();
        os.write(ChangeResponse.newChangeResponse()
                .stateCode(StateCode.ILLEGAL_MOVE)
                .build().toBytes());
    }

    @Override
    public int hashCode() {
        return this.ID;
//        if (getPlayer() == null) {
//            return this.ID;
//        } else {
//            return getPlayer().getKey().hashCode();
//        }
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }


    void notifyCloseConnection() {
        if (Application.DEBUG) {
            Logger.getAnonymousLogger().info("Notify close connection: " + getPlayer() + "---"
                    + AuthResponse.buildCloseConnectionResponse());
        }

        try {
            OutputStream os = null;
            os = getSocket().getOutputStream();
            os.write(AuthResponse.buildCloseConnectionResponse().toBytes());
            getSocket().close();
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, e.toString());
            if (Application.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            this.setReady(false);
        }
    }

    public String getIpAddress() {
        return this.address.toString();
    }

    static String entryHeaderString() {
        return "id, name, mac, ip, port";
    }

    public String toEntry() {
        return getPlayer().getId() +
                "," +
                getPlayer().getName() +
                "," +
                getPlayer().getMacAddress() +
                "," +
                getIpAddress() +
                "," +
                getSocket().getPort();
    }
}
