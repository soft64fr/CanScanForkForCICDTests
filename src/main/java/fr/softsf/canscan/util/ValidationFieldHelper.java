/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import javax.swing.JSlider;
import javax.swing.JTextField;

/** Validates and sanitizes user input for QR code parameters. */
public enum ValidationFieldHelper {
    INSTANCE;

    private static final int DEFAULT_QR_CODE_DIMENSION = 400;
    private static final int MINIMUM_QR_CODE_DIMENSION = 10;
    private static final int MARGIN_MAXIMUM_VALUE = 10;

    /**
     * Validates and returns image-to-QR ratio from slider.
     *
     * @param ratioSlider the slider containing the ratio value
     * @return validated ratio between 0 and 1
     */
    public double validateAndGetRatio(JSlider ratioSlider) {
        if (Checker.INSTANCE.checkNPE(ratioSlider, "validateAndGetRatio", "ratioSlider")) {
            return DoubleConstants.DEFAULT_IMAGE_RATIO.getValue();
        }
        try {
            double ratio = (double) ratioSlider.getValue() / IntConstants.MAX_PERCENTAGE.getValue();
            if (ratio < 0 || ratio > 1) {
                return DoubleConstants.DEFAULT_IMAGE_RATIO.getValue();
            }
            return ratio;
        } catch (NumberFormatException ex) {
            return DoubleConstants.DEFAULT_IMAGE_RATIO.getValue();
        }
    }

    /**
     * Validates and returns QR code margin from slider.
     *
     * @param marginSlider the slider containing the margin value
     * @return validated margin between 0 and 10
     */
    public int validateAndGetMargin(JSlider marginSlider) {
        if (Checker.INSTANCE.checkNPE(marginSlider, "validateAndGetMargin", "marginSlider")) {
            return 3;
        }
        try {
            int margin = marginSlider.getValue();
            if (margin < 0) {
                return 0;
            }
            return Math.min(margin, MARGIN_MAXIMUM_VALUE);
        } catch (NumberFormatException ex) {
            return 3;
        }
    }

    /**
     * Validates and returns QR code size from text field.
     *
     * @param sizeField the text field containing the size value
     * @return validated size in pixels
     */
    public int validateAndGetSize(JTextField sizeField) {
        if (Checker.INSTANCE.checkNPE(sizeField, "validateAndGetSize", "sizeField")) {
            return DEFAULT_QR_CODE_DIMENSION;
        }
        try {
            int size = Integer.parseInt(sizeField.getText());
            return Math.max(size, MINIMUM_QR_CODE_DIMENSION);
        } catch (NumberFormatException ex) {
            sizeField.setText(StringConstants.DEFAULT_QR_CODE_DIMENSION_FIELD.getValue());
            return DEFAULT_QR_CODE_DIMENSION;
        }
    }
}
