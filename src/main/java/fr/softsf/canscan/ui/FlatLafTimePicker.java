/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.Dimension;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;

/**
 * TimePicker component with FlatLaf styling applied and maintained. Provides: - FlatLaf-based
 * colors and borders via customized TimePickerSettings. - Automatic application of UIManager
 * defaults to the internal text field. - Preferred size adjusted to match standard text field
 * height.
 */
public class FlatLafTimePicker extends TimePicker implements IFlatLafStyledForLGoodDatePicker {

    private static final String TEXT_FIELD_BACKGROUND = "TextField.background";

    /** Creates a new FlatLafTimePicker with FlatLaf styling applied. */
    public FlatLafTimePicker() {
        super(createSettings());
        applyTheme();
    }

    /** Configures TimePickerSettings with FlatLaf colors and borders. */
    private static TimePickerSettings createSettings() {
        TimePickerSettings settings = new TimePickerSettings();
        settings.setColor(
                TimePickerSettings.TimeArea.TimePickerTextInvalidTime,
                UIManager.getColor("CheckBox.icon.selectedBackground"));
        settings.setColor(
                TimePickerSettings.TimeArea.TimePickerTextValidTime,
                UIManager.getColor("TextField.foreground"));
        settings.setColor(
                TimePickerSettings.TimeArea.TextFieldBackgroundInvalidTime,
                UIManager.getColor(TEXT_FIELD_BACKGROUND));
        settings.setColor(
                TimePickerSettings.TimeArea.TextFieldBackgroundValidTime,
                UIManager.getColor(TEXT_FIELD_BACKGROUND));
        return settings;
    }

    /** Returns the internal JTextField of the TimePicker. */
    @Override
    public JTextField getInternalTextField() {
        return getComponentTimeTextField();
    }

    /** Returns preferred size with height adjusted to standard text field height. */
    @Override
    public Dimension getPreferredSize() {
        return computePreferredSize(super.getPreferredSize());
    }
}
