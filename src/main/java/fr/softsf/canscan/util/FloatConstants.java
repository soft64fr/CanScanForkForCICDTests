/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

/** Float constants. */
public enum FloatConstants {
    OVERLAY_PANEL_ALIGNMENT(0.5f);

    private final float value;

    FloatConstants(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }
}
