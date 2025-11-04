/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

/**
 * Holds the encoded QR data and its associated default file name.
 *
 * @param data the QR code content to encode
 * @param defaultFileName the suggested default name for export
 */
public record QrDataResult(String data, String defaultFileName) {}
