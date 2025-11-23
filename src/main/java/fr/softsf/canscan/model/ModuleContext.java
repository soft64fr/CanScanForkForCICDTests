/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

/**
 * Context for QR module rendering, including configuration, matrix dimensions, and logo area.
 *
 * @param config QR code configuration
 * @param matrixWidth matrix width
 * @param matrixHeight matrix height
 * @param whiteBoxX logo area X coordinate
 * @param whiteBoxY logo area Y coordinate
 * @param whiteBoxSize logo area size
 */
public record ModuleContext(
        CommonFields config,
        int matrixWidth,
        int matrixHeight,
        int whiteBoxX,
        int whiteBoxY,
        int whiteBoxSize) {}
