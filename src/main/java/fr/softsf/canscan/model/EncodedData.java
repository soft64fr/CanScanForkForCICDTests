/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

/**
 * Encapsulates the encoded QR data and the suggested default file name for saving.
 *
 * @param data the text input provided by the user
 * @param defaultFileName the suggested default file name (defined by the MODE)
 */
public record EncodedData(String data, String defaultFileName) {}
