/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

import java.awt.Color;

/**
 * Input data structure for QR code generation and rendering.
 *
 * <p>Encapsulates all user-provided content and common configuration required to generate a QR
 * code. Supports multiple encoding profiles, including:
 *
 * <ul>
 *   <li>MECARD (structured contact information)
 *   <li>MEETING (event / meeting data)
 *   <li>FREE (arbitrary text)
 * </ul>
 *
 * <p>This record is immutable and aggregates:
 *
 * <ul>
 *   <li>Contact details (MECARD mode)
 *   <li>Meeting/event metadata (MEETING mode)
 *   <li>Free-form text (FREE mode)
 *   <li>Common customization (colors, logo, modules)
 *   <li>Size and layout options
 * </ul>
 *
 * @param availableHeightForQrCode vertical space available for rendering in the UI
 * @param currentMode encoding mode: {@link Mode#MECARD}, {@link Mode#MEET}, {@link Mode#FREE}
 *     <!-- MECARD mode -->
 * @param name full name
 * @param org organization or company
 * @param tel telephone number
 * @param email email address
 * @param adr postal address
 * @param url website or profile URL
 *     <!-- MEETING mode -->
 * @param meetTitle meeting title
 * @param meetUId unique meeting identifier
 * @param meetName organizer or meeting name
 * @param meetBeginDateTime start date and time UTC (19970715T040000Z)
 * @param meetEndDateTime end date and time UTC (19970715T040000Z)
 * @param meetLat latitude for meeting location
 * @param meetLong longitude for meeting location
 *     <!-- FREE mode -->
 * @param free free-form text content
 *     <!-- Common configuration -->
 * @param logoPath optional logo file path to embed in the QR code
 * @param size target image size in pixels
 * @param margin quiet zone in modules
 * @param ratio logo visibility ratio (0.0–1.0)
 * @param qrColor QR modules color
 * @param bgColor background color
 * @param isRoundedModules whether modules are drawn with rounded corners
 */
public record WholeFields(
        int availableHeightForQrCode,
        Mode currentMode,
        String free,
        String name,
        String org,
        String tel,
        String email,
        String adr,
        String url,
        String meetTitle,
        String meetUId,
        String meetName,
        String meetBeginDateTime,
        String meetEndDateTime,
        String meetLat,
        String meetLong,
        String logoPath,
        int size,
        int margin,
        double ratio,
        Color qrColor,
        Color bgColor,
        boolean isRoundedModules) {}
