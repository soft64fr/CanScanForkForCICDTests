/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.service;

import javax.swing.JSlider;
import javax.swing.JTextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.softsf.canscan.constant.IntConstants;
import fr.softsf.canscan.constant.StringConstants;
import fr.softsf.canscan.util.ValidationFieldHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Suite de tests unitaires pour le service de validation des champs de saisie (UI). */
@DisplayName("*** Validation field helper tests ***")
class ValidationFieldHelperUTest {

    /** Vérifie la normalisation du ratio (pourcentage vers double entre 0 et 1). */
    @Test
    @DisplayName("VALIDATION : Normalisation du ratio depuis le slider")
    void givenValidSliderValueWhenValidateAndGetRatioThenReturnNormalizedRatio() {
        JSlider slider = new JSlider();
        slider.setValue(50); // 50%
        double result = ValidationFieldHelper.INSTANCE.validateAndGetRatio(slider);
        assertEquals(0.5, result);
    }

    /** Vérifie le plafonnement du ratio à 1.0 en cas de dépassement. */
    @Test
    @DisplayName("VALIDATION : Ratio plafonné au maximum")
    void givenOverMaximumSliderValueWhenValidateAndGetRatioThenReturnOne() {
        JSlider slider = new JSlider();
        slider.setMaximum(IntConstants.MAX_PERCENTAGE.getValue());
        slider.setValue(200);
        double result = ValidationFieldHelper.INSTANCE.validateAndGetRatio(slider);
        assertEquals(1.0, result);
    }

    /** Vérifie la récupération d'une valeur de marge valide. */
    @Test
    @DisplayName("VALIDATION : Récupération d'une marge valide")
    void givenValidSliderValueWhenValidateAndGetMarginThenReturnMargin() {
        JSlider slider = new JSlider();
        slider.setValue(5);
        int result = ValidationFieldHelper.INSTANCE.validateAndGetMargin(slider);
        assertEquals(5, result);
    }

    /** Vérifie que les marges négatives sont ramenées à zéro. */
    @Test
    @DisplayName("VALIDATION : Marge négative ramenée à zéro")
    void givenNegativeSliderValueWhenValidateAndGetMarginThenReturnZero() {
        JSlider slider = new JSlider();
        slider.setValue(-3);
        int result = ValidationFieldHelper.INSTANCE.validateAndGetMargin(slider);
        assertEquals(0, result);
    }

    /** Vérifie le plafonnement de la marge à sa valeur maximale autorisée (10). */
    @Test
    @DisplayName("VALIDATION : Marge plafonnée au maximum")
    void givenTooLargeSliderValueWhenValidateAndGetMarginThenReturnMaximum() {
        JSlider slider = new JSlider();
        slider.setValue(50);
        int result = ValidationFieldHelper.INSTANCE.validateAndGetMargin(slider);
        assertEquals(10, result);
    }

    /** Vérifie la validation d'une taille de QR Code correcte. */
    @Test
    @DisplayName("VALIDATION : Taille de QR Code valide")
    void givenValidSizeFieldWhenValidateAndGetSizeThenReturnSize() {
        JTextField field = new JTextField("250");
        int result = ValidationFieldHelper.INSTANCE.validateAndGetSize(field);
        assertEquals(250, result);
    }

    /** Vérifie que les tailles trop petites sont ramenées au minimum (10). */
    @Test
    @DisplayName("VALIDATION : Taille minimale imposée")
    void givenTooSmallSizeFieldWhenValidateAndGetSizeThenReturnMinimum() {
        JTextField field = new JTextField("5");
        int result = ValidationFieldHelper.INSTANCE.validateAndGetSize(field);
        assertEquals(10, result);
    }

    /**
     * Vérifie le retour à la valeur par défaut et la réinitialisation du champ en cas de saisie
     * invalide.
     */
    @Test
    @DisplayName("VALIDATION : Réinitialisation si saisie de taille non numérique")
    void givenInvalidSizeFieldWhenValidateAndGetSizeThenReturnDefaultAndResetField() {
        JTextField field = new JTextField("abc");
        int result = ValidationFieldHelper.INSTANCE.validateAndGetSize(field);
        assertEquals(400, result);
        assertEquals(StringConstants.DEFAULT_QR_CODE_DIMENSION_FIELD.getValue(), field.getText());
    }

    /**
     * Vérifie la sanitisation d'un titre de réunion (suppression accents, espaces, ponctuation).
     */
    @Test
    @DisplayName("SERVICE : Génération d'un UID de réunion sanitizé")
    void givenMeetTitleWithAccentsAndSpacesWhenValidateAndGetMeetUIDThenReturnSanitizedUid() {
        String result =
                ValidationFieldHelper.INSTANCE.validateAndGetMeetUID("Réunion spéciale 2025!");
        assertTrue(result.startsWith("REUNIONSPECIALE2025"));
        assertTrue(result.endsWith(StringConstants.DOMAIN.getValue()));
    }
}
