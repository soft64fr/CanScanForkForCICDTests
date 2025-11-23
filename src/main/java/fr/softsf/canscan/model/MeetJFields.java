/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

import javax.swing.JTextField;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;

/**
 * Encapsulates all MEET fields for QR code generation.
 *
 * @param meetTitleField text field for the event summary/title
 * @param meetUIdField text field for the unique event identifier
 * @param meetNameField text field for the organizer's name
 * @param meetBeginDatePicker date picker for the event start date
 * @param meetBeginTimePicker time picker for the event start time
 * @param meetEndDatePicker date picker for the event end date
 * @param meetEndTimePicker time picker for the event end time
 * @param meetLatField text field for latitude (GEO/LOCATION)
 * @param meetLongField text field for longitude (GEO/LOCATION)
 */
public record MeetJFields(
        JTextField meetTitleField,
        JTextField meetUIdField,
        JTextField meetNameField,
        DatePicker meetBeginDatePicker,
        TimePicker meetBeginTimePicker,
        DatePicker meetEndDatePicker,
        TimePicker meetEndTimePicker,
        JTextField meetLatField,
        JTextField meetLongField) {}
