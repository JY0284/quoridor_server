package org.cic.yu.multiplayer.quoridor.data;

public interface CommunicationFormat {
    int KEY_LENGTH = 64;
    int TYPE_LENGTH = 4;
    int AUTH_STATE_LENGTH = 4;
    int GAME_STATE_LENGTH = 4;
}
