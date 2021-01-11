package org.cic.yu.game.quoridor;


import java.util.Date;

public class Step {
    private Date startTime;
    private QuoridorMovement movement;
    private StateCode stateCode;
    private QuoridorPlayer player;

    private Step(Builder builder) {
        this.startTime = builder.startTime;
        this.movement = builder.movement;
        this.stateCode = builder.stateCode;
        this.player = builder.player;
    }

    public static Builder newStep() {
        return new Builder();
    }


    public static final class Builder {
        private Date startTime;
        private QuoridorMovement movement;
        private StateCode stateCode;
        private QuoridorPlayer player;

        private Builder() {
        }

        public Step build() {
            return new Step(this);
        }

        public Builder startTime(Date startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder movement(QuoridorMovement movement) {
            this.movement = movement;
            return this;
        }

        public Builder stateCode(StateCode stateCode) {
            this.stateCode = stateCode;
            return this;
        }

        public Builder player(QuoridorPlayer player) {
            this.player = player;
            return this;
        }
    }
}
