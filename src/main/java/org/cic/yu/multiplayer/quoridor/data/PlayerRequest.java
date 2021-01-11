package org.cic.yu.multiplayer.quoridor.data;

import java.nio.ByteBuffer;

public class PlayerRequest implements CommunicationFormat {
    public static int BYTE_LENGTH =  TYPE_LENGTH + KEY_LENGTH;

    private static final int IDENTIFIER = 0x01;

    /**
     * key[64]
     */
    protected String key;

    public PlayerRequest() {
    }

    public PlayerRequest(String key) {
        this.key = key;
    }

    public static void dump(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int identifier = buffer.getInt();
        String key = new String(bytes, buffer.position(), KEY_LENGTH);
        System.out.println("identifier: " + identifier);
        System.out.println("key: " + key);
    }

    public static int identifier() {
        return IDENTIFIER;
    }

    public String getKey() {
        return key;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[BYTE_LENGTH];
        // Wrap a byte array into a buffer
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.putInt(identifier());
        buf.put(key.getBytes());

        assert key.length() == KEY_LENGTH;

        return bytes;
    }
}
