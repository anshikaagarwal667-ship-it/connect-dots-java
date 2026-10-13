package connectdots;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * JPanel responsible for rendering the game board and accepting mouse input.
 * Uses Graphics2D for antialiased drawing.
 */
public class BoardPanel extends JPanel {

    // ── Layout constants ─────────────────────────────────────────────────────
    private static final int MARGIN     = 40;
    private static final int DOT_RADIUS = 8;

    // Colours
    private static final Color BG_COLOR      = new Color(22, 27, 45);
    private static final Color DOT_COLOR     = new Color(236, 240, 241);
    private static final Color HOVER_COLOR   = new Color(255, 255, 255, 180);
    private static final Color UNDRAWN_COLOR = new Color(60, 70, 100);

    // ── State ─────────────────────────────────────────────────────────────────
    private GameLogic gameLogic;
    private Line      hoveredLine;   // line under the mouse cursor
    private MoveClickListener clickListener;

    // ── Constructor ───────────────────────────────────────────────────────────

    public BoardPanel(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(560, 560));
        wireMouseListeners();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public interface MoveClickListener {
        void onLineClicked(Line line);
    }

    public void setClickListener(MoveClickListener l)  { this.clickListener = l; }
    public void setGameLogic(GameLogic gl)             { this.gameLogic = gl; repaint(); }

    // ── Mouse handling ────────────────────────────────────────────────────────

    private void wireMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                Line prev = hoveredLine;
                hoveredLine = findLine(e.getX(), e.getY());
                if (hoveredLine != prev) repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                Line line = findLine(e.getX(), e.getY());
                if (line != null && clickListener != null && !gameLogic.isGameOver()) {
                    clickListener.onLineClicked(line);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                hoveredLine = null; repaint();
            }
        });
    }

    private int cellSize() {
        int usable = Math.min(getWidth(), getHeight()) - 2 * MARGIN;
        return usable / (gameLogic.getGridSize() - 1);
    }

    private Line findLine(int px, int py) {
        if (gameLogic == null) return null;
        return gameLogic.getBoard().findNearestLine(px, py, cellSize(), DOT_RADIUS, MARGIN);
    }

    // ── Painting ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameLogic == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Board  board = gameLogic.getBoard();
        int    n     = board.getGridSize();
        int    cs    = cellSize();

        drawBoxBackgrounds(g2, board, n, cs);
        drawUndrawnLines(g2, board, n, cs);
        drawDrawnLines(g2, board, n, cs);
        drawHoverLine(g2, n, cs);
        drawBoxLabels(g2, board, n, cs);
        drawDots(g2, n, cs);

        g2.dispose();
    }

    private int dotX(int col, int cs) { return MARGIN + col * cs; }
    private int dotY(int row, int cs) { return MARGIN + row * cs; }

    // Filled box backgrounds
    private void drawBoxBackgrounds(Graphics2D g2, Board board, int n, int cs) {
        for (int r = 0; r < n - 1; r++) {
            for (int c = 0; c < n - 1; c++) {
                Box box = board.getBox(r, c);
                if (box.isOwned()) {
                    Color base = box.getOwner().getColor();
                    g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 70));
                    g2.fillRect(dotX(c, cs) + 1, dotY(r, cs) + 1, cs - 2, cs - 2);
                }
            }
        }
    }

    // Faint placeholders for undrawn lines
    private void drawUndrawnLines(Graphics2D g2, Board board, int n, int cs) {
        g2.setColor(UNDRAWN_COLOR);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1, new float[]{6, 6}, 0));
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n - 1; c++) {
                if (!board.getHorizontalLine(r, c).isDrawn())
                    g2.drawLine(dotX(c, cs), dotY(r, cs), dotX(c + 1, cs), dotY(r, cs));
            }
        for (int r = 0; r < n - 1; r++)
            for (int c = 0; c < n; c++) {
                if (!board.getVerticalLine(r, c).isDrawn())
                    g2.drawLine(dotX(c, cs), dotY(r, cs), dotX(c, cs), dotY(r + 1, cs));
            }
    }

    // Drawn lines with player colour
    private void drawDrawnLines(Graphics2D g2, Board board, int n, int cs) {
        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n - 1; c++) {
                Line l = board.getHorizontalLine(r, c);
                if (l.isDrawn()) {
                    g2.setColor(l.getOwner().getColor());
                    g2.drawLine(dotX(c, cs), dotY(r, cs), dotX(c + 1, cs), dotY(r, cs));
                }
            }
        for (int r = 0; r < n - 1; r++)
            for (int c = 0; c < n; c++) {
                Line l = board.getVerticalLine(r, c);
                if (l.isDrawn()) {
                    g2.setColor(l.getOwner().getColor());
                    g2.drawLine(dotX(c, cs), dotY(r, cs), dotX(c, cs), dotY(r + 1, cs));
                }
            }
    }

    // Hover highlight
    private void drawHoverLine(Graphics2D g2, int n, int cs) {
        if (hoveredLine == null || gameLogic.isGameOver()) return;
        g2.setColor(HOVER_COLOR);
        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int r = hoveredLine.getRow();
        int c = hoveredLine.getCol();
        if (hoveredLine.getOrientation() == Line.Orientation.HORIZONTAL) {
            g2.drawLine(dotX(c, cs), dotY(r, cs), dotX(c + 1, cs), dotY(r, cs));
        } else {
            g2.drawLine(dotX(c, cs), dotY(r, cs), dotX(c, cs), dotY(r + 1, cs));
        }
    }

    // Box ownership initials
    private void drawBoxLabels(Graphics2D g2, Board board, int n, int cs) {
        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, cs / 3)));
        FontMetrics fm = g2.getFontMetrics();
        for (int r = 0; r < n - 1; r++) {
            for (int c = 0; c < n - 1; c++) {
                Box box = board.getBox(r, c);
                if (box.isOwned()) {
                    String initial = String.valueOf(box.getOwner().getName().charAt(0)).toUpperCase();
                    int tx = dotX(c, cs) + cs / 2 - fm.stringWidth(initial) / 2;
                    int ty = dotY(r, cs) + cs / 2 + fm.getAscent() / 2 - 2;
                    g2.setColor(box.getOwner().getColor().brighter());
                    g2.drawString(initial, tx, ty);
                }
            }
        }
    }

    // Dots on top of everything
    private void drawDots(Graphics2D g2, int n, int cs) {
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                int cx = dotX(c, cs);
                int cy = dotY(r, cs);
                // glow
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(cx - DOT_RADIUS - 3, cy - DOT_RADIUS - 3,
                        (DOT_RADIUS + 3) * 2, (DOT_RADIUS + 3) * 2);
                // dot
                g2.setColor(DOT_COLOR);
                g2.fillOval(cx - DOT_RADIUS, cy - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);
            }
        }
    }
}
