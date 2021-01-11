package org.cic.yu.multiplayer.quoridor;

import org.cic.yu.Application;
import org.cic.yu.game.quoridor.ActorMovement;
import org.cic.yu.multiplayer.quoridor.data.PlayerRequest;
import org.cic.yu.multiplayer.quoridor.data.StepRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class FakeClient {
    private static int idCount = 0;

    private int retCode = 0;
    private String key;
    private final int listenPort;
    private int id = 0;

    public FakeClient(int listenPort, String key) {
        this.listenPort = listenPort;
        this.id = idCount++;

        if (key.length() < 64) {
            StringBuilder sb = new StringBuilder(key);
            for (int i = 0; i < 64 - key.length(); i++) {
                sb.append("_");
            }
            this.key = sb.toString();
        } else {
            this.key = key;
        }
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public void start() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FakeClient.this.run();
            }
        });

        t.start();
    }

    public void run() {
        try {
            //创建Socket对象
            Socket s = new Socket(InetAddress.getByName("127.0.0.1"), this.listenPort);
            ReentrantLock lock = new ReentrantLock();
            CountDownLatch latch = new CountDownLatch(99999);

            Thread sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //获取输出流对象
                    OutputStream os = null;
                    try {
                        while (!s.isClosed()) {
                            if (Application.DEBUG) {
                                Logger.getAnonymousLogger().info("Sending...");
                            }
                            os = s.getOutputStream();
                            PlayerRequest request = new PlayerRequest(key);

                            //发送数据
                            if (retCode != 1002) {
                                os.write(request.toBytes());
                            } else {
                                os.write(StepRequest.newStepRequest()
                                        .movement(new ActorMovement(5, 2)).key(key).build().toBytes());
                            }

                            if (Application.DEBUG) {
                                Logger.getAnonymousLogger().info("Sent!");
                            }

                            synchronized (lock) {
                                if (Application.DEBUG) {
                                    Logger.getAnonymousLogger().info("I'm going to lock.");
                                }
                                lock.wait();
                            }
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            Thread receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!s.isClosed()) {
                        if (Application.DEBUG) {
                            System.out.println(String.format("client %d report in.", FakeClient.this.id));
                        }
                        if (Application.DEBUG) {
                            Logger.getAnonymousLogger().info("Receiving...");
                        }
                        InputStream is = null;
                        try {
                            is = s.getInputStream();
                            InetAddress address = s.getInetAddress();
                            byte[] bys = new byte[1024];
                            int len;
                            len = is.read(bys);
                            ByteBuffer buffer = ByteBuffer.wrap(bys);
                            int type = buffer.getInt();

                            if (Application.DEBUG) {
                                System.out.println("sender:" + address);
                                System.out.println("length:" + len);
                                System.out.println("type:" + type);
                                System.out.println("auth_state:" + buffer.getInt());
                            }

                            switch (type) {
                                case 1001:
                                    if (Application.DEBUG) {
                                        Logger.getAnonymousLogger().info("Something wrong about server verification.");
                                    }
                                    throw new RuntimeException();

                                case 1002:
                                    if (Application.DEBUG) {
                                        System.out.println("game_state:" + buffer.getInt());
                                        System.out.println("actor_loc:" + buffer.getInt() + "," + buffer.getInt());
                                        System.out.println("the_other_actor_loc:" + buffer.getInt() + "," + buffer.getInt());
                                        System.out.println("block_loc:" + buffer.getInt() + "," + buffer.getInt()
                                                + "," + buffer.getInt() + "," + buffer.getInt());
                                    }
                                    if (Application.DEBUG) {
                                        Logger.getAnonymousLogger().info("Received changes about the board.");
                                    }
                                    retCode = 1002;

                                    break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        latch.countDown();

//                        if (Application.DEBUG) {Logger.getAnonymousLogger().info("Try notify...");}
                        synchronized (lock) {
                            lock.notify();
//                            if (Application.DEBUG) {Logger.getAnonymousLogger().info("notify(done)");}
                        }
                    }
                }
            });

            receiveThread.start();
            sendThread.start();

            latch.await();
            //释放
            s.close();
        } catch (Exception e) {
            if (Application.DEBUG) {
                Logger.getAnonymousLogger().info("Client 1 is trying...");
            }
            run();
        }
    }
}
