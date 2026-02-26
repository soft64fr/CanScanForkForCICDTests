/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Suite de tests unitaires pour les utilitaires de formatage de dates. */
@DisplayName("*** Date helper tests ***")
class DateHelperUTest {

    /** Vérifie que le formatage de l'instant présent respecte la norme UTC ISO-8601 compacte. */
    @Test
    @DisplayName("UTC : Formatage de l'instant présent")
    void givenCurrentTimeWhenNowUtcThenReturnFormattedString() {
        String result = DateHelper.INSTANCE.nowUtc();
        assertNotNull(result);
        assertTrue(result.matches("\\d{8}T\\d{6}Z"));
    }

    /** Vérifie la concaténation et le formatage d'une date et d'une heure spécifiques. */
    @Test
    @DisplayName("UTC : Formatage d'une date et heure valides")
    void givenValidDateAndTimeWhenValidateAndGetDateAndTimeThenReturnFormattedUtcString() {
        LocalDate date = LocalDate.of(2025, 11, 21);
        LocalTime time = LocalTime.of(17, 30, 45);
        String result = DateHelper.INSTANCE.validateAndGetDateAndTime(date, time);
        assertEquals("20251121T173045Z", result);
    }

    /** Vérifie la gestion d'une date nulle. */
    @Test
    @DisplayName("VALIDATION : Gestion d'une date nulle")
    void givenNullDateWhenValidateAndGetDateAndTimeThenReturnEmptyString() {
        LocalTime time = LocalTime.of(10, 0);
        String result = DateHelper.INSTANCE.validateAndGetDateAndTime(null, time);
        assertEquals("", result);
    }

    /** Vérifie la gestion d'une heure nulle. */
    @Test
    @DisplayName("VALIDATION : Gestion d'une heure nulle")
    void givenNullTimeWhenValidateAndGetDateAndTimeThenReturnEmptyString() {
        LocalDate date = LocalDate.of(2025, 11, 21);
        String result = DateHelper.INSTANCE.validateAndGetDateAndTime(date, null);
        assertEquals("", result);
    }

    /** Vérifie la gestion de paramètres nuls simultanés. */
    @Test
    @DisplayName("VALIDATION : Gestion date et heure nulles")
    void givenNullDateAndTimeWhenValidateAndGetDateAndTimeThenReturnEmptyString() {
        String result = DateHelper.INSTANCE.validateAndGetDateAndTime(null, null);
        assertEquals("", result);
    }
}
