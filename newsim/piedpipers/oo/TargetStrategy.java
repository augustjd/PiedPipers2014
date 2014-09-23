package piedpipers.oo;

import java.util.*;
import java.lang.Double;
import java.lang.Math.*;

import piedpipers.sim.*;
import java.awt.Color;

public class TargetStrategy implements Strategy {
    Vector target;
    Vector intermediate_target;
    public boolean choose_next = true;
    static final double EPSILON = 0.1;

    public TargetStrategy() {
        this(new Vector(0.0,0.0));
    }
    public TargetStrategy(Vector target) {
        this.target = target;
        this.intermediate_target = target;
    }

    public static final int GATE_BUFFER = 10;

    public void onReachedTarget(Player p, Scene s) {}

    public Vector getSideOfGate(Scene s, Scene.Side side) {
        if (side == Scene.Side.RIGHT) {
            return s.getGatePosition().add(GATE_BUFFER, 0);
        } else {
            return s.getGatePosition().sub(GATE_BUFFER, 0);
        }
    }

    public Scene.Side getOppositeSide(Scene.Side side) {
        if (side == Scene.Side.RIGHT) {
            return Scene.Side.LEFT;
        } else {
            return Scene.Side.RIGHT;
        }
    }

    boolean crossed_gate = false;
    boolean finished_crossing = false;

    public Vector getGateTarget(Scene.Side player_side, Scene.Side destination_side, Player p, Scene s) {
        Vector position = s.getPiper(p.id);

        if (crossed_gate) {
            if (getSideOfGate(s, destination_side).equals(position)) {
                finished_crossing = true;
            }
            return getSideOfGate(s, destination_side);
        } else {
            if (getSideOfGate(s, player_side).equals(position)) {
                crossed_gate = true;
            }
            return getSideOfGate(s, player_side);
        }
    }

    @Override
    public Vector getMove(Player p, Scene s) {
        Vector position = s.getPiper(p.id);

        if (position.equals(target)) {
            onReachedTarget(p, s);
        }

        Scene.Side player_side      = s.getSide(position);
        Scene.Side destination_side = s.getSide(this.target);

        if (player_side != destination_side && !finished_crossing) {
            this.intermediate_target = getGateTarget(player_side, destination_side, p, s);
        } else {
            this.intermediate_target = this.target;
        }

        Vector difference;
        if (this.intermediate_target == null) {
            this.intermediate_target = this.target;
        }
        try {
            difference = this.intermediate_target.sub(position);
        } catch (NullPointerException e) {
            difference = new Vector(0,0);
        }

        double step_size = Math.min(p.getSpeed() * .99, difference.length());

        if (step_size == 0) {
            if (!this.intermediate_target.equals(this.target)) {
                this.intermediate_target = this.target;
            }
            return position;
        }

        Vector step = difference.unit().scale(step_size);

        return position.add(step);
    }

    private boolean reachedTarget(Player p, Scene s) {
        Vector player_position = s.getPiper(p.id);
        boolean result;
        try {
            result = target.distanceTo(player_position) < EPSILON;
        } catch (NullPointerException e) {
            return true;
        }
        return result;
    }

    public String getName() { return "TargetStrategy"; }
}
