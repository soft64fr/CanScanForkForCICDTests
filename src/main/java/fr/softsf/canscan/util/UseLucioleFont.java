/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.FontUtils;

import fr.softsf.canscan.CanScan;
import fr.softsf.canscan.ui.Popup;

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

    /**
     * Initializes and applies the Luciole font as the default Swing UI font. If the font cannot be
     * loaded, a popup error dialog is shown.
     */
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
            AffineTransform shiftDown = AffineTransform.getTranslateInstance(0, 2.5);
            Font adjustedFont =
                    luciole.deriveFont(shiftDown).deriveFont(luciole.getStyle(), luciole.getSize());
            UIManager.put(DEFAULT_FONT, adjustedFont);
            SwingUtilities.invokeLater(FlatLaf::updateUI);
        } catch (FontFormatException | IOException | IllegalStateException e) {
            Popup.INSTANCE.showDialog(
                    "La police Luciole n'a pas pu être utilisée\n", e.getMessage(), "ERREUR");
        }
    }
}
