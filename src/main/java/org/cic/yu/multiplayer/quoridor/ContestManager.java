package org.cic.yu.multiplayer.quoridor;

import org.cic.yu.Application;
import org.cic.yu.game.Player;
import org.cic.yu.multiplayer.quoridor.data.ClientRecord;
import org.cic.yu.multiplayer.quoridor.data.ClientRoundRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class ContestManager {

    static class Pair<T, V> {
        T key;
        V value;

        Pair(T key, V val) {
            this.key = key;
            this.value = val;
        }

        T getKey() {
            return key;
        }

        V getValue() {
            return value;
        }
    }

    public enum ContestType {
        KNOCK_OUT,
        VERSUS_EVERYONE;
    }

    private final String contestLogPathPrefix;
    private List<ClientRoundRecord> records;
    private List<Seat> seats;
    private Map<Integer, Boolean> gameMap;
    private FileWriter gameResultFileWriter;

    public ContestManager(List<Seat> seats) {
        this.seats = seats;
        this.records = new LinkedList<>();
        this.gameMap = new HashMap<>();
        this.contestLogPathPrefix = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());

        File file = new File(contestLogPathPrefix);
        //noinspection ResultOfMethodCallIgnored
        file.mkdirs();

        writePlayerList(seats);

        try {
            gameResultFileWriter = new FileWriter((contestLogPathPrefix + "/" + "result.csv"));
            gameResultFileWriter.write("time, player_name, score\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writePlayerList(List<Seat> seats) {
        try {
            FileWriter writer = new FileWriter(contestLogPathPrefix + "/" + "player_list.csv");
            writer.write(Seat.entryHeaderString() + "\n");
            for (Seat seat : seats) {
                if(seat.isReady()) {
                    writer.write(seat.toEntry() + "\n");
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startKnockoutContest() {
        Random random = new Random(new Date().getTime());
        List<Seat> enrollingSeats = new ArrayList<>(seats);
        List<Pair<Seat, Seat>> competingPairs = new ArrayList<>();
        int round = 1;
        List<ClientRecord> gameRecords = new LinkedList<>();

        while (enrollingSeats.size() > 1) {
            String roundPathPrefix = startRound(round);

            buildCompetePairs(random, enrollingSeats, competingPairs);

            if (enrollingSeats.size() > 0) {
                System.out.println(String.format("[INFO] %s is lucky and will directly show in next round[round %d].",
                        enrollingSeats.get(0).getPlayer().getName(),
                        round + 1));
            }

            CountDownLatch countDownLatch = new CountDownLatch(competingPairs.size());

            doContestOnThreads(competingPairs, gameRecords, countDownLatch);

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (ClientRecord gameRecord : gameRecords) {
                Seat seat1 = gameRecord.get(0).getDefensiveSeat();
                Seat seat2 = gameRecord.get(0).getOffensiveSeat();

                if (gameRecord.getWinner() == null) {
                    System.out.println(String.format("[INFO] %s and %s had a draw, " +
                                    "so both of them will show in next round[round %d].",
                            seat1.getPlayer().getName(),
                            seat2.getPlayer().getName(),
                            round + 1));

                    enrollingSeats.add(seat1);
                    enrollingSeats.add(seat2);
                } else {
                    if (gameRecord.getWinner().equals(seat1.getPlayer())) {
                        enrollingSeats.add(seat1);
                    } else {
                        enrollingSeats.add(seat2);
                    }

                    System.out.println(String.format("[%d : %d] ",
                            gameRecord.winRounds(gameRecord.getWinner()),
                            gameRecord.winRounds(gameRecord.getLoser()))
                            + gameRecord.getWinner().getName()
                            + " defeat--> "
                            + gameRecord.getLoser().getName());

                    updateResultFile(round, gameRecord.getLoser());
                }

                gameRecord.toCsv(roundPathPrefix,
                        String.format("%s_VS_%s.csv",
                                seat1.getPlayer().getName(),
                                seat2.getPlayer().getName()));
            }

//            gameRecords.add(gameRecord);

            gameRecords.clear();
            competingPairs.clear();

            round++;

            System.out.println("--------------------------------------------------");
        }

        System.out.println("CHAMPION: " + enrollingSeats.get(0).getPlayer());
        updateResultFile(round, enrollingSeats.get(0).getPlayer());

        closeResultFile();
    }

    private void closeResultFile() {
        try {
            gameResultFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateResultFile(int round, Player defeatedPlayer) {
        StringBuilder resultRow = new StringBuilder();
        resultRow.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append(',');
        resultRow.append(defeatedPlayer.getName()).append(',');
        resultRow.append(round).append('\n');
        try {
            gameResultFileWriter.write(resultRow.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doContestOnThreads(List<Pair<Seat, Seat>> competingPairs,
                                    List<ClientRecord> records,
                                    CountDownLatch countDownLatch) {
        for (Pair<Seat, Seat> pair:competingPairs) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
//                        System.out.println(pair.getKey().getPlayer() + " vs " + pair.getValue().getPlayer());
                    List<ClientRoundRecord> roundRecords = doContest(pair.getKey(), pair.getValue());
                    ClientRecord clientRecord = new ClientRecord();
                    for (ClientRoundRecord roundRecord : roundRecords) {
                        //noinspection UseBulkOperation
                        clientRecord.add(roundRecord);
                    }
                    synchronized (records) {
                        records.add(clientRecord);

                        countDownLatch.countDown();
                    }
                }
            });
            thread.start();
        }
    }

    private void buildCompetePairs(Random random, List<Seat> enrollingSeats, List<Pair<Seat, Seat>> competingPairs) {
        while (enrollingSeats.size() >= 2) {
            int idx1 = random.nextInt(enrollingSeats.size());
            Seat seat1 = enrollingSeats.get(idx1);
            if (!seat1.isReady()) {
                enrollingSeats.remove(idx1);

                continue;
            }
            enrollingSeats.remove(idx1);

            int idx2 = random.nextInt(enrollingSeats.size());
            Seat seat2 = enrollingSeats.get(idx2);
            if (!seat2.isReady()) {
                enrollingSeats.remove(idx2);
                enrollingSeats.add(seat1);

                continue;
            }
            enrollingSeats.remove(idx2);

            competingPairs.add(new Pair<>(seat1, seat2));
        }
    }

    private String startRound(int round) {
        System.out.println("**************************************************");
        System.out.println("Knockout Round " + round + " :");
        System.out.println("**************************************************");

        String roundPathPrefix = contestLogPathPrefix + "/" + "round_" + round + "/";
        File roundDir = new File(roundPathPrefix);
        roundDir.mkdirs();
        return roundPathPrefix;
    }

    public void startContest(ContestType type) {
        System.out.println("Contest start at: " +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        System.out.println("-----------------------****-----------------------");


        switch (type) {
            case VERSUS_EVERYONE:
                startEveryoneVersusEveryoneContest();
                break;
            case KNOCK_OUT:
                startKnockoutContest();
                break;
        }

        System.out.println("-----------------------****-----------------------");
        System.out.println("Contest end at: " +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    private void startEveryoneVersusEveryoneContest() {
        for (Seat seat1 : seats) {
            for (Seat seat2 : seats) {
                Integer hashKey1 = seat1.hashCode() + seat2.hashCode() * seats.size();
                Integer hashKey2 = seat2.hashCode() + seat1.hashCode() * seats.size();
                if (seat1 != seat2 && gameMap.get(hashKey1) == null) {
                    gameMap.put(hashKey1, true);
                    gameMap.put(hashKey2, true);
                } else {
                    continue;
                }

                System.out.println(String.format("[Contest] %s : %s", seat1.getPlayer(), seat2.getPlayer()));

                List<ClientRoundRecord> results = doContest(seat1, seat2);
                records.addAll(results);

                outputLogCsv(contestLogPathPrefix, seat1, seat2, results);
            }
        }

        if (Application.DEBUG) {
            for (ClientRoundRecord record : records) {
                System.out.println(record);
            }
        }
    }

    private void outputLogCsv(String pathPrefix, Seat seat1, Seat seat2, List<ClientRoundRecord> results) {
        //noinspection ResultOfMethodCallIgnored
        new File(pathPrefix + "/" + seat1.getPlayer().getName()).mkdirs();
        //noinspection ResultOfMethodCallIgnored
        new File(pathPrefix + "/" + seat2.getPlayer().getName()).mkdirs();

        for (ClientRoundRecord record:results) {
            record.toCsv(String.format(pathPrefix + "/" + "%s/%s_%s.csv",
                    seat1.getPlayer().getName(),
                    record.getOffensivePositionPlayer().getName(),
                    record.getDefensivePositionPlayer().getName()));
            record.toCsv(String.format(pathPrefix + "/" + "%s/%s_%s.csv",
                    seat2.getPlayer().getName(),
                    record.getOffensivePositionPlayer().getName(),
                    record.getDefensivePositionPlayer().getName()));
        }
    }

    private List<ClientRoundRecord> doContest(Seat seat1, Seat seat2) {
       ClientRecord results = new ClientRecord();
        for (int i = 0; i < Application.ROUNDS_PER_GAME; i++) {
            GameTable gameTable = new GameTable();
            Seat offensiveSeat, defensiveSeat;

            // TODO: 2020/4/16 when the number of players in a game is more than 2, this logic must be updated.
            if (i % 2 == 0) {
                offensiveSeat = seat1;
                defensiveSeat = seat2;
            } else {
                offensiveSeat = seat2;
                defensiveSeat = seat1;
            }
            gameTable.addSeat(offensiveSeat, 0);
            gameTable.addSeat(defensiveSeat, 1);

            if (Application.DEBUG) {
                Logger.getAnonymousLogger().info("offensive:" + offensiveSeat.getPlayer() + ","
                        + "defensive:" + defensiveSeat.getPlayer());
            }

            gameTable.startGame();

            if (Application.DEBUG) {
                Logger.getAnonymousLogger().info("result:" + gameTable.getGameResult());
            }

            ClientRoundRecord record = gameTable.getGameStepLog();
            record.setResult(gameTable.getGameResult());
            record.setOffensiveSeat(offensiveSeat);
            record.setDefensiveSeat(defensiveSeat);
            results.add(record);
        }

        return results;
    }
}
