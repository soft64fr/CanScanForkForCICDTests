/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.constant;

import fr.softsf.canscan.util.UseLucioleFont;

/** Int constants. */
public enum IntConstants {
    DEFAULT_GAP(15),
    LOADER_SIZE_OFFSET(4),
    MAX_PERCENTAGE(100),
    TEXT_FIELDS_COLUMNS(25),
    DEFAULT_LABEL_WIDTH(UseLucioleFont.INSTANCE.getCharWidth() * 9);

    private final int value;

    IntConstants(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
