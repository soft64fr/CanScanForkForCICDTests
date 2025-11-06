/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan;

import java.awt.Font;
import javax.swing.UIManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.softsf.canscan.util.UseLucioleFont;

import static org.junit.jupiter.api.Assertions.*;

class UseLucioleFontTest {

    @BeforeEach
    void setUp() {
        UIManager.put("defaultFont", new Font("Arial", Font.PLAIN, 12));
    }

    @Test
    void givenDefaultFont_whenInitializeLuciole_thenDefaultFontIsReplaced() {
        Font before = UIManager.getFont("defaultFont");
        assertNotNull(before);
        UseLucioleFont.INSTANCE.initialize();
        Font after = UIManager.getFont("defaultFont");
        assertNotNull(after);
        assertNotEquals(before, after, "La font Luciole doit remplacer la font par défaut");
        assertTrue(
                after.getFamily().toLowerCase().contains("luciole"),
                "La famille de la font doit contenir 'Luciole'");
    }
}
