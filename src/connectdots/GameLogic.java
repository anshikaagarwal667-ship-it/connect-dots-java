package connectdots;

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Controls all game-play logic: turns, scoring, move history, undo, and
 * save/load.  Separated from UI (abstraction principle).
 */
public class GameLogic implements Serializable {
    private static final long serialVersionUID = 1L;

    // ── State ─────────────────────────────────────────────────────────────────

    private Board   board;
    private Player  player1;
    private Player  player2;
    private Player  currentPlayer;
    private boolean gameOver;
    private int     moveCounter;
    private int     gridSize;

    private final Deque<MoveRecord> moveHistory = new ArrayDeque<>();
    private final List<GameEventListener> listeners = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    public GameLogic(int gridSize, String p1Name, String p2Name) {
        this.gridSize = gridSize;
        initGame(gridSize, p1Name, p2Name);
    }

    private void initGame(int size, String p1Name, String p2Name) {
        board         = new Board(size);
        player1       = new Player(1, p1Name, new java.awt.Color(52, 152, 219));   // blue
        player2       = new Player(2, p2Name, new java.awt.Color(231, 76, 60));    // red
        currentPlayer = player1;
        gameOver      = false;
        moveCounter   = 0;
        moveHistory.clear();
    }

    // ── Event listener pattern ────────────────────────────────────────────────

    public interface GameEventListener {
        void onMoveMade(MoveRecord record, boolean extraTurn);
        void onGameOver(Player winner); // null = draw
    }

    public void addListener(GameEventListener l)    { listeners.add(l); }
    public void removeListener(GameEventListener l) { listeners.remove(l); }

    private void fireMoveMade(MoveRecord r, boolean extra) {
        for (GameEventListener l : listeners) l.onMoveMade(r, extra);
    }
    private void fireGameOver(Player winner) {
        for (GameEventListener l : listeners) l.onGameOver(winner);
    }

    // ── Gameplay ──────────────────────────────────────────────────────────────

    /**
     * Attempts to draw a line for the current player.
     * @param line the line to draw
     * @return true if the move was valid and applied
     */
    public boolean makeMove(Line line) {
        if (gameOver || line == null || line.isDrawn()) return false;

        board.drawLine(line, currentPlayer);
        List<Box> claimed = board.claimCompletedBoxes(line, currentPlayer);

        // Award points
        if (!claimed.isEmpty()) {
            currentPlayer.addScore(claimed.size());
        }

        moveCounter++;
        MoveRecord record = new MoveRecord(moveCounter, currentPlayer, line, claimed);
        moveHistory.push(record);

        boolean extraTurn = !claimed.isEmpty();
        fireMoveMade(record, extraTurn);

        // Check game-over
        if (board.isGameOver()) {
            gameOver = true;
            Player winner = determineWinner();
            fireGameOver(winner);
            return true;
        }

        // Switch player only if no box was completed
        if (!extraTurn) {
            currentPlayer = (currentPlayer == player1) ? player2 : player1;
        }

        return true;
    }

    /**
     * Undoes the last move.
     * @return the undone MoveRecord, or null if history is empty
     */
    public MoveRecord undoLastMove() {
        if (moveHistory.isEmpty() || gameOver) return null;

        MoveRecord last = moveHistory.pop();
        Line line       = last.getLine();
        List<Box> boxes = last.getBoxesClaimed();

        // Subtract score
        if (!boxes.isEmpty()) {
            // We need to find whose score to reduce - it was the player who made the move
            Player mover = last.getPlayer();
            // Directly manipulate via reflection would break encapsulation; 
            // instead Player exposes an internal package-visible method via reset
            // We work around this by tracking score deductions here:
            subtractScore(mover, boxes.size());
        }

        board.undoLine(line, boxes);
        moveCounter--;

        // Restore current player: if last move was by player1, restore to player1
        // (who may have had an extra turn, or we switch back)
        // Simpler: after undo, currentPlayer = the player who made the undone move
        currentPlayer = last.getPlayer();

        gameOver = false;
        return last;
    }

    /** Package-visible score subtraction helper (avoids reflection). */
    private void subtractScore(Player p, int amount) {
        // Player.addScore prevents negative additions; we use a workaround:
        // Recalculate from board state
        p.resetScore();
        p.addScore(board.countBoxesOwnedBy(p));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public Board   getBoard()          { return board; }
    public Player  getPlayer1()        { return player1; }
    public Player  getPlayer2()        { return player2; }
    public Player  getCurrentPlayer()  { return currentPlayer; }
    public boolean isGameOver()        { return gameOver; }
    public int     getMoveCounter()    { return moveCounter; }
    public Deque<MoveRecord> getMoveHistory() { return moveHistory; }
    public int     getGridSize()       { return gridSize; }

    public Player determineWinner() {
        int s1 = player1.getScore();
        int s2 = player2.getScore();
        if (s1 > s2) return player1;
        if (s2 > s1) return player2;
        return null; // draw
    }

    // ── Reset / resize ────────────────────────────────────────────────────────

    public void reset() {
        initGame(gridSize, player1.getName(), player2.getName());
    }

    public void changeGridSize(int newSize) {
        this.gridSize = newSize;
        initGame(newSize, player1.getName(), player2.getName());
    }

    // ── Save / Load ───────────────────────────────────────────────────────────

    public void saveGame(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        }
    }

    public static GameLogic loadGame(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameLogic) ois.readObject();
        }
    }
}
