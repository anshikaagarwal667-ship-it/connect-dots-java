package connectdots;

import java.io.Serializable;

/**
 * Represents a single box on the board.
 * A box is bounded by 4 lines:
 *   top    → horizontal[row][col]
 *   bottom → horizontal[row+1][col]
 *   left   → vertical[row][col]
 *   right  → vertical[row][col+1]
 *
 * Demonstrates OOP Composition: Box contains (references) Lines.
 */
public class Box implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int row;
    private final int col;

    // The four boundary lines of this box (composition)
    private final Line top;
    private final Line bottom;
    private final Line left;
    private final Line right;

    private Player owner; // null until all 4 sides are drawn

    public Box(int row, int col, Line top, Line bottom, Line left, Line right) {
        this.row    = row;
        this.col    = col;
        this.top    = top;
        this.bottom = bottom;
        this.left   = left;
        this.right  = right;
        this.owner  = null;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getRow()     { return row; }
    public int getCol()     { return col; }
    public Player getOwner() { return owner; }
    public boolean isOwned() { return owner != null; }

    public Line getTop()    { return top; }
    public Line getBottom() { return bottom; }
    public Line getLeft()   { return left; }
    public Line getRight()  { return right; }

    // ── Completion check ─────────────────────────────────────────────────────

    /**
     * Checks whether all four sides are drawn.
     * If so and no owner is assigned yet, assigns the given player as owner.
     * @return true if this call completed the box (newly completed)
     */
    public boolean checkAndClaim(Player player) {
        if (!isOwned() && isComplete()) {
            owner = player;
            return true;
        }
        return false;
    }

    public boolean isComplete() {
        return top.isDrawn() && bottom.isDrawn() && left.isDrawn() && right.isDrawn();
    }

    /** How many sides are currently drawn (useful for AI / hints). */
    public int sidesDrawn() {
        int count = 0;
        if (top.isDrawn())    count++;
        if (bottom.isDrawn()) count++;
        if (left.isDrawn())   count++;
        if (right.isDrawn())  count++;
        return count;
    }

    public void reset() {
        owner = null;
    }

    @Override
    public String toString() {
        return "Box[" + row + "," + col + "] owner=" + (owner != null ? owner.getName() : "none");
    }
}
