package ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.util.Collections;

/**
 * Central place for the application's look and feel.
 *
 * <p>Uses the FlatLaf library for the flat, modern component styling, plus a
 * small hand-picked palette for the card layout. Supports switching between
 * light and dark mode at runtime.</p>
 */
public final class Theme {

    /** Indigo accent used for the primary (default) button and highlights. */
    public static final Color ACCENT_LIGHT = new Color(0x4F46E5);
    public static final Color ACCENT_DARK = new Color(0x818CF8);

    private static boolean dark;

    private Theme() {
    }

    /** Installs FlatLaf with the accent colour. Call once on the EDT before creating UI. */
    public static void install() {
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#4f46e5"));
        apply(false);
    }

    /** Switches between light and dark mode and refreshes all open windows. */
    public static void apply(boolean darkMode) {
        dark = darkMode;
        try {
            UIManager.setLookAndFeel(dark ? new FlatDarkLaf() : new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("FlatLaf could not be installed: " + e.getMessage());
        }

        // Slightly larger, friendlier base font than the FlatLaf default.
        Font base = UIManager.getFont("defaultFont");
        if (base != null) {
            UIManager.put("defaultFont", base.deriveFont(14f));
        }
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("Table.rowHeight", 26);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.showHorizontalLines", false);
        UIManager.put("Table.intercellSpacing", 0);

        if (FlatLaf.isLafDark() != dark) {
            return;   // LAF install failed; leave the palette alone
        }
        FlatLaf.updateUI();
    }

    public static boolean isDark() {
        return dark;
    }

    // ------------------------------------------------------------------
    // Palette (kept explicit so cards look identical on every platform)
    // ------------------------------------------------------------------

    /** Window background behind the cards. */
    public static Color windowBackground() {
        return dark ? new Color(0x111827) : new Color(0xE8EBF0);
    }

    /** Card surface sitting on the window background. */
    public static Color cardBackground() {
        return dark ? new Color(0x1F2937) : Color.WHITE;
    }

    /** Hairline used for table grids and subtle separators. */
    public static Color hairline() {
        return dark ? new Color(0x374151) : new Color(0xE5E7EB);
    }

    /** Secondary/muted text colour. */
    public static Color mutedText() {
        return dark ? new Color(0x9CA3AF) : new Color(0x6B7280);
    }

    public static Color accent() {
        return dark ? ACCENT_DARK : ACCENT_LIGHT;
    }

    /**
     * Header-band accent. Stays on the stronger light-mode accent in both
     * themes: white header text keeps good contrast, and the vivid band pops
     * against the dark window.
     */
    public static Color headerAccent() {
        return ACCENT_LIGHT;
    }

    /** Soft tint of the accent for banners and badges. */
    public static Color accentTint() {
        return dark ? new Color(0x312E81) : new Color(0xE0E7FF);
    }

    /** Success tint (best-algorithm banner, numeric tags). */
    public static Color successTint() {
        return dark ? new Color(0x14532D) : new Color(0xD1FAE5);
    }

    public static Color successText() {
        return dark ? new Color(0x86EFAC) : new Color(0x047857);
    }

    /** Blends two colours; fraction 0 = a, 1 = b. */
    public static Color blend(Color a, Color b, float fraction) {
        float[] ca = a.getRGBComponents(null);
        float[] cb = b.getRGBComponents(null);
        return new Color(
                ca[0] + (cb[0] - ca[0]) * fraction,
                ca[1] + (cb[1] - ca[1]) * fraction,
                ca[2] + (cb[2] - ca[2]) * fraction);
    }
}
