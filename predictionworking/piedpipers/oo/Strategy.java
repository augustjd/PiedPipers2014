package piedpipers.oo;

import java.util.*;
import java.util.concurrent.*;

import piedpipers.sim.*;

public interface Strategy {
    public Vector getMove(Player p, Scene s);
    public String getName();
}
