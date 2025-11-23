/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

import java.awt.Color;
import java.io.File;

/** Common form fields used to configure QR code generation. */
public record CommonFields(
        File logoFile,
        int size,
        double imageRatio,
        Color qrColor,
        Color bgColor,
        boolean roundedModules,
        int margin) {}
