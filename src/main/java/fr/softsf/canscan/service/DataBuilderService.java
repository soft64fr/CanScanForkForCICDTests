/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.service;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import fr.softsf.canscan.constant.StringConstants;
import fr.softsf.canscan.model.EncodedData;
import fr.softsf.canscan.model.Mode;
import fr.softsf.canscan.model.WholeFields;
import fr.softsf.canscan.util.Checker;
import fr.softsf.canscan.util.DateHelper;

/**
 * Service for generating QR code content and default filenames.
 *
 * <p>Supports MECARD MEET FREE modes with input validation and standardized formatting. Returns
 * structured results for encoding and export.
 */
public enum DataBuilderService {
    INSTANCE;

    private static final String APPEND_FIELD_WITH_SPECIFIED_SEPARATOR =
            "appendFieldWithSpecifiedSeparator";

    /**
     * Builds the QR content and default filename based on the selected mode.
     *
     * <p>Returns {@code null} if {@code mode} or {@code input} is {@code null}.
     *
     * @param mode the QR encoding mode (MECARD or MEET or FREE)
     * @param input the structured input data
     * @return a {@link EncodedData} containing the encoded content and default filename, or {@code
     *     null} if input is invalid
     */
    public EncodedData buildData(Mode mode, WholeFields input) {
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
                yield new EncodedData(data, "codeqr_mecard.png");
            }
            case MEET -> {
                String data =
                        buildMeet(
                                input.meetTitle().trim(),
                                input.meetUId().trim(),
                                input.meetBeginDateTime().trim(),
                                input.meetEndDateTime().trim(),
                                input.meetName().trim(),
                                input.meetLat().trim(),
                                input.meetLong().trim());
                Objects.requireNonNull(data, "data must not be null");
                yield new EncodedData(data, "codeqr_calendar.png");
            }
            case FREE -> new EncodedData(input.free().trim(), "codeqr_free.png");
        };
    }

    /**
     * Builds an iCalendar (VCALENDAR) formatted string representing a meeting (VEVENT).
     *
     * <p>Skips blank or null fields. Returns an empty string if all required fields are blank.
     * Latitude and longitude are optional; if provided, they are included in the GEO and LOCATION
     * fields.
     *
     * @param meetTitle the meeting title (SUMMARY)
     * @param meetUId the unique meeting identifier (UID)
     * @param meetBeginDateTime the meeting start date/time (DTSTART)
     * @param meetEndDateTime the meeting end date/time (DTEND)
     * @param meetName the organizer name (ORGANIZER); optional
     * @param meetLat the meeting latitude; optional
     * @param meetLong the meeting longitude; optional
     * @return an iCalendar (VCALENDAR) string representing the meeting, or empty if all fields are
     *     blank
     */
    public String buildMeet(
            String meetTitle,
            String meetUId,
            String meetBeginDateTime,
            String meetEndDateTime,
            String meetName,
            String meetLat,
            String meetLong) {
        if (StringUtils.isBlank(
                meetTitle
                        + meetUId
                        + meetBeginDateTime
                        + meetEndDateTime
                        + meetName
                        + meetLat
                        + meetLat)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String separator = System.lineSeparator();
        appendFieldWithSpecifiedSeparator(sb, "BEGIN", "VCALENDAR", separator);
        appendFieldWithSpecifiedSeparator(sb, "VERSION", "2.0", separator);
        appendFieldWithSpecifiedSeparator(
                sb,
                "PRODID",
                String.format("-//Soft64.fr//CanScan %s//FR", StringConstants.VERSION.getValue()),
                separator);
        appendFieldWithSpecifiedSeparator(sb, "BEGIN", "VEVENT", separator);
        appendFieldWithSpecifiedSeparator(sb, "UID", meetUId, separator);
        if (StringUtils.isNotBlank(meetName)) {
            appendFieldWithSpecifiedSeparator(sb, "ORGANIZER", meetName, separator);
        }
        appendFieldWithSpecifiedSeparator(sb, "DTSTAMP", DateHelper.INSTANCE.nowUtc(), separator);
        appendFieldWithSpecifiedSeparator(sb, "DTSTART", meetBeginDateTime, separator);
        appendFieldWithSpecifiedSeparator(sb, "DTEND", meetEndDateTime, separator);
        appendFieldWithSpecifiedSeparator(sb, "SUMMARY", meetTitle, separator);
        if (StringUtils.isNotBlank(meetLat) && StringUtils.isNotBlank(meetLong)) {
            appendFieldWithSpecifiedSeparator(
                    sb, "GEO", String.format("%s;%s", meetLat, meetLong), separator);
            appendFieldWithSpecifiedSeparator(
                    sb,
                    "LOCATION",
                    String.format(
                            "https://www.openstreetmap.org/?mlat=%s&mlon=%s&zoom=15",
                            meetLat, meetLong),
                    separator);
        }
        appendFieldWithSpecifiedSeparator(sb, "END", "VEVENT", separator);
        appendFieldWithSpecifiedSeparator(sb, "END", "VCALENDAR", separator);
        return sb.toString();
    }

    /**
     * Builds a MECARD-formatted string from the provided contact fields.
     *
     * <p>Any blank or null fields are skipped. Returns a valid MECARD string or an empty string if
     * all fields are blank.
     *
     * @param name full name
     * @param tel telephone number
     * @param email email address
     * @param org organization or company
     * @param adr postal address
     * @param url website or profile URL
     * @return a MECARD string representing the contact information, or empty if all fields are
     *     blank
     */
    public String buildMecard(
            String name, String tel, String email, String org, String adr, String url) {
        if (StringUtils.isBlank(name + tel + email + org + adr + url)) {
            return "";
        }
        StringBuilder sb = new StringBuilder("MECARD:");
        appendFieldWithSpecifiedSeparator(sb, "N", name, ";");
        appendFieldWithSpecifiedSeparator(sb, "TEL", tel, ";");
        appendFieldWithSpecifiedSeparator(sb, "EMAIL", email, ";");
        appendFieldWithSpecifiedSeparator(sb, "ORG", org, ";");
        appendFieldWithSpecifiedSeparator(sb, "ADR", adr, ";");
        appendFieldWithSpecifiedSeparator(sb, "URL", url, ";");
        sb.append(';');
        return sb.toString();
    }

    /**
     * Appends a MECARD field in the format {@code fieldName:value<separator>} if the value is
     * non-blank.
     *
     * <p>If {@code sb} or {@code fieldName} is {@code null}, or if {@code value} is blank, the
     * method does nothing.
     *
     * @param sb the target {@link StringBuilder}
     * @param fieldName the MECARD field name (e.g., {@code "N"}, {@code "TEL"})
     * @param value the field value to append; ignored if blank
     * @param separator the separator to append after the field (e.g., ";", or {@link
     *     System#lineSeparator()})
     */
    private void appendFieldWithSpecifiedSeparator(
            StringBuilder sb, String fieldName, String value, String separator) {
        if (Checker.INSTANCE.checkNPE(sb, APPEND_FIELD_WITH_SPECIFIED_SEPARATOR, "sb")
                || Checker.INSTANCE.checkNPE(
                        fieldName, APPEND_FIELD_WITH_SPECIFIED_SEPARATOR, "fieldName")
                || StringUtils.isBlank(value)
                || separator == null) {
            return;
        }
        sb.append(fieldName).append(':').append(value).append(separator);
    }
}
