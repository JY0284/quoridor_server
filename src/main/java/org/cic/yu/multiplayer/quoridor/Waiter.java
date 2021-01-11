package org.cic.yu.multiplayer.quoridor;

import org.cic.yu.Application;
import org.cic.yu.game.quoridor.QuoridorPlayer;
import org.cic.yu.multiplayer.Checker;
import org.cic.yu.multiplayer.MultiPlayerException;
import org.cic.yu.multiplayer.quoridor.data.PlayerRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Waiter {
    private static final Waiter INSTANCE = new Waiter();
    private ExecutorService executor = Executors.newFixedThreadPool(Application.PLAYERS_IN_THIS_CONTEST);
    private ServerSocket serverSocket;
    private static final String TAG = "MULTI-PLAYER";
    private List<Seat> seats;
    private int validPlayerCount = 0;

    private Waiter() {
    }

    ServerSocket getServerSocket() {
        return serverSocket;
    }

    public static Waiter getINSTANCE() {
        return INSTANCE;
    }

    public void dismiss() {
        try {
            serverSocket.close();
            executor.shutdown();

            for (Seat seat:seats) {
                if (seat.isReady()) {
                    seat.notifyCloseConnection();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.validPlayerCount = 0;

        if (Application.DEBUG) {Logger.getAnonymousLogger().info("The Waiter is dismissed.");}
    }

    /**
     * Notice that this method will block until all the seats are ready to play.
     * The seats number is the same as the config in Application class.
     * @return ready seats list.
     */
    public List<Seat> serve() {
        List<Seat> seats = new ArrayList<>(Application.PLAYERS_IN_THIS_CONTEST);
        this.seats = seats;

        for (int i = 0; i < Application.PLAYERS_IN_THIS_CONTEST; i++) {
            seats.add(new Seat());
        }

        try {
            serve(seats);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return seats;
    }

    private void serve(List<Seat> seats) throws IOException {
        if (serverSocket == null) {
            serverSocket = new ServerSocket(Application.WAITER_LISTENING_PORT);
        }

        CountDownLatch waiterLock = new CountDownLatch(seats.size());

        for (Seat s : seats) {
            try {
                serve(waiterLock, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            System.out.println("Waiter is waiting for the players to sit down.");
            waiterLock.await();
            System.out.println("Waiter's work is done.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (Application.DEBUG) {Logger.getAnonymousLogger().info("Waiter is leaving.");}
    }

    private void serve(CountDownLatch latch, Seat seat) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Application.DEBUG) {Logger.getAnonymousLogger().info("Serving " + seat);}
                    while (!serverSocket.isClosed() && !seat.isReady()) {
                        if (seat.getSocket() == null) {
                            seat.setSocket(serverSocket.accept());
                        }
                        Socket s = seat.getSocket();
                        InputStream is = s.getInputStream();
                        byte[] bys = new byte[1024];
                        int len = is.read(bys);

                        // Due to the previous design, the method to judge request by the 1st byte
                        // is very out of fashion and probably introduce many potential bugs.
                        ByteBuffer buffer = ByteBuffer.wrap(bys);
                        int identifier = buffer.getInt();

                        if (Application.DEBUG) {Logger.getAnonymousLogger().info("Waiter received a request: type = " + identifier);}

                        PlayerRequest request;
                        if (identifier == PlayerRequest.identifier()) {
                            request = new PlayerRequest(new String(bys, buffer.position(), len));
                        } else {
                            throw new MultiPlayerException("Not a valid request.");
                        }

                        if (Application.DEBUG) {
                            Logger.getAnonymousLogger().info("Received PlayerRequest:" + request);
                        }

                        QuoridorPlayer player = Checker.getInstance().verify(request);
                        if (player == null) {
                            throw new MultiPlayerException("Key error: " + request.getKey());
                        } else {
                            validPlayerCount++;
                            System.out.println(String.format("[INFO] %10s is ready at %s. (%d/%d)", player.getName(),
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                                    Waiter.this.validPlayerCount,
                                    Waiter.this.seats.size()));
                        }

                        seat.setPlayer(player);
                        seat.setReady(true);

                        if (Application.DEBUG) {
                            InetAddress address = s.getInetAddress();
                            Logger.getAnonymousLogger().info("sender:" + address);
                            Logger.getAnonymousLogger().info(request.getKey());
                        }

                        if (Application.DEBUG) {Logger.getAnonymousLogger().info("The seat is ready: " + seat);}
                    }

                } catch (IOException | MultiPlayerException e) {
                    e.printStackTrace();
                }

                latch.countDown();
            }
        });
    }
}
