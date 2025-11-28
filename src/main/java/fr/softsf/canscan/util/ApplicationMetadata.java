/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import fr.softsf.canscan.constant.StringConstants;
import fr.softsf.canscan.ui.MyPopup;

/**
 * Application metadata loaded from `version.properties`.
 *
 * <p>Name, version, and organization are initialized at construction. Version is accessible via
 * {@link #getVersion()}, and the window title can be built with {@link #initializeTitle()}.
 */
public enum ApplicationMetadata {
    INSTANCE;

    private final String version;
    private final String name;
    private final String organization;

    // Loads properties once upon enum initialization
    ApplicationMetadata() {
        Properties props = new Properties();
        String tmpVersion = null;
        String tmpName = null;
        String tmpOrg = null;
        try (InputStream in =
                ApplicationMetadata.class
                        .getClassLoader()
                        .getResourceAsStream("version.properties")) {
            if (in != null) {
                props.load(in);
                tmpVersion = props.getProperty("app.version");
                tmpName = props.getProperty("app.name");
                tmpOrg = props.getProperty("app.organization");
            }
        } catch (IOException e) {
            MyPopup.INSTANCE.showDialog(
                    "The version.properties file is unreadable\n",
                    e.getMessage(),
                    StringConstants.ERREUR.getValue());
        }
        version = tmpVersion;
        name = tmpName;
        organization = tmpOrg;
    }

    /**
     * Returns the application version.
     *
     * @return the version string, or {@code null} if unavailable
     */
    public String getVersion() {
        return version;
    }

    /**
     * Builds the application window title using loaded metadata.
     *
     * @return formatted title, e.g., "📱 CanScan v1.0.0.0 • Soft64.fr"
     */
    public String initializeTitle() {
        return String.format("\uD83D\uDCF1 %s v%s • %s", name, version, organization);
    }
}
