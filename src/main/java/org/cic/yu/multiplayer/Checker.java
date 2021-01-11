package org.cic.yu.multiplayer;

import org.cic.yu.game.quoridor.QuoridorPlayer;
import org.cic.yu.multiplayer.quoridor.data.PlayerRequest;

import java.util.HashMap;
import java.util.Map;

public class Checker {
    private final static Checker INSTANCE = new Checker();

    private final static Map<String, String> KEY_NAME_MAP = new HashMap<String, String>() {
        {
            put("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0000\u0000\u0000\u0000", "tester");
            put("0_______________________________________________________________\u0000\u0000\u0000\u0000", "tester_0");
            put("1_______________________________________________________________\u0000\u0000\u0000\u0000", "tester_1");
        }
    };
    private static final Map<String, Integer> REGISTER_KEY_COUNT_MAP = new HashMap<>();

    private Checker() {
    }

    public static Checker getInstance() {
        return INSTANCE;
    }


    public QuoridorPlayer verify(PlayerRequest request) throws MultiPlayerException {
        QuoridorPlayer player = new QuoridorPlayer();
        String key = request.getKey();

        // the following line will not work because of the java string key's length is 64 + 4
//        if (key.indexOf('_') < key.length() && key.lastIndexOf('_') == key.length() - 1) {
        if (key.indexOf('_') < key.length()
                && key.lastIndexOf('_') == PlayerRequest.KEY_LENGTH - 1) {
            int firstSep = key.indexOf('_');
            String name = key.substring(0, firstSep);
            String macAddress = key.substring(firstSep + 1, firstSep + 1 + 12);

            if (macAddress.contains("_")) {
                System.out.println(String.format("[INFO] Please update your client: %s", name));
                throw new MultiPlayerException(
                        String.format("Please update your client to the latest version: %s", name));
            }

            player.setKey(key);
            player.setName(name);
            player.setMacAddress(macAddress);
            if (KEY_NAME_MAP.get(key) == null) {
                KEY_NAME_MAP.put(key, name);
            } else {
                // the key has appeared before: add a suffix to note that name
                if (REGISTER_KEY_COUNT_MAP.get(request.getKey()) != null) {
                    player.setName(
                            KEY_NAME_MAP.get(request.getKey()) +
                                    "_" + REGISTER_KEY_COUNT_MAP.get(request.getKey()));
                    REGISTER_KEY_COUNT_MAP.put(request.getKey()
                            , REGISTER_KEY_COUNT_MAP.get(request.getKey()) + 1);
                } else {
                    player.setName(name + "_1");
                    REGISTER_KEY_COUNT_MAP.put(request.getKey()
                            , 2);
                }
            }
        } else {
            throw new MultiPlayerException("The key is not correct.");
        }

        return player;
    }
}
