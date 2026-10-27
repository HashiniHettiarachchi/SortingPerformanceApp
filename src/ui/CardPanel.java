package ui;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A rounded-corner card: the surface the app's sections are grouped into.
 *
 * <p>Paints its own anti-aliased rounded background instead of relying on a
 * look-and-feel specific API, so the card look is identical in light and dark
 * mode (and would survive a look-and-feel change).</p>
 */
public class CardPanel extends JPanel {

    private final int arc;

    public CardPanel(int arc) {
        this.arc = arc;
        setOpaque(false);
        setBackground(Theme.cardBackground());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        } finally {
            g2.dispose();
        }
        // Not calling super: the parent JPanel would fill a square background.
    }
}
