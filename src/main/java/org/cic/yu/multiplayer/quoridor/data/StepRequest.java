package org.cic.yu.multiplayer.quoridor.data;

import org.cic.yu.game.quoridor.ActorMovement;
import org.cic.yu.game.quoridor.BlockMovement;
import org.cic.yu.game.quoridor.Point;
import org.cic.yu.game.quoridor.QuoridorMovement;

import java.nio.ByteBuffer;

public class StepRequest extends PlayerRequest {
    public static int BYTE_LENGTH = PlayerRequest.BYTE_LENGTH + 4 * 2 + 4 * 4;

    private Point actorLoc;
    private BlockMovement blockLoc;

    transient private QuoridorMovement movement;

    private StepRequest(String key) {
        super(key);
    }

    private StepRequest(Builder builder) {
        this.actorLoc = builder.actorLoc;
        this.blockLoc = builder.blockLoc;
        this.movement = builder.movement;
        this.key = builder.key;
    }

    public static int identifier() {
        return 0x02;
    }

    public static StepRequest fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int identifier = buffer.getInt();

        assert identifier == identifier(): "Request type error.";

        String key = new String(bytes, buffer.position(), KEY_LENGTH);
        StepRequest stepRequest = new StepRequest(key);
        buffer.position(buffer.position() + KEY_LENGTH);
        QuoridorMovement movement = buildMoveFromByteBuffer(buffer);
        if (movement instanceof ActorMovement) {
            stepRequest.actorLoc = ((ActorMovement) movement).getLoc();
        } else if (movement instanceof BlockMovement) {
            stepRequest.blockLoc = (BlockMovement) movement;
        }
        stepRequest.movement = movement;

        return stepRequest;
    }
    
    public static void dump(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int identifier = buffer.getInt();
        String key = new String(bytes, buffer.position(), KEY_LENGTH);
        buffer.position(buffer.position() + KEY_LENGTH);
        int actorX = buffer.getInt();
        int actorY = buffer.getInt();
        int blockX1 = buffer.getInt();
        int blockY1 = buffer.getInt();
        int blockX2 = buffer.getInt();
        int blockY2 = buffer.getInt();
        
        System.out.println("identifier: " + identifier);
        System.out.println("key: " + key);
        System.out.println("actorX: " + actorX);
        System.out.println("actorY: " + actorY);
        System.out.println("blockX1: " + blockX1);
        System.out.println("blockY1: " + blockY1);
        System.out.println("blockX2: " + blockX2);
        System.out.println("blockY2: " + blockY2);
    }

    private static QuoridorMovement buildMoveFromByteBuffer(ByteBuffer buffer) {
        int actorX = buffer.getInt();
        int actorY = buffer.getInt();
        int blockX1 = buffer.getInt();
        int blockY1 = buffer.getInt();
        int blockX2 = buffer.getInt();
        int blockY2 = buffer.getInt();

        QuoridorMovement movement;
        if (actorX == QuoridorMovement.EMPTY_LOC.x) {
            movement = new BlockMovement(blockX1, blockY1, blockX2, blockY2);
        } else {
            movement = new ActorMovement(actorX, actorY);
        }

        return movement;
    }

    public static Builder newStepRequest() {
        return new Builder();
    }

    public Point getActorLoc() {
        return actorLoc;
    }

    public void setActorLoc(Point actorLoc) {
        this.actorLoc = actorLoc;
    }

    public BlockMovement getBlockLoc() {
        return blockLoc;
    }

    public void setBlockLoc(BlockMovement blockLoc) {
        this.blockLoc = blockLoc;
    }

    public QuoridorMovement getMovement() {
        return movement;
    }


    public static final class Builder {
        private Point actorLoc = QuoridorMovement.EMPTY_LOC;
        private BlockMovement blockLoc = BlockMovement.EMPTY_MOVEMENT;
        private QuoridorMovement movement;
        private String key = "THIS_IS_ONLY_FOR_TESTING";

        private Builder() {
        }

        public StepRequest build() {
            return new StepRequest(this);
        }

        public Builder actorLoc(Point actorLoc) {
            this.actorLoc = actorLoc;
            return this;
        }

        public Builder blockLoc(BlockMovement blockLoc) {
            this.blockLoc = blockLoc;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder movement(QuoridorMovement movement) {
            this.movement = movement;

            if (movement instanceof ActorMovement) {
                this.actorLoc = ((ActorMovement) movement).getLoc();
            }
            if (movement instanceof BlockMovement) {
                this.blockLoc = (BlockMovement) movement;
            }

            return this;
        }
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[BYTE_LENGTH];
        // Wrap a byte array into a buffer
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.putInt(identifier());

        assert key.length() == KEY_LENGTH;

        buf.put(key.getBytes());
        buf.put(actorLoc.toBytes());
        buf.put(blockLoc.toBytes());

        assert buf.position() == BYTE_LENGTH;

        return bytes;
    }
}
