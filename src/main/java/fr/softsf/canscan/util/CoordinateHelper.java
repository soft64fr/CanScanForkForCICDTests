/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import javax.swing.JTextField;

/**
 * Validating and formatting geographic coordinates.
 *
 * <p>Provides real-time validation for latitude/longitude input fields, ensuring format compliance
 * and range validity for QR code generation.
 *
 * <p>Usage: {@code CoordinateHelper.INSTANCE.getValidatedCoordinate(field, true)}
 */
public enum CoordinateHelper {
    INSTANCE;

    private static final String EMPTY = "";
    private static final int LATITUDE_PRECISION = 8;
    private static final int LONGITUDE_PRECISION = 7;
    private static final int MAXIMUM_LATITUDE = 90;
    private static final int MINIMUMU_LATITUDE = -MAXIMUM_LATITUDE;
    private static final int MAXIMUM_LONGITUDE = 180;
    private static final int MINIMUM_LONGITUDE = -MAXIMUM_LONGITUDE;
    private static final int DECIMAL_BASE = 10;

    /**
     * Validates and truncates a coordinate from a JTextField for QR code generation.
     *
     * <p>Validates format {@code -?\\d*(\\.\\d*)?} and range (lat: [-90,90], lon: [-180,180]).
     * Truncates decimals toward zero (lat: 8, lon: 7). Clears field on invalid input.
     *
     * @param field the JTextField to validate
     * @param isLatitude true for latitude, false for longitude
     * @return truncated coordinate, or "" if invalid
     */
    public String getValidatedCoordinate(JTextField field, boolean isLatitude) {
        if (Checker.INSTANCE.checkNPE(field, "getValidatedCoordinate", "field")) {
            return EMPTY;
        }
        String text = field.getText().trim();
        if (text.equals("-")) {
            return "-0";
        }
        if (text.matches("^-?\\d*(\\.\\d*)?$")) {
            return parseAndTruncate(field, text, isLatitude);
        }
        return clearAndReturnEmpty(field);
    }

    /**
     * Clears the field and returns empty string.
     *
     * @param field the JTextField to clear
     * @return empty string
     */
    private String clearAndReturnEmpty(JTextField field) {
        field.setText(EMPTY);
        return EMPTY;
    }

    /**
     * Parses and truncates a coordinate string.
     *
     * @param field the JTextField to clear if invalid
     * @param text the coordinate string to parse
     * @param isLatitude true for latitude, false for longitude
     * @return truncated coordinate, or "" if parsing fails or out of range
     */
    private String parseAndTruncate(JTextField field, String text, boolean isLatitude) {
        try {
            double value = Double.parseDouble(text);
            if (isInRange(value, isLatitude)) {
                return truncate(value, isLatitude);
            }
            return clearAndReturnEmpty(field);
        } catch (NumberFormatException e) {
            return clearAndReturnEmpty(field);
        }
    }

    /**
     * Checks if a coordinate value is within valid range.
     *
     * @param value the coordinate value
     * @param isLatitude true for latitude [-90,90], false for longitude [-180,180]
     * @return true if in range
     */
    private boolean isInRange(double value, boolean isLatitude) {
        return isLatitude
                ? value >= MINIMUMU_LATITUDE && value <= MAXIMUM_LATITUDE
                : value >= MINIMUM_LONGITUDE && value <= MAXIMUM_LONGITUDE;
    }

    /**
     * Truncates coordinate decimals toward zero.
     *
     * @param value the coordinate value
     * @param isLatitude true for 8 decimals, false for 7 decimals
     * @return truncated coordinate as string
     */
    private String truncate(double value, boolean isLatitude) {
        int precision = isLatitude ? LATITUDE_PRECISION : LONGITUDE_PRECISION;
        double factor = Math.pow(DECIMAL_BASE, precision);
        value =
                value >= 0
                        ? Math.floor(value * factor) / factor
                        : Math.ceil(value * factor) / factor;
        return String.valueOf(value);
    }
}
