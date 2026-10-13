package connectdots;

import java.io.Serializable;

/**
 * Manages the full board state: dots, lines, and boxes.
 *
 * For a grid of size N (N dots per side):
 *   Dots:        N × N
 *   H-lines:     N × (N-1)   horizontalLines[row][col]
 *   V-lines:    (N-1) × N    verticalLines[row][col]
 *   Boxes:      (N-1) × (N-1)
 */
public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int gridSize;          // number of dots per side
    private final int boxCount;          // (gridSize-1)^2

    private final Line[][] horizontalLines; // [row 0..N-1][col 0..N-2]
    private final Line[][] verticalLines;   // [row 0..N-2][col 0..N-1]
    private final Box[][]  boxes;           // [row 0..N-2][col 0..N-2]

    private int totalLinesDrawn;

    public Board(int gridSize) {
        if (gridSize < 2) throw new IllegalArgumentException("Grid size must be >= 2");
        this.gridSize        = gridSize;
        int n                = gridSize;
        this.boxCount        = (n - 1) * (n - 1);
        this.totalLinesDrawn = 0;

        // Initialise lines
        horizontalLines = new Line[n][n - 1];
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n - 1; c++)
                horizontalLines[r][c] = new Line(Line.Orientation.HORIZONTAL, r, c);

        verticalLines = new Line[n - 1][n];
        for (int r = 0; r < n - 1; r++)
            for (int c = 0; c < n; c++)
                verticalLines[r][c] = new Line(Line.Orientation.VERTICAL, r, c);

        // Initialise boxes with their four boundary lines
        boxes = new Box[n - 1][n - 1];
        for (int r = 0; r < n - 1; r++) {
            for (int c = 0; c < n - 1; c++) {
                boxes[r][c] = new Box(
                        r, c,
                        horizontalLines[r][c],      // top
                        horizontalLines[r + 1][c],  // bottom
                        verticalLines[r][c],         // left
                        verticalLines[r][c + 1]      // right
                );
            }
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public int getGridSize()   { return gridSize; }
    public int getBoxCount()   { return boxCount; }
    public int getTotalLinesDrawn() { return totalLinesDrawn; }

    public Line getHorizontalLine(int row, int col) {
        return horizontalLines[row][col];
    }

    public Line getVerticalLine(int row, int col) {
        return verticalLines[row][col];
    }

    public Box getBox(int row, int col) {
        return boxes[row][col];
    }

    public Line[][] getHorizontalLines() { return horizontalLines; }
    public Line[][] getVerticalLines()   { return verticalLines; }
    public Box[][]  getBoxes()           { return boxes; }

    // ── Line drawing ─────────────────────────────────────────────────────────

    /**
     * Attempts to draw the specified line for the given player.
     * @return true if the line was newly drawn, false if already drawn.
     */
    public boolean drawLine(Line line, Player player) {
        boolean success = line.draw(player);
        if (success) totalLinesDrawn++;
        return success;
    }

    /**
     * Checks all boxes adjacent to the supplied line and claims any
     * that have just been completed.
     * @return number of boxes newly claimed by this move
     */
    public java.util.List<Box> claimCompletedBoxes(Line line, Player player) {
        java.util.List<Box> claimed = new java.util.ArrayList<>();
        int r = line.getRow();
        int c = line.getCol();
        int n = gridSize;

        if (line.getOrientation() == Line.Orientation.HORIZONTAL) {
            // top side of box [r][c]  →  box row = r,   col = c
            // bot side of box [r-1][c] → box row = r-1, col = c
            if (r > 0     && c < n-1 && boxes[r-1][c].checkAndClaim(player)) claimed.add(boxes[r-1][c]);
            if (r < n-1  && c < n-1 && boxes[r][c].checkAndClaim(player))   claimed.add(boxes[r][c]);
        } else {
            // left side of box [r][c]   → box row=r, col=c
            // right side of box [r][c-1]→ box row=r, col=c-1
            if (c > 0     && r < n-1 && boxes[r][c-1].checkAndClaim(player)) claimed.add(boxes[r][c-1]);
            if (c < n-1  && r < n-1 && boxes[r][c].checkAndClaim(player))   claimed.add(boxes[r][c]);
        }
        return claimed;
    }

    // ── Undo support ─────────────────────────────────────────────────────────

    /**
     * Undoes a drawn line and un-claims any boxes that were closed by it.
     */
    public void undoLine(Line line, java.util.List<Box> boxesToUnclaim) {
        if (line.isDrawn()) {
            line.reset();
            totalLinesDrawn--;
        }
        for (Box box : boxesToUnclaim) {
            box.reset();
        }
    }

    // ── State queries ────────────────────────────────────────────────────────

    public boolean isGameOver() {
        for (int r = 0; r < gridSize - 1; r++)
            for (int c = 0; c < gridSize - 1; c++)
                if (!boxes[r][c].isOwned()) return false;
        return true;
    }

    public int countBoxesOwnedBy(Player player) {
        int count = 0;
        for (int r = 0; r < gridSize - 1; r++)
            for (int c = 0; c < gridSize - 1; c++)
                if (player.equals(boxes[r][c].getOwner())) count++;
        return count;
    }

    // ── Reset ────────────────────────────────────────────────────────────────

    public void reset() {
        totalLinesDrawn = 0;
        for (Line[] row : horizontalLines) for (Line l : row) l.reset();
        for (Line[] row : verticalLines)   for (Line l : row) l.reset();
        for (Box[]  row : boxes)           for (Box  b : row) b.reset();
    }

    /** Finds the nearest un-drawn line to a pixel coordinate on the panel. */
    public Line findNearestLine(int pixelX, int pixelY,
                                int cellSize, int dotRadius, int margin) {
        int n = gridSize;
        Line best = null;
        double bestDist = cellSize * 0.4; // click must be within 40% of a cell

        // Check horizontal lines
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n - 1; c++) {
                if (horizontalLines[r][c].isDrawn()) continue;
                int lx = margin + c * cellSize + cellSize / 2;
                int ly = margin + r * cellSize;
                double dist = Math.hypot(pixelX - lx, pixelY - ly);
                if (dist < bestDist) { bestDist = dist; best = horizontalLines[r][c]; }
            }
        }

        // Check vertical lines
        for (int r = 0; r < n - 1; r++) {
            for (int c = 0; c < n; c++) {
                if (verticalLines[r][c].isDrawn()) continue;
                int lx = margin + c * cellSize;
                int ly = margin + r * cellSize + cellSize / 2;
                double dist = Math.hypot(pixelX - lx, pixelY - ly);
                if (dist < bestDist) { bestDist = dist; best = verticalLines[r][c]; }
            }
        }

        return best;
    }
}
