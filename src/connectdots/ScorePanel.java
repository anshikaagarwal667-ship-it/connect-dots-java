package connectdots;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Horizontal panel at the top of the window showing player scores and
 * whose turn it is.
 */
public class ScorePanel extends JPanel {

    private static final Color BG    = new Color(15, 20, 35);
    private static final Color MUTED = new Color(130, 145, 170);

    private final JLabel p1Label   = new JLabel();
    private final JLabel p2Label   = new JLabel();
    private final JLabel turnLabel = new JLabel();
    private final JLabel moveLabel = new JLabel();

    public ScorePanel() {
        setLayout(new GridLayout(1, 4, 8, 0));
        setBackground(BG);
        setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, new Color(40, 50, 80)),
                new EmptyBorder(14, 18, 14, 18)
        ));
        setPreferredSize(new Dimension(0, 76));

        styleLabel(p1Label,   new Color(52, 152, 219));
        styleLabel(p2Label,   new Color(231, 76, 60));
        styleLabel(turnLabel, new Color(241, 196, 15));
        styleLabel(moveLabel, MUTED);

        add(p1Label);
        add(p2Label);
        add(turnLabel);
        add(moveLabel);
    }

    private void styleLabel(JLabel lbl, Color color) {
        lbl.setForeground(color);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void update(GameLogic gl) {
        Player p1 = gl.getPlayer1();
        Player p2 = gl.getPlayer2();
        Player cur = gl.getCurrentPlayer();

        p1Label.setText("<html><center>" + p1.getName() + "<br/>" +
                "<span style='font-size:18pt'>" + p1.getScore() + "</span></center></html>");
        p2Label.setText("<html><center>" + p2.getName() + "<br/>" +
                "<span style='font-size:18pt'>" + p2.getScore() + "</span></center></html>");

        if (gl.isGameOver()) {
            Player winner = gl.determineWinner();
            String msg = (winner == null) ? "It's a Draw!" : winner.getName() + " Wins! 🏆";
            turnLabel.setText("<html><center>Game Over<br/>" + msg + "</center></html>");
            turnLabel.setForeground(new Color(46, 204, 113));
        } else {
            turnLabel.setText("<html><center>Current Turn<br/>" + cur.getName() + "</center></html>");
            turnLabel.setForeground(cur.getColor());
        }

        moveLabel.setText("<html><center>Moves<br/>" + gl.getMoveCounter() + "</center></html>");
    }

    public void reset(GameLogic gl) { update(gl); }
}
