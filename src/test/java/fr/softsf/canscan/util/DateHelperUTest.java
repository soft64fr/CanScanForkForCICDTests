/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("*** Date helper tests ***")
class DateHelperUTest {
    @Test
    void givenCurrentTimeWhenNowUtcThenReturnFormattedString() {
        String result = DateHelper.INSTANCE.nowUtc();
        assertNotNull(result);
        assertTrue(result.matches("\\d{8}T\\d{6}Z"));
    }

    @Test
    void givenValidDateAndTimeWhenValidateAndGetDateAndTimeThenReturnFormattedUtcString() {
        LocalDate date = LocalDate.of(2025, 11, 21);
        LocalTime time = LocalTime.of(17, 30, 45);
        String result = DateHelper.INSTANCE.validateAndGetDateAndTime(date, time);
        assertEquals("20251121T173045Z", result);
    }

    @Test
    void givenNullDateWhenValidateAndGetDateAndTimeThenReturnEmptyString() {
        LocalTime time = LocalTime.of(10, 0);
        String result = DateHelper.INSTANCE.validateAndGetDateAndTime(null, time);
        assertEquals("", result);
    }

    @Test
    void givenNullTimeWhenValidateAndGetDateAndTimeThenReturnEmptyString() {
        LocalDate date = LocalDate.of(2025, 11, 21);
        String result = DateHelper.INSTANCE.validateAndGetDateAndTime(date, null);
        assertEquals("", result);
    }

    @Test
    void givenNullDateAndTimeWhenValidateAndGetDateAndTimeThenReturnEmptyString() {
        String result = DateHelper.INSTANCE.validateAndGetDateAndTime(null, null);
        assertEquals("", result);
    }
}
