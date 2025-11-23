/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.constant;

/** Double constants. */
public enum DoubleConstants {
    DEFAULT_IMAGE_RATIO(0.27);

    private final double value;

    DoubleConstants(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
