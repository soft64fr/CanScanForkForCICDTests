/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("*** Data builder service tests ***")
class DataBuilderServiceUTest {

    @Test
    void givenAllMecardFieldsWhenBuildMecardThenReturnValidString() {
        String result =
                DataBuilderService.INSTANCE.buildMecard(
                        "John Doe",
                        "0123456789",
                        "john@example.com",
                        "Soft64",
                        "1 rue de Paris",
                        "https://soft64.fr");
        assertNotNull(result);
        assertTrue(result.startsWith("MECARD:"));
        assertTrue(result.contains("N:John Doe;"));
        assertTrue(result.contains("TEL:0123456789;"));
        assertTrue(result.contains("EMAIL:john@example.com;"));
        assertTrue(result.endsWith(";"));
    }

    @Test
    void givenBlankMecardFieldsWhenBuildMecardThenReturnEmptyString() {
        String result = DataBuilderService.INSTANCE.buildMecard("", "", "", "", "", "");
        assertEquals("", result);
    }

    @Test
    void givenMeetFieldsWithGeoWhenBuildMeetThenIncludeGeoAndLocation() {
        String result =
                DataBuilderService.INSTANCE.buildMeet(
                        "Meeting title",
                        "12345",
                        "20250101T090000Z",
                        "20250101T100000Z",
                        "Organizer",
                        "48.8566",
                        "2.3522");
        assertNotNull(result);
        assertTrue(result.contains("BEGIN:VEVENT"));
        assertTrue(result.contains("SUMMARY:Meeting title"));
        assertTrue(result.contains("UID:12345"));
        assertTrue(result.contains("GEO:48.8566;2.3522"));
        assertTrue(
                result.contains(
                        "LOCATION:https://www.openstreetmap.org/?mlat=48.8566&mlon=2.3522&zoom=15"));
        assertTrue(result.contains("END:VEVENT"));
    }

    @Test
    void givenMeetFieldsWithoutGeoWhenBuildMeetThenSkipGeoAndLocation() {
        String result =
                DataBuilderService.INSTANCE.buildMeet(
                        "Meeting title",
                        "12345",
                        "20250101T090000Z",
                        "20250101T100000Z",
                        "Organizer",
                        "",
                        "");

        assertNotNull(result);
        assertTrue(result.contains("SUMMARY:Meeting title"));
        assertFalse(result.contains("GEO:"));
        assertFalse(result.contains("LOCATION:"));
    }

    @Test
    void givenBlankMeetFieldsWhenBuildMeetThenReturnEmptyString() {
        String result = DataBuilderService.INSTANCE.buildMeet("", "", "", "", "", "", "");
        assertEquals("", result);
    }
}
