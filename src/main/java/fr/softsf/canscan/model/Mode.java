/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

/**
 * Defines the application operation modes.
 *
 * <ul>
 *   <li>MECARD – structured format mode
 *   <li>FREE – unrestricted input mode
 * </ul>
 */
public enum Mode {
    MECARD,
    FREE;

    /**
     * Returns the name of the mode as a string.
     *
     * @return the mode name
     */
    public String text() {
        return name();
    }
}
