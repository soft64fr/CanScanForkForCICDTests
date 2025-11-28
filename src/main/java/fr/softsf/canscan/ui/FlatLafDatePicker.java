/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

/**
 * DatePicker component with FlatLaf styling applied and maintained. Provides: - FlatLaf-based
 * colors and borders via customized DatePickerSettings. - Automatic application of UIManager
 * defaults to the internal text field and calendar. - Preferred size adjusted to match standard
 * text field height.
 */
public class FlatLafDatePicker extends DatePicker implements IFlatLafStyledForLGoodDatePicker {

    private static final String BUTTON_FOREGROUND = "Button.foreground";
    private static final String BUTTON_BACKGROUND = "Button.background";
    private static final String TEXT_FIELD_BACKGROUND = "TextField.background";
    private static final String TEXT_FIELD_FOREGROUND = "TextField.foreground";

    /** Creates a new FlatLafDatePicker with FlatLaf styling applied. */
    public FlatLafDatePicker() {
        super(createSettings());
        applyTheme();
    }

    /** Configures DatePickerSettings with FlatLaf colors and borders. */
    private static DatePickerSettings createSettings() {
        DatePickerSettings settings = new DatePickerSettings();
        settings.setColor(
                DatePickerSettings.DateArea.DatePickerTextInvalidDate,
                UIManager.getColor("CheckBox.icon.selectedBackground"));
        settings.setColor(
                DatePickerSettings.DateArea.DatePickerTextValidDate,
                UIManager.getColor(TEXT_FIELD_FOREGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.TextFieldBackgroundInvalidDate,
                UIManager.getColor(TEXT_FIELD_BACKGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.TextFieldBackgroundValidDate,
                UIManager.getColor(TEXT_FIELD_BACKGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.BackgroundOverallCalendarPanel,
                UIManager.getColor("Panel.background"));
        settings.setColorBackgroundWeekdayLabels(UIManager.getColor("Panel.background"), false);
        settings.setColor(
                DatePickerSettings.DateArea.CalendarTextWeekdays,
                UIManager.getColor(TEXT_FIELD_FOREGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.CalendarTextNormalDates,
                UIManager.getColor(TEXT_FIELD_FOREGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.CalendarBackgroundNormalDates,
                UIManager.getColor(TEXT_FIELD_BACKGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.BackgroundClearLabel,
                UIManager.getColor(TEXT_FIELD_BACKGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.TextClearLabel, UIManager.getColor(BUTTON_FOREGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.BackgroundTodayLabel,
                UIManager.getColor(TEXT_FIELD_BACKGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.TextTodayLabel, UIManager.getColor(BUTTON_FOREGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.BackgroundMonthAndYearMenuLabels,
                UIManager.getColor(TEXT_FIELD_BACKGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.TextMonthAndYearMenuLabels,
                UIManager.getColor(BUTTON_FOREGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.BackgroundCalendarPanelLabelsOnHover,
                UIManager.getColor(BUTTON_BACKGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.CalendarBorderSelectedDate,
                UIManager.getColor(BUTTON_BACKGROUND));
        settings.setColor(
                DatePickerSettings.DateArea.CalendarBackgroundSelectedDate,
                UIManager.getColor(BUTTON_BACKGROUND));
        settings.setBorderPropertiesList(new ArrayList<>());
        settings.setBorderCalendarPopup(UIManager.getBorder("TextField.border"));
        return settings;
    }

    /** Returns the internal JTextField of the DatePicker. */
    @Override
    public JTextField getInternalTextField() {
        return getComponentDateTextField();
    }

    /** Returns preferred size with height adjusted to standard text field height. */
    @Override
    public Dimension getPreferredSize() {
        return computePreferredSize(super.getPreferredSize());
    }
}
