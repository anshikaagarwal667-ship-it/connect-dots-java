package connectdots;

import javax.swing.*;

/**
 * Application entry point.
 * Launches the Connect Dots game on the Swing Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
            new GUI();
        });
    }
}
