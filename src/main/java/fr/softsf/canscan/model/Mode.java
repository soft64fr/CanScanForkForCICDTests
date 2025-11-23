/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

/** Application operation modes. */
public enum Mode {
    MECARD("Contact"),
    FREE("Saisie libre"),
    MEET("Agenda");

    private final String label;

    Mode(String label) {
        this.label = label;
    }

    /** Returns the mode label. */
    public String text() {
        return label;
    }
}
