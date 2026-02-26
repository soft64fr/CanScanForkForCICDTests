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

/** Suite de tests unitaires pour la gestion de la typographie via FontManager. */
@DisplayName("*** Use Luciole font tests ***")
class FontManagerUTest {

    /** Réinitialise la police par défaut de l'UI avant chaque test. */
    @BeforeEach
    void setUp() {
        UIManager.put("defaultFont", new Font("Arial", Font.PLAIN, 12));
    }

    /**
     * Vérifie que l'initialisation du gestionnaire remplace correctement la police système par
     * Luciole.
     */
    @Test
    @DisplayName("FONT : Remplacement de la police par défaut par Luciole")
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
}
