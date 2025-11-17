/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

/** String constants for messages and identifiers. */
public enum StringConstants {
    QR_DATA("qrData"),
    ERREUR("Erreur"),
    GENERATE_QR_CODE("generateQrCode"),
    GENERATE_AND_SAVE_QR_CODE("generateAndSaveQrCode"),
    DEFAULT_QR_CODE_DIMENSION_FIELD("400");

    private final String value;

    StringConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
