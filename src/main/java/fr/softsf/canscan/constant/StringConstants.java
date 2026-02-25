/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.constant;

import java.util.Locale;

import fr.softsf.canscan.util.ApplicationMetadata;

/** String constants for messages and identifiers. */
public enum StringConstants {
    VERSION(ApplicationMetadata.INSTANCE.getVersion()),
    QR_DATA("qrData"),
    ERREUR("Erreur"),
    GENERATE_QR_CODE("generateQrCode"),
    GENERATE_AND_SAVE_QR_CODE("generateAndSaveQrCode"),
    DEFAULT_QR_CODE_DIMENSION_FIELD("400"),
    DOMAIN("@SOFT64.FR"),
    LATEST_RELEASES_REPO_URL("https://github.com/Lob2018/CanScan/releases/latest"),
    CURRENT_OS(System.getProperty("os.name").toLowerCase(Locale.ROOT)),
    OS_WINDOWS_KEY("win"),
    OS_LINUX_KEY("linux"),
    JAVA_2D_DPI_AWARE("sun.java2d.dpiaware"),
    JAVA_2D_XRENDER("sun.java2d.xrender"),
    JAVA_2D_UI_SCALE_ENABLED("sun.java2d.uiScale.enabled"),
    TRUE("true");

    private final String value;

    StringConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
