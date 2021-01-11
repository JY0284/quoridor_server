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

public class FakeClient1 {
    private static int retCode = 0;
    public static void main(String[] args) {
        try {
            //创建Socket对象
            Socket s = new Socket(InetAddress.getByName("127.0.0.1"), Application.WAITER_LISTENING_PORT);
            ReentrantLock lock = new ReentrantLock();
            CountDownLatch latch = new CountDownLatch(3);

            Thread sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //获取输出流对象
                    OutputStream os = null;
                    try {
                        while(true) {
                            Logger.getAnonymousLogger().info("Sending...");
                            os = s.getOutputStream();
                            String key = "client1890123456789012345678901234567890123456789012345678901234";
                            PlayerRequest request =
                                    new PlayerRequest(key);

                            //发送数据
                            if(retCode != 1002) {
                                os.write(request.toBytes());
                            } else {
                                os.write(StepRequest.newStepRequest()
                                .movement(new ActorMovement(5, 2)).key(key).build().toBytes());
                            }

                            Logger.getAnonymousLogger().info("Sent!");

                            synchronized (lock) {
                                Logger.getAnonymousLogger().info("I'm going to lock.");
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
                    while (true) {
                        Logger.getAnonymousLogger().info("Receiving...");
                        InputStream is = null;
                        try {
                            is = s.getInputStream();
                            InetAddress address = s.getInetAddress();
                            byte[] bys = new byte[1024];
                            int len;
                            len = is.read(bys);
                            System.out.println("sender:" + address);
                            System.out.println("length:" + len);
                            ByteBuffer buffer = ByteBuffer.wrap(bys);
                            int type = buffer.getInt();
                            System.out.println("type:" + type);
                            System.out.println("auth_state:" + buffer.getInt());


                            switch (type) {
                                case 1001:
                                    Logger.getAnonymousLogger().info("Something wrong about server verification.");
                                    throw new RuntimeException();

                                case 1002:
                                    System.out.println("game_state:" + buffer.getInt());
                                    System.out.println("actor_loc:" + buffer.getInt() + "," + buffer.getInt());
                                    System.out.println("the_other_actor_loc:" + buffer.getInt() + "," + buffer.getInt());
                                    System.out.println("block_loc:" + buffer.getInt() + "," + buffer.getInt()
                                            + "," + buffer.getInt() + "," + buffer.getInt());

                                    Logger.getAnonymousLogger().info("Received changes about the board.");
                                    retCode = 1002;

                                    break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

//                        latch.countDown();
//                        if (latch.getCount() == 0) {
//                            try {
//                                s.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            break;
//                        }

                        Logger.getAnonymousLogger().info("Try notify...");
                        synchronized (lock) {
                            lock.notify();
                            Logger.getAnonymousLogger().info("notify(done)");
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
            Logger.getAnonymousLogger().info("Client 1 is trying...");
            main(null);
        }
    }
}
