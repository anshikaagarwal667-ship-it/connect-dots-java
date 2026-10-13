package connectdots;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Deque;

/**
 * Sidebar panel that displays the move history as a scrollable list.
 */
public class HistoryPanel extends JPanel {

    private static final Color BG   = new Color(18, 22, 38);
    private static final Color FG   = new Color(200, 210, 230);
    private static final Color HDR  = new Color(100, 120, 160);

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> list;

    public HistoryPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(BG);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(200, 0));

        JLabel title = new JLabel("Move History");
        title.setForeground(HDR);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(title, BorderLayout.NORTH);

        list = new JList<>(listModel);
        list.setBackground(new Color(28, 33, 54));
        list.setForeground(FG);
        list.setFont(new Font("Monospaced", Font.PLAIN, 11));
        list.setSelectionBackground(new Color(52, 73, 94));
        list.setBorder(new EmptyBorder(4, 4, 4, 4));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 60, 90)));
        scroll.getViewport().setBackground(new Color(28, 33, 54));
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh(Deque<MoveRecord> history) {
        listModel.clear();
        // Deque is LIFO; display newest at top
        for (MoveRecord r : history) {
            listModel.addElement(r.toString());
        }
    }

    public void clear() {
        listModel.clear();
    }
}
