/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import java.awt.Font;
import javax.swing.UIManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit test suite for typography and scaling management via FontManager. */
@DisplayName("*** Use Luciole font tests ***")
class FontManagerUTest {

    /** Resets the UI default font before each test to ensure a clean state. */
    @BeforeEach
    void setUp() {
        UIManager.put("defaultFont", new Font("Arial", Font.PLAIN, 12));
    }

    /** Verifies that the manager initialization correctly replaces the system font with Luciole. */
    @Test
    @DisplayName("FONT : Replacement of default font with Luciole")
    void givenDefaultFont_whenInitializeLuciole_thenDefaultFontIsReplaced() {
        Font before = UIManager.getFont("defaultFont");
        assertNotNull(before);
        FontManager.INSTANCE.initialize();
        Font after = UIManager.getFont("defaultFont");
        assertNotNull(after);
        assertNotEquals(before, after, "La font Luciole doit remplacer la font par défaut");
        assertTrue(
                after.getFamily().toLowerCase().contains("luciole"),
                "La famille de la font doit contenir 'Luciole'");
    }

    /**
     * Verifies that getScaledDimension returns the base value when font height matches reference.
     */
    @Test
    @DisplayName("SCALING : Dimension stays same when font is standard (16px)")
    void givenStandardFont_whenGetScaledDimension_thenReturnSameValue() {
        UIManager.put("defaultFont", new Font("Arial", Font.PLAIN, 12));
        int basePx = 100;
        int result = FontManager.INSTANCE.getScaledDimension(basePx);
        double expectedRatio =
                FontManager.INSTANCE.getLineHeight() / FontManager.BASE_LINE_HEIGHT_REFERENCE;
        assertEquals((int) Math.round(basePx * expectedRatio), result);
    }

    /** Verifies scaling with a large font to simulate HiDPI/Windows Scaling. */
    @Test
    @DisplayName("SCALING : Dimension increases with larger font")
    void givenLargeFont_whenGetScaledDimension_thenReturnLargerValue() {
        UIManager.put("defaultFont", new Font("Arial", Font.PLAIN, 24));
        int basePx = 50;
        int scaled = FontManager.INSTANCE.getScaledDimension(basePx);
        assertTrue(scaled > basePx, "Scaled dimension should be larger for a 24pt font");
    }

    /** Verifies that the calculation handles the fallback font if UIManager is cleared. */
    @Test
    @DisplayName("SCALING : Fallback font usage when defaultFont is null")
    void givenNullFont_whenGetScaledDimension_thenUseFallbackMetrics() {
        UIManager.put("defaultFont", null);
        int result = FontManager.INSTANCE.getScaledDimension(50);
        assertTrue(result > 0, "Should return a valid scaled dimension even with fallback font");
    }
}
