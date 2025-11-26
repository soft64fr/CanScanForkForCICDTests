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

@DisplayName("*** Validation field helper tests ***")
class ValidationFieldHelperUTest {

    @Test
    void givenValidSliderValueWhenValidateAndGetRatioThenReturnNormalizedRatio() {
        JSlider slider = new JSlider();
        slider.setValue(50); // 50%
        double result = ValidationFieldHelper.INSTANCE.validateAndGetRatio(slider);
        assertEquals(0.5, result);
    }

    @Test
    void givenOverMaximumSliderValueWhenValidateAndGetRatioThenReturnOne() {
        JSlider slider = new JSlider();
        slider.setMaximum(IntConstants.MAX_PERCENTAGE.getValue());
        slider.setValue(200);
        double result = ValidationFieldHelper.INSTANCE.validateAndGetRatio(slider);
        assertEquals(1.0, result);
    }

    @Test
    void givenValidSliderValueWhenValidateAndGetMarginThenReturnMargin() {
        JSlider slider = new JSlider();
        slider.setValue(5);
        int result = ValidationFieldHelper.INSTANCE.validateAndGetMargin(slider);
        assertEquals(5, result);
    }

    @Test
    void givenNegativeSliderValueWhenValidateAndGetMarginThenReturnZero() {
        JSlider slider = new JSlider();
        slider.setValue(-3);
        int result = ValidationFieldHelper.INSTANCE.validateAndGetMargin(slider);
        assertEquals(0, result);
    }

    @Test
    void givenTooLargeSliderValueWhenValidateAndGetMarginThenReturnMaximum() {
        JSlider slider = new JSlider();
        slider.setValue(50);
        int result = ValidationFieldHelper.INSTANCE.validateAndGetMargin(slider);
        assertEquals(10, result);
    }

    @Test
    void givenValidSizeFieldWhenValidateAndGetSizeThenReturnSize() {
        JTextField field = new JTextField("250");
        int result = ValidationFieldHelper.INSTANCE.validateAndGetSize(field);
        assertEquals(250, result);
    }

    @Test
    void givenTooSmallSizeFieldWhenValidateAndGetSizeThenReturnMinimum() {
        JTextField field = new JTextField("5");
        int result = ValidationFieldHelper.INSTANCE.validateAndGetSize(field);
        assertEquals(10, result);
    }

    @Test
    void givenInvalidSizeFieldWhenValidateAndGetSizeThenReturnDefaultAndResetField() {
        JTextField field = new JTextField("abc");
        int result = ValidationFieldHelper.INSTANCE.validateAndGetSize(field);
        assertEquals(400, result);
        assertEquals(StringConstants.DEFAULT_QR_CODE_DIMENSION_FIELD.getValue(), field.getText());
    }

    @Test
    void givenMeetTitleWithAccentsAndSpacesWhenValidateAndGetMeetUIDThenReturnSanitizedUid() {
        String result =
                ValidationFieldHelper.INSTANCE.validateAndGetMeetUID("Réunion spéciale 2025!");
        assertTrue(result.startsWith("REUNIONSPECIALE2025"));
        assertTrue(result.endsWith(StringConstants.DOMAIN.getValue()));
    }
}
