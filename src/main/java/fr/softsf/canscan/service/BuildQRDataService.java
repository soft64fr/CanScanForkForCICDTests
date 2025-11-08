/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.service;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import fr.softsf.canscan.model.Mode;
import fr.softsf.canscan.model.QrDataResult;
import fr.softsf.canscan.model.QrInput;
import fr.softsf.canscan.util.Checker;

/**
 * Service for generating QR code content and default filenames.
 *
 * <p>Supports MECARD and FREE modes with input validation and standardized formatting. Returns
 * structured results for encoding and export.
 */
public enum BuildQRDataService {
    INSTANCE;

    private static final String APPEND_FIELD = "appendField";

    /**
     * Builds the QR content and default filename based on the selected mode.
     *
     * <p>Returns {@code null} if {@code mode} or {@code input} is {@code null}.
     *
     * @param mode the QR encoding mode (MECARD or FREE)
     * @param input the structured input data
     * @return a {@link QrDataResult} containing the encoded content and default filename, or {@code
     *     null} if input is invalid
     */
    public QrDataResult buildQrData(Mode mode, QrInput input) {
        if (Checker.INSTANCE.checkNPE(mode, "buildQrData", "mode")
                || Checker.INSTANCE.checkNPE(input, "buildQrData", "input")) {
            return null;
        }
        return switch (mode) {
            case MECARD -> {
                String data =
                        buildMecard(
                                input.name().trim(),
                                input.tel().trim(),
                                input.email().trim(),
                                input.org().trim(),
                                input.adr().trim(),
                                input.url().trim());
                Objects.requireNonNull(data, "data must not be null");
                if (data.equals("MECARD:;")) {
                    data = "";
                }
                yield new QrDataResult(data, "codeqr_mecard.png");
            }
            case FREE -> new QrDataResult(input.free().trim(), "codeqr_free.png");
        };
    }

    /**
     * Builds a MECARD-formatted string from the provided contact fields.
     *
     * <p>Skips blank or null fields. Returns a valid MECARD string, even if partially populated.
     *
     * @param name full name
     * @param tel telephone number
     * @param email email address
     * @param org organization or company
     * @param adr postal address
     * @param url website or profile URL
     * @return a MECARD string representing the contact information
     */
    public String buildMecard(
            String name, String tel, String email, String org, String adr, String url) {
        StringBuilder sb = new StringBuilder("MECARD:");
        appendField(sb, "N", name);
        appendField(sb, "TEL", tel);
        appendField(sb, "EMAIL", email);
        appendField(sb, "ORG", org);
        appendField(sb, "ADR", adr);
        appendField(sb, "URL", url);
        sb.append(';');
        return sb.toString();
    }

    /**
     * Appends a field to the MECARD string if the value is non-null and non-blank.
     *
     * @param sb the target {@link StringBuilder}
     * @param fieldName the MECARD field name (e.g., {@code "N"}, {@code "TEL"})
     * @param value the field value to append
     */
    private void appendField(StringBuilder sb, String fieldName, String value) {
        if (Checker.INSTANCE.checkNPE(sb, APPEND_FIELD, "sb")
                || Checker.INSTANCE.checkNPE(fieldName, APPEND_FIELD, "fieldName")
                || StringUtils.isBlank(value)) {
            return;
        }
        sb.append(fieldName).append(':').append(value).append(';');
    }
}
