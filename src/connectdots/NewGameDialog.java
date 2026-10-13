package connectdots;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Modal dialog for configuring a new game: player names and board size.
 */
public class NewGameDialog extends JDialog {

    private static final Color BG     = new Color(22, 27, 45);
    private static final Color FG     = new Color(220, 230, 245);
    private static final Color ACCENT = new Color(52, 152, 219);

    private JTextField p1Field;
    private JTextField p2Field;
    private JComboBox<String> sizeBox;
    private boolean confirmed = false;

    public NewGameDialog(Frame parent, String currentP1, String currentP2, int currentSize) {
        super(parent, "New Game Setup", true);
        setResizable(false);
        buildUI(currentP1, currentP2, currentSize);
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI(String p1, String p2, int size) {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(24, 28, 20, 28));

        // Title
        JLabel title = new JLabel("Connect Dots – New Game");
        title.setForeground(ACCENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setBackground(BG);
        form.setBorder(new EmptyBorder(10, 0, 10, 0));

        form.add(label("Player 1 Name:"));
        p1Field = field(p1);
        form.add(p1Field);

        form.add(label("Player 2 Name:"));
        p2Field = field(p2);
        form.add(p2Field);

        form.add(label("Board Size:"));
        sizeBox = new JComboBox<>(new String[]{"3×3", "4×4", "5×5", "6×6", "7×7"});
        sizeBox.setSelectedIndex(size - 3);
        sizeBox.setBackground(new Color(35, 42, 65));
        sizeBox.setForeground(FG);
        sizeBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        form.add(sizeBox);

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btns.setBackground(BG);

        JButton ok = styledButton("Start Game", ACCENT);
        JButton cancel = styledButton("Cancel", new Color(80, 90, 110));

        ok.addActionListener(e -> { confirmed = true; dispose(); });
        cancel.addActionListener(e -> dispose());

        btns.add(ok);
        btns.add(cancel);
        root.add(btns, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(ok);
        setContentPane(root);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(FG);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return l;
    }

    private JTextField field(String text) {
        JTextField f = new JTextField(text);
        f.setBackground(new Color(35, 42, 65));
        f.setForeground(FG);
        f.setCaretColor(FG);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 90, 130)),
                new EmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 36));
        return b;
    }

    // ── Result getters ────────────────────────────────────────────────────────

    public boolean isConfirmed()  { return confirmed; }
    public String  getP1Name()    { return p1Field.getText().trim().isEmpty() ? "Player 1" : p1Field.getText().trim(); }
    public String  getP2Name()    { return p2Field.getText().trim().isEmpty() ? "Player 2" : p2Field.getText().trim(); }
    public int     getGridSize()  { return sizeBox.getSelectedIndex() + 3; }
}
