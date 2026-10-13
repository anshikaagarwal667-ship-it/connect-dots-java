package connectdots;

import java.io.Serializable;

/**
 * Represents a player in the Connect Dots game.
 * Encapsulates player name, score, and color assignment.
 */
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private int score;
    private final java.awt.Color color;
    private final int id; // 1 or 2

    public Player(int id, String name, java.awt.Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.score = 0;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getId()          { return id; }
    public String getName()     { return name; }
    public int getScore()       { return score; }
    public java.awt.Color getColor() { return color; }

    // ── Score management ─────────────────────────────────────────────────────

    public void addScore(int points) {
        if (points > 0) score += points;
    }

    public void resetScore() {
        score = 0;
    }

    @Override
    public String toString() {
        return name + " (Score: " + score + ")";
    }
}
