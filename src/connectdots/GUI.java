package connectdots;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Main application window (JFrame).
 * Composes all sub-panels and wires event handling between UI and GameLogic.
 */
public class GUI extends JFrame implements GameLogic.GameEventListener {

    // ── Colours & theme ───────────────────────────────────────────────────────
    private static final Color BG_MAIN = new Color(22, 27, 45);
    private static final Color ACCENT  = new Color(52, 152, 219);

    // ── Components ────────────────────────────────────────────────────────────
    private GameLogic    gameLogic;
    private BoardPanel   boardPanel;
    private ScorePanel   scorePanel;
    private HistoryPanel historyPanel;
    private JLabel       statusBar;

    // ── Constructor ───────────────────────────────────────────────────────────

    public GUI() {
        super("Connect Dots");
        gameLogic = new GameLogic(4, "Player 1", "Player 2");
        gameLogic.addListener(this);
        buildUI();
        applyTheme();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(700, 600));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── UI Construction ───────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());

        // Score panel at the top
        scorePanel = new ScorePanel();
        scorePanel.update(gameLogic);
        add(scorePanel, BorderLayout.NORTH);

        // Centre: board
        boardPanel = new BoardPanel(gameLogic);
        boardPanel.setClickListener(this::handleLineClick);
        boardPanel.setBorder(new EmptyBorder(12, 12, 12, 0));
        add(boardPanel, BorderLayout.CENTER);

        // Right sidebar: history + controls
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.EAST);

        // Status bar at the bottom
        statusBar = new JLabel(" Ready – " + gameLogic.getPlayer1().getName() + "'s turn");
        statusBar.setForeground(new Color(160, 180, 210));
        statusBar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(40, 50, 80)),
                new EmptyBorder(6, 14, 6, 14)));
        statusBar.setBackground(new Color(15, 18, 30));
        statusBar.setOpaque(true);
        add(statusBar, BorderLayout.SOUTH);

        buildMenuBar();
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 10));
        sidebar.setBackground(new Color(18, 22, 38));
        sidebar.setBorder(new EmptyBorder(12, 0, 12, 12));
        sidebar.setPreferredSize(new Dimension(210, 0));

        historyPanel = new HistoryPanel();
        sidebar.add(historyPanel, BorderLayout.CENTER);

        JPanel btnPanel = buildButtonPanel();
        sidebar.add(btnPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 8));
        p.setBackground(new Color(18, 22, 38));
        p.setBorder(new EmptyBorder(8, 0, 0, 0));

        p.add(accentButton("New Game",    e -> promptNewGame()));
        p.add(accentButton("Undo Move",   e -> doUndo()));
        p.add(accentButton("Save Game",   e -> doSave()));
        p.add(accentButton("Load Game",   e -> doLoad()));

        return p;
    }

    private JButton accentButton(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.setBackground(new Color(35, 42, 65));
        b.setForeground(new Color(200, 215, 240));
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(0, 38));
        b.addActionListener(al);
        b.addMouseListener(new MouseAdapter() {
            final Color normal = b.getBackground();
            final Color hover  = normal.brighter();
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(normal); }
        });
        return b;
    }

    private void buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(new Color(15, 18, 30));
        mb.setBorder(new MatteBorder(0, 0, 1, 0, new Color(40, 50, 80)));

        JMenu gameMenu = darkMenu("Game");
        gameMenu.add(darkItem("New Game",  e -> promptNewGame()));
        gameMenu.add(darkItem("Undo Move", e -> doUndo()));
        gameMenu.addSeparator();
        gameMenu.add(darkItem("Save Game", e -> doSave()));
        gameMenu.add(darkItem("Load Game", e -> doLoad()));
        gameMenu.addSeparator();
        gameMenu.add(darkItem("Exit",      e -> System.exit(0)));

        JMenu helpMenu = darkMenu("Help");
        helpMenu.add(darkItem("How to Play", e -> showHelp()));
        helpMenu.add(darkItem("About",        e -> showAbout()));

        mb.add(gameMenu);
        mb.add(helpMenu);
        setJMenuBar(mb);
    }

    private JMenu darkMenu(String text) {
        JMenu m = new JMenu(text);
        m.setForeground(new Color(190, 210, 240));
        m.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return m;
    }

    private JMenuItem darkItem(String text, ActionListener al) {
        JMenuItem i = new JMenuItem(text);
        i.setBackground(new Color(25, 30, 50));
        i.setForeground(new Color(200, 215, 240));
        i.setFont(new Font("SansSerif", Font.PLAIN, 13));
        i.addActionListener(al);
        return i;
    }

    private void applyTheme() {
        getContentPane().setBackground(BG_MAIN);
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
    }

    // ── Game actions ──────────────────────────────────────────────────────────

    private void handleLineClick(Line line) {
        boolean ok = gameLogic.makeMove(line);
        if (!ok) {
            setStatus("That line is already drawn!");
        }
    }

    private void doUndo() {
        if (gameLogic.getMoveHistory().isEmpty()) {
            setStatus("No moves to undo.");
            return;
        }
        MoveRecord undone = gameLogic.undoLastMove();
        if (undone != null) {
            setStatus("Undone: " + undone);
            historyPanel.refresh(gameLogic.getMoveHistory());
            scorePanel.update(gameLogic);
            boardPanel.repaint();
        }
    }

    private void promptNewGame() {
        NewGameDialog dlg = new NewGameDialog(this,
                gameLogic.getPlayer1().getName(),
                gameLogic.getPlayer2().getName(),
                gameLogic.getGridSize());
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            gameLogic.removeListener(this);
            gameLogic = new GameLogic(dlg.getGridSize(), dlg.getP1Name(), dlg.getP2Name());
            gameLogic.addListener(this);
            boardPanel.setGameLogic(gameLogic);
            scorePanel.reset(gameLogic);
            historyPanel.clear();
            setStatus("New game started – " + gameLogic.getPlayer1().getName() + "'s turn");
        }
    }

    private void doSave() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Connect Dots Save (*.cds)", "cds"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".cds")) f = new File(f.getAbsolutePath() + ".cds");
            try {
                gameLogic.saveGame(f);
                setStatus("Game saved to " + f.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doLoad() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Connect Dots Save (*.cds)", "cds"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                GameLogic loaded = GameLogic.loadGame(fc.getSelectedFile());
                gameLogic.removeListener(this);
                gameLogic = loaded;
                gameLogic.addListener(this);
                boardPanel.setGameLogic(gameLogic);
                scorePanel.update(gameLogic);
                historyPanel.refresh(gameLogic.getMoveHistory());
                setStatus("Game loaded – " + gameLogic.getCurrentPlayer().getName() + "'s turn");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── GameEventListener ─────────────────────────────────────────────────────

    @Override
    public void onMoveMade(MoveRecord record, boolean extraTurn) {
        scorePanel.update(gameLogic);
        historyPanel.refresh(gameLogic.getMoveHistory());
        boardPanel.repaint();

        Player cur = gameLogic.getCurrentPlayer();
        if (extraTurn && !gameLogic.isGameOver()) {
            setStatus(record.getPlayer().getName() + " claimed " + record.getBoxesClaimed().size()
                    + " box(es) – extra turn!");
        } else if (!gameLogic.isGameOver()) {
            setStatus(cur.getName() + "'s turn");
        }
    }

    @Override
    public void onGameOver(Player winner) {
        scorePanel.update(gameLogic);
        boardPanel.repaint();
        String msg;
        if (winner == null) {
            msg = "It's a draw! Both players tied.";
            setStatus("Game over – Draw!");
        } else {
            msg = winner.getName() + " wins with " + winner.getScore() + " box(es)! 🎉";
            setStatus("Game over – " + winner.getName() + " wins!");
        }
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setStatus(String text) {
        statusBar.setText(" " + text);
    }

    private void showHelp() {
        String help = "<html><b>How to Play Connect Dots</b><br/><br/>"
                + "1. Players take turns clicking between two adjacent dots.<br/>"
                + "2. A line is drawn between those dots.<br/>"
                + "3. If you complete the 4th side of a box, you own it and score a point.<br/>"
                + "4. Completing a box grants you an extra turn.<br/>"
                + "5. The game ends when all boxes are filled.<br/>"
                + "6. The player with the most boxes wins!<br/><br/>"
                + "<b>Tips:</b> Use Undo to take back a move. Save/Load to resume later.</html>";
        JOptionPane.showMessageDialog(this, help, "How to Play", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "<html><b>Connect Dots</b><br/>A Java OOP project demonstrating:<br/>"
                + "• Encapsulation &amp; Abstraction<br/>"
                + "• Composition (Box ↔ Lines)<br/>"
                + "• Event-driven Swing GUI<br/>"
                + "• File serialisation (save/load)<br/>"
                + "• Move history &amp; Undo</html>",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }
}
