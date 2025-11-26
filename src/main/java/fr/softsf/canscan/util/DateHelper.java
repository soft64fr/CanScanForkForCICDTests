/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Produces iCalendar-compliant UTC timestamps (RFC 5545).
 *
 * <p>All inputs are treated as UTC; no system timezone conversion is applied. Format: {@code
 * yyyyMMdd'T'HHmmss'Z'}.
 */
public enum DateHelper {
    INSTANCE;

    private static final DateTimeFormatter ICALENDAR_COMPLIANT_UTC_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    /**
     * Returns the current local date/time formatted as an iCalendar-compliant UTC timestamp
     * (yyyyMMdd'T'HHmmss'Z').
     */
    public String nowUtc() {
        return ICALENDAR_COMPLIANT_UTC_FORMATTER.format(ZonedDateTime.now());
    }

    /**
     * Formats the given local date/time as an iCalendar-compliant UTC timestamp (RFC 5545).
     *
     * @param date local date
     * @param time local time
     * @return UTC timestamp (yyyyMMdd'T'HHmmss'Z'), or "" if null
     */
    public String validateAndGetDateAndTime(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return "";
        }
        return ICALENDAR_COMPLIANT_UTC_FORMATTER.format(
                ZonedDateTime.of(date, time, ZoneOffset.UTC));
    }
}
