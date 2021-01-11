package org.cic.yu.multiplayer.quoridor.data;

import org.cic.yu.game.quoridor.*;

import java.nio.ByteBuffer;

import static org.cic.yu.game.quoridor.QuoridorMovement.EMPTY_LOC;

public class ChangeResponse implements CommunicationFormat {
    private ChangeResponse(Builder builder) {
        this.state = builder.state;
        this.stateCode = builder.stateCode;
        this.actorLoc = builder.actorLoc;
        this.theOtherActorNewLoc = builder.theOtherActorNewLoc;
        this.theOtherPlayerNewBlockMove = builder.theOtherPlayerNewBlockMove;
    }

    public static int identifier() {
        return 1002;
    }

    private AuthResponse.State state;
    private StateCode stateCode;
    private Point actorLoc;
    private Point theOtherActorNewLoc;
    private BlockMovement theOtherPlayerNewBlockMove;

    public static Builder newChangeResponse() {
        return new Builder();
    }

    public static ChangeResponse buildFromOpponentMove(QuoridorMovement move, Point opponentLoc,
                                                       StateCode stateCode) {
        Builder builder = newChangeResponse();

        builder.actorLoc = EMPTY_LOC;
        builder.stateCode = stateCode;
        builder.state = AuthResponse.State.OK;

        if (move instanceof BlockMovement) {
            builder.theOtherActorNewLoc = opponentLoc;
            builder.theOtherPlayerNewBlockMove = (BlockMovement) move;
        } else if(move instanceof ActorMovement) {
            builder.theOtherActorNewLoc = ((ActorMovement) move).getLoc();
            builder.theOtherPlayerNewBlockMove = BlockMovement.EMPTY_MOVEMENT;
        }
        return builder.build();
    }

    public static ChangeResponse buildWinOrLoseResponse(StateCode stateCode) {
        return ChangeResponse.newChangeResponse()
                .state(AuthResponse.State.OK)
                .stateCode(stateCode)
                .build();
    }

    public AuthResponse.State getState() {
        return state;
    }

    public void setState(AuthResponse.State state) {
        this.state = state;
    }

    public StateCode getStateCode() {
        return stateCode;
    }

    public void setStateCode(StateCode stateCode) {
        this.stateCode = stateCode;
    }

    public Point getActorLoc() {
        return actorLoc;
    }

    public void setActorLoc(Point actorLoc) {
        this.actorLoc = actorLoc;
    }

    public Point getTheOtherActorNewLoc() {
        return theOtherActorNewLoc;
    }

    public void setTheOtherActorNewLoc(Point theOtherActorNewLoc) {
        this.theOtherActorNewLoc = theOtherActorNewLoc;
    }

    public BlockMovement getTheOtherPlayerNewBlockMove() {
        return theOtherPlayerNewBlockMove;
    }

    public void setTheOtherPlayerNewBlockMove(BlockMovement theOtherPlayerNewBlockMove) {
        this.theOtherPlayerNewBlockMove = theOtherPlayerNewBlockMove;
    }


    public byte[] toBytes() {
        byte[] bytes = new byte[4 + 4 + 4 + 8 + 8 + 16];

        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.putInt(identifier())
                .putInt(state.getVal())
                .putInt(stateCode.getValue())
                .put(actorLoc.toBytes())
                .put(theOtherActorNewLoc.toBytes());
        theOtherPlayerNewBlockMove.putBytes(buf);

        return bytes;
    }

    public static final class Builder {
        private AuthResponse.State state = AuthResponse.State.EMPTY;
        private StateCode stateCode = StateCode.EMPTY;
        private Point actorLoc = EMPTY_LOC;
        private Point theOtherActorNewLoc = EMPTY_LOC;
        private BlockMovement theOtherPlayerNewBlockMove = BlockMovement.EMPTY_MOVEMENT;

        private Builder() {
        }

        public ChangeResponse build() {
            return new ChangeResponse(this);
        }

        public Builder state(AuthResponse.State state) {
            this.state = state;
            return this;
        }

        public Builder stateCode(StateCode stateCode) {
            this.stateCode = stateCode;
            return this;
        }

        public Builder actorLoc(Point actorLoc) {
            this.actorLoc = actorLoc;
            return this;
        }

        public Builder theOtherActorNewLoc(Point theOtherActorNewLoc) {
            this.theOtherActorNewLoc = theOtherActorNewLoc;
            return this;
        }

        public Builder theOtherPlayerNewBlockLoc(BlockMovement theOtherPlayerNewBlockLoc) {
            this.theOtherPlayerNewBlockMove = theOtherPlayerNewBlockLoc;
            return this;
        }
    }

    @Override
    public String toString() {
        return "ChangeResponse{" +
                "state=" + state +
                ", stateCode=" + stateCode +
                ", actorLoc=" + actorLoc +
                ", theOtherActorNewLoc=" + theOtherActorNewLoc +
                ", theOtherPlayerNewBlockMove=" + theOtherPlayerNewBlockMove +
                '}';
    }
}
