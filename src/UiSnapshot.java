import ui.SorterFrame;
import ui.Theme;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

/** Throwaway helper: renders the main window to PNGs (light + dark, populated) for visual review. */
public class UiSnapshot {
    public static void main(String[] args) throws Exception {
        Theme.install();
        SorterFrame frame = new SorterFrame();
        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));
        Thread.sleep(1200);
        shot(frame, "ui_light.png");

        // Populate with the sample dataset and run a real sort so the tables,
        // column tags, banner and best-row highlight are all visible.
        SwingUtilities.invokeAndWait(() -> {
            try {
                frame.loadDataset(Path.of("sample-data", "ai_usage.csv"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            frame.sortColumn(7);   // 'Monthly_Income'
        });
        Thread.sleep(2500);
        shot(frame, "ui_light_data.png");

        SwingUtilities.invokeAndWait(() -> frame.setDarkMode(true));
        Thread.sleep(1200);
        shot(frame, "ui_dark_data.png");

        System.exit(0);
    }

    private static void shot(SorterFrame frame, String name) throws Exception {
        BufferedImage img = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
        frame.printAll(img.createGraphics());
        ImageIO.write(img, "png", new File(name));
        System.out.println("wrote " + name + " " + frame.getWidth() + "x" + frame.getHeight());
    }
}
