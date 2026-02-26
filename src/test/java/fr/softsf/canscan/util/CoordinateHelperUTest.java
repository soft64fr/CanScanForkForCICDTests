/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import javax.swing.JTextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Suite de tests unitaires pour la validation et le formatage des coordonnées géographiques. */
@DisplayName("*** Coordinate helper tests ***")
class CoordinateHelperUTest {

    /** Vérifie que la latitude est correctement tronquée après validation. */
    @Test
    @DisplayName("VALIDATION : Tronquage d'une latitude valide")
    void givenValidLatitudeWhenGetValidatedCoordinateThenReturnTruncatedValue() {
        JTextField field = new JTextField("48.856612345");
        String result = CoordinateHelper.INSTANCE.getValidatedCoordinate(field, true);
        assertNotNull(result);
        assertTrue(result.startsWith("48.8566"));
    }

    /** Vérifie que la longitude est correctement tronquée après validation. */
    @Test
    @DisplayName("VALIDATION : Tronquage d'une longitude valide")
    void givenValidLongitudeWhenGetValidatedCoordinateThenReturnTruncatedValue() {
        JTextField field = new JTextField("2.352212345");
        String result = CoordinateHelper.INSTANCE.getValidatedCoordinate(field, false);
        assertNotNull(result);
        assertTrue(result.startsWith("2.3522"));
    }

    /** Vérifie le comportement spécifique lorsqu'un signe moins est saisi seul. */
    @Test
    @DisplayName("UI : Gestion du signe moins isolé")
    void givenMinusSignOnlyWhenGetValidatedCoordinateThenReturnMinusZero() {
        JTextField field = new JTextField("-");
        String result = CoordinateHelper.INSTANCE.getValidatedCoordinate(field, true);
        assertEquals("-0", result);
    }

    /**
     * Vérifie que les entrées hors limites ou mal formatées vident le champ et retournent une
     * chaîne vide.
     */
    @ParameterizedTest(name = "Entrée : {0}, IsLatitude : {1}")
    @CsvSource({
        "100,true", // latitude not in range [-90,90]
        "200,false", // longitude not in range [-180,180]
        "abc,true" // invalid format
    })
    @DisplayName("VALIDATION : Rejet des entrées hors limites ou invalides")
    void givenInvalidInputWhenGetValidatedCoordinateThenReturnEmptyAndClearField(
            String input, boolean isLatitude) {
        JTextField field = new JTextField(input);
        String result = CoordinateHelper.INSTANCE.getValidatedCoordinate(field, isLatitude);
        assertEquals("", result);
        assertEquals("", field.getText());
    }

    /** Vérifie le traitement des latitudes négatives. */
    @Test
    @DisplayName("VALIDATION : Latitude négative valide")
    void givenNegativeLatitudeWhenGetValidatedCoordinateThenReturnTruncatedNegativeValue() {
        JTextField field = new JTextField("-48.856612345");
        String result = CoordinateHelper.INSTANCE.getValidatedCoordinate(field, true);
        assertTrue(result.startsWith("-48.8566"));
    }
}
