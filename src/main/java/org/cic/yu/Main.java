package org.cic.yu;

import org.apache.commons.cli.*;
import org.cic.yu.game.Game;
import org.cic.yu.game.GameException;
import org.cic.yu.game.Player;
import org.cic.yu.game.quoridor.ActorMovement;
import org.cic.yu.game.quoridor.BlockMovement;
import org.cic.yu.game.quoridor.QuoridorGame;
import org.cic.yu.game.quoridor.QuoridorPlayer;
import org.cic.yu.multiplayer.quoridor.ContestManager;
import org.cic.yu.multiplayer.quoridor.FakeClient;
import org.cic.yu.multiplayer.quoridor.Seat;
import org.cic.yu.multiplayer.quoridor.Waiter;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        extractCmdArgs(args);
        printVersionInfo(args);

        List<Seat> seats = Waiter.getINSTANCE().serve();

        ContestManager contestManager = new ContestManager(seats);
        contestManager.startContest(ContestManager.ContestType.KNOCK_OUT);

        Waiter.getINSTANCE().dismiss();
    }

    private static void printVersionInfo(String[] args) {
        System.out.println("Server Version: " + Application.VERSION);
        System.out.println("Args:" + Arrays.toString(args));
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        System.out.println("--------------------------------------------------");
    }

    private static void buildAndStartTestClients() {
        for (int i = 0; i < Application.PLAYERS_IN_THIS_CONTEST; i++) {
            FakeClient client = new FakeClient(Application.WAITER_LISTENING_PORT, "MF" + Integer.toString(i)
                    + new Random().nextInt(10)
                    + new Random().nextInt(10));
            client.start();

//            System.out.println(String.format("put(\"%s\", \"tester_%d\");",
//                    client.getKey(), client.getId()));
        }
    }

    static void gameTest() throws GameException {
        Game myGame = new QuoridorGame();
        Player player3 = new QuoridorPlayer();

        myGame.addPlayer(player3);

        QuoridorGame game = new QuoridorGame();
        QuoridorPlayer player1 = new QuoridorPlayer();
        QuoridorPlayer player2 = new QuoridorPlayer();

        player1.setName("bot1");
        player2.setName("bot2");

        game.addPlayer(player2, 0);
        game.addPlayer(player1, 1);


        game.asPlayer(player2).move(5,2);
        game.asPlayer(player1).move(4,1,6,1);

        game.asPlayer(player2).move(new ActorMovement(4, 2));
        game.asPlayer(player1).move(0, 1, 2, 1);

        game.asPlayer(player2).move(3,1, 3,4);
        game.asPlayer(player1).move(0,5,0,7);

        game.asPlayer(player2).move(new ActorMovement(5, 4));
        game.asPlayer(player1).move(3,2,4,2);

        game.asPlayer(player2).move(new ActorMovement(4, 3));
        game.asPlayer(player1).move(new ActorMovement(5, 6));

        game.asPlayer(player2).move(new ActorMovement(3, 2));
        game.asPlayer(player1).move(4,1,4,3);

        game.asPlayer(player2).move(new ActorMovement(5,2));
        game.asPlayer(player1).move(new BlockMovement(4,5, 4, 8));

        game.asPlayer(player2).move(new ActorMovement(4,1));
        game.asPlayer(player1).move(new BlockMovement(4,6, 4, 8));

        game.asPlayer(player2).move(new ActorMovement(3, 1));
        game.asPlayer(player1).move(new ActorMovement(5, 4));

        game.asPlayer(player2).move(new ActorMovement(3, 2));
        game.asPlayer(player1).move(new ActorMovement(5, 3));

        game.asPlayer(player2).move(new ActorMovement(3, 3));
        game.asPlayer(player1).move(new ActorMovement(5, 2));

        game.asPlayer(player2).move(new ActorMovement(8, 8));
        game.asPlayer(player1).move(new ActorMovement(5, 1));

        game.getStepLog().toCsv("C:\\Users\\jiang\\Documents\\WeChat Files\\jycomputerIT\\FileStorage\\File\\2020-04\\QuoridorUI_Bin\\log_xxxxxxxx_xxxxxx.csv");

        System.out.println(game.getStepLog());

    }

    private static void extractCmdArgs(String[] args) {
        Options options = new Options();

        Option port = new Option("p", "port", true, "server listening port");
        port.setRequired(true);
        options.addOption(port);

        Option playersNum = new Option("n", "num", true,
                "total number of players in a contest. default: 2");
        playersNum.setRequired(false);
        options.addOption(playersNum);

        Option debug = new Option("d", "debug", false,
                "start server in debug mode, " +
                        "if set, the verbose log will be printed in stdout.");
        debug.setRequired(false);
        options.addOption(debug);

        Option roundsPerGame = new Option("r", "rounds", true,
                "rounds per game, this indicates how many rounds " +
                        "to run for every pair of players to decide which player is the winner. default: 1");
        playersNum.setRequired(false);
        options.addOption(roundsPerGame);

        Option maxStepTime = new Option("t", "timeout", true,
                "step timeout in milliseconds, if step request from one client is waited more than " +
                        "this timeout, the client will receive a TimeOut state code. default: 1000");
        maxStepTime.setRequired(false);
        options.addOption(maxStepTime);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("quodidor server", options);

            System.exit(1);
        }

        String portArg = cmd.getOptionValue("port");
        String numArg = cmd.getOptionValue("num");
        String roundsArg = cmd.getOptionValue("rounds");
        String timeoutArg = cmd.getOptionValue("timeout");

        if (numArg != null) {
            Application.PLAYERS_IN_THIS_CONTEST = Integer.parseInt(numArg);
        }
        if (roundsArg != null) {
            Application.ROUNDS_PER_GAME = Integer.parseInt(roundsArg);
        }

        Application.WAITER_LISTENING_PORT = Integer.parseInt(portArg);

        if (timeoutArg != null) {
            Application.TIMEOUT_MILLISECONDS = Integer.parseInt(timeoutArg);
        }

        if (cmd.hasOption('d')) {
            Application.DEBUG = true;
        }
    }
}
