package org.cic.yu.multiplayer.quoridor.data;

import java.nio.ByteBuffer;

/**
 * Type: AA 1001
 */
public class AuthResponse implements CommunicationFormat {
    public static int identifier() {
        return 1001;
    }
    private static final int BYTE_LENGTH = 8;

    private AuthResponse() {

    }

    public enum State {
        EMPTY(56, "Placeholder state."),
        OK(0, "OK"),
        KEY_ERROR(1, "The key is invalid."),
        CLOSE(-1, "The connection will be closed in a moment.");

        private int val;
        private String msg;

        State(int val, String msg) {
            this.val = val;
            this.msg = msg;
        }

        public int getVal() {
            return val;
        }

        public String getMsg() {
            return msg;
        }

        @Override
        public String toString() {
            return "State{" +
                    "val=" + val +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }

    public static AuthResponse build(boolean isSuccess) {
        AuthResponse response = new AuthResponse();
        if(isSuccess) {
            response.state = State.OK;
        } else {
            response.state = State.KEY_ERROR;
        }

        return response;
    }

    public static AuthResponse buildCloseConnectionResponse() {
        AuthResponse response = new AuthResponse();
        response.state = State.CLOSE;

        return response;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[BYTE_LENGTH];

        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.putInt(identifier());
        buf.putInt(state.val);

        return bytes;
    }

    /**
     * state[4]
     */
    private State state;

    public State getState() {
        return state;
    }
}
