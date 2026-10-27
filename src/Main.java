import ui.SorterFrame;
import ui.Theme;

import javax.swing.SwingUtilities;

/**
 * Application entry point: installs the FlatLaf theme, then opens the main
 * window on the event dispatch thread, as Swing requires.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new SorterFrame().setVisible(true);
        });
    }
}
