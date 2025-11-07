/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

import java.awt.Color;

/**
 * Comprehensive input data structure for QR code generation and rendering.
 *
 * <p>Encapsulates all user-provided data and configuration parameters required to generate, style,
 * and display a QR code. Supports both MECARD (structured contact) and FREE (arbitrary text)
 * encoding modes.
 *
 * <p>This record serves as an immutable data transfer object (DTO) that aggregates:
 *
 * <ul>
 *   <li>Contact information (MECARD mode)
 *   <li>Free-form text content (FREE mode)
 *   <li>Visual customization (colors, modules, logo)
 *   <li>Size and layout constraints
 * </ul>
 *
 * @param availableHeightForQrCode the vertical space (in pixels) available for rendering the QR
 *     code in the UI; used to calculate optimal display size
 * @param currentMode the encoding mode: {@link Mode#MECARD} for structured contact data or {@link
 *     Mode#FREE} for arbitrary text content
 * @param free free-form text content for non-MECARD encoding; used when {@code currentMode} is
 *     {@link Mode#FREE}; can be any string (URL, message, JSON, etc.)
 * @param name full name of the contact (MECARD mode); typically in "LastName, FirstName" format;
 *     ignored in FREE mode
 * @param org organization or company name associated with the contact (MECARD mode); represents the
 *     business or employer; ignored in FREE mode
 * @param tel telephone number in international or local format (MECARD mode); should follow E.164
 *     format when possible (e.g., "+33123456789"); ignored in FREE mode
 * @param email email address of the contact (MECARD mode); must be a valid email format (e.g.,
 *     "user@example.com"); ignored in FREE mode
 * @param adr postal address of the contact (MECARD mode); can include street, city, postal code,
 *     and country; typically comma or newline-separated; ignored in FREE mode
 * @param url website or profile URL (MECARD mode); can be a personal site, LinkedIn profile,
 *     portfolio, or social media link; must be a valid URL format; ignored in FREE mode
 * @param logoPath file system path to an optional logo image to embed in the QR code center;
 *     supports PNG, JPG, JPEG formats; if null or empty, no logo is embedded; logo size is
 *     automatically scaled based on {@code ratio}
 * @param size the target dimension (width and height) of the generated QR code image in pixels;
 * @param margin the outer margin (quiet zone) around the QR code in modules (QR matrix units);
 *     valid range: 0-10; values below 4 may cause scanning issues on some readers; recommended: 3-4
 *     for reliable detection
 * @param ratio the logo visibility ratio as a decimal (0.0-1.0); determines the proportion of the
 *     QR code center occupied by the logo; typical range: 0.2-0.3; values above 0.3 may compromise
 *     scannability despite error correction
 * @param qrColor the color of the QR code modules (dark pixels); must provide sufficient contrast
 *     with {@code bgColor}; default: {@link Color#BLACK}; custom colors allowed but high contrast
 *     is essential for reliable scanning
 * @param bgColor the background color (light pixels) of the QR code; must provide sufficient
 *     contrast with {@code qrColor}; default: {@link Color#WHITE}; custom colors allowed but high
 *     contrast is essential for reliable scanning
 * @param isRoundedModules whether to render QR code modules with rounded corners instead of square
 *     pixels; {@code true} for aesthetic rounded style, {@code false} for traditional square
 *     modules; rounded modules may reduce scannability on some readers but are generally supported
 *     with high error correction
 */
public record QrInput(
        int availableHeightForQrCode,
        Mode currentMode,
        String free,
        String name,
        String org,
        String tel,
        String email,
        String adr,
        String url,
        String logoPath,
        int size,
        int margin,
        double ratio,
        Color qrColor,
        Color bgColor,
        boolean isRoundedModules) {}
