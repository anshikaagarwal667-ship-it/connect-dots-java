package connectdots;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Immutable record of a single move, used for history tracking and undo.
 */
public class MoveRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int moveNumber;
    private final Player player;
    private final Line line;
    private final List<Box> boxesClaimed; // boxes completed by this move
    private final boolean extraTurn;       // did this move grant an extra turn?

    public MoveRecord(int moveNumber, Player player, Line line,
                      List<Box> boxesClaimed) {
        this.moveNumber   = moveNumber;
        this.player       = player;
        this.line         = line;
        this.boxesClaimed = new ArrayList<>(boxesClaimed);
        this.extraTurn    = !boxesClaimed.isEmpty();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getMoveNumber()        { return moveNumber; }
    public Player getPlayer()         { return player; }
    public Line getLine()             { return line; }
    public List<Box> getBoxesClaimed() { return boxesClaimed; }
    public boolean wasExtraTurn()      { return extraTurn; }

    @Override
    public String toString() {
        String side = line.getOrientation() == Line.Orientation.HORIZONTAL ? "H" : "V";
        String base = "#" + moveNumber + " " + player.getName()
                + " → " + side + "[" + line.getRow() + "," + line.getCol() + "]";
        if (!boxesClaimed.isEmpty()) {
            base += " +" + boxesClaimed.size() + " box(es)";
        }
        return base;
    }
}
