/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.FontUtils;

import fr.softsf.canscan.CanScan;
import fr.softsf.canscan.constant.StringConstants;
import fr.softsf.canscan.ui.MyPopup;

/**
 * Singleton responsible for loading and applying custom fonts in the Swing UI.
 *
 * <p>It loads the **Luciole font** as the default {@link UIManager} font (with vertical adjustment)
 * and registers the **Material Icons font** for use in specialized UI components. This class is
 * accessed via the {@code INSTANCE} enum constant.
 */
public enum FontManager {
    INSTANCE;

    private static final String FONT_LUCIOLE_PATH = "/font/Luciole-Regular.ttf";
    private static final String FONT_MATERIAL_ICONS_PATH = "/font/MaterialIcons-Regular.ttf";
    private static final String DEFAULT_FONT = "defaultFont";
    private static final double FONT_SHIFT_DOWN = 2.5;
    private static final JLabel DUMMY_JLABEL = new JLabel();
    private static final Font JRE_GUARANTEED_FONT_FALLBACK_FOR_UNIT_TESTS =
            new Font("Dialog", Font.PLAIN, 12);

    /**
     * Initializes and sets the **Luciole font** as the default Swing UI font (including
     * scaling/shifting). It also loads and registers the **Material Icons font** for UI components.
     * *
     *
     * <p>The UI is updated asynchronously on the EDT using {@link
     * SwingUtilities#invokeLater(Runnable)} after fonts are loaded.
     */
    public void initialize() {
        loadLucioleFontAsDefaultFont();
        loadMaterialIconsFont();
    }

    private static void loadLucioleFontAsDefaultFont() {
        try (InputStream is = CanScan.class.getResourceAsStream(FONT_LUCIOLE_PATH)) {
            if (is == null) {
                throw new IllegalStateException(
                        "La police Luciole n'a pas pu être chargée : " + FONT_LUCIOLE_PATH);
            }
            Font defaultFont = UIManager.getFont(DEFAULT_FONT);
            Font loadedFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loadedFont);
            Font luciole =
                    FontUtils.getCompositeFont(
                            loadedFont.getFamily(), defaultFont.getStyle(), defaultFont.getSize());
            AffineTransform shiftDown = AffineTransform.getTranslateInstance(0, FONT_SHIFT_DOWN);
            Font adjustedFont =
                    luciole.deriveFont(shiftDown).deriveFont(luciole.getStyle(), luciole.getSize());
            UIManager.put(DEFAULT_FONT, adjustedFont);
            SwingUtilities.invokeLater(FlatLaf::updateUI);
        } catch (FontFormatException | IOException | IllegalStateException e) {
            MyPopup.INSTANCE.showDialog(
                    "La police Luciole n'a pas pu être utilisée\n",
                    e.getMessage(),
                    StringConstants.ERREUR.getValue());
        }
    }

    /**
     * Loads Material Icons (TTF) and registers it in the JVM's Graphics Environment. The font is
     * then accessible using the family name "Material Icons".
     */
    private void loadMaterialIconsFont() {
        try (InputStream is = CanScan.class.getResourceAsStream(FONT_MATERIAL_ICONS_PATH)) {
            if (is == null) {
                throw new IllegalStateException(
                        "La police Material icon n'a pas pu être chargée : "
                                + FONT_MATERIAL_ICONS_PATH);
            }
            Font loadedFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loadedFont);
        } catch (FontFormatException | IOException e) {
            MyPopup.INSTANCE.showDialog(
                    "La police Material icon n'a pas pu être utilisée\n",
                    e.getMessage(),
                    StringConstants.ERREUR.getValue());
        }
    }

    /**
     * Returns the reference character width based on 'W'.
     *
     * <p>Uses an internal JLabel context to calculate exact metrics.
     *
     * @return The width of 'W' in pixels.
     */
    public int getCharWidth() {
        return getCurrentFontMetrics().charWidth('W');
    }

    /**
     * Returns the total line height (ascent + descent + leading).
     *
     * @return The line height in pixels.
     */
    public int getLineHeight() {
        return getCurrentFontMetrics().getHeight();
    }

    /**
     * Retrieves {@link FontMetrics} safely.
     *
     * <p>Guarantees non-null return, resolving the Font and relying on client catch blocks for
     * fault tolerance in headless environments.
     *
     * @return The real {@link FontMetrics} object.
     */
    private FontMetrics getCurrentFontMetrics() {
        Font font = UIManager.getFont(DEFAULT_FONT);
        if (font == null) {
            font = JRE_GUARANTEED_FONT_FALLBACK_FOR_UNIT_TESTS;
        }
        return DUMMY_JLABEL.getFontMetrics(font);
    }
}
