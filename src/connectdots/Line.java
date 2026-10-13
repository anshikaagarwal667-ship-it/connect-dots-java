package connectdots;

import java.io.Serializable;

/**
 * Represents a line (edge) between two adjacent dots.
 * Lines can be HORIZONTAL (between dots in the same row) or VERTICAL.
 *
 * For a grid of N×N dots:
 *   Horizontal lines: row  0..N-1,  col 0..N-2   → indices [row][col]
 *   Vertical   lines: row  0..N-2,  col 0..N-1   → indices [row][col]
 */
public class Line implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Orientation { HORIZONTAL, VERTICAL }

    private final Orientation orientation;
    private final int row;
    private final int col;
    private boolean drawn;
    private Player owner; // the player who drew this line

    public Line(Orientation orientation, int row, int col) {
        this.orientation = orientation;
        this.row  = row;
        this.col  = col;
        this.drawn = false;
        this.owner = null;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Orientation getOrientation() { return orientation; }
    public int getRow()                  { return row; }
    public int getCol()                  { return col; }
    public boolean isDrawn()             { return drawn; }
    public Player getOwner()             { return owner; }

    // ── Draw ─────────────────────────────────────────────────────────────────

    /**
     * Marks this line as drawn by the given player.
     * @return false if already drawn (duplicate move)
     */
    public boolean draw(Player player) {
        if (drawn) return false;
        this.drawn = true;
        this.owner = player;
        return true;
    }

    /** Resets the line to undrawn state (used on board reset or undo). */
    public void reset() {
        drawn = false;
        owner = null;
    }

    @Override
    public String toString() {
        return orientation + "[" + row + "," + col + "] drawn=" + drawn;
    }
}
