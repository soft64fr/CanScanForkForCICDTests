/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
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
 * Singleton that loads and applies the Luciole font as the default Swing UI font.
 *
 * <p>The font is loaded from the application resources, registered, slightly vertically adjusted
 * for baseline alignment, and applied via {@link UIManager}. The UI is then refreshed with {@link
 * FlatLaf#updateUI()}.
 */
public enum UseLucioleFont {
    INSTANCE;

    private static final String FONT_LUCIOLE_PATH = "/font/Luciole-Regular.ttf";
    private static final String DEFAULT_FONT = "defaultFont";
    private static final double FONT_SHIFT_DOWN = 2.5;
    private static final JLabel DUMMY_JLABEL = new JLabel();

    /** Initializes and applies the Luciole font as the default Swing UI font. */
    public void initialize() {
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
     * Retrieves the FontMetrics object for the applied UI font.
     *
     * <p>Assumes the UIManager always contains a valid font reference for the default UI font.
     *
     * @return The FontMetrics for the applied font.
     */
    private FontMetrics getCurrentFontMetrics() {
        return DUMMY_JLABEL.getFontMetrics(UIManager.getFont(DEFAULT_FONT));
    }
}
