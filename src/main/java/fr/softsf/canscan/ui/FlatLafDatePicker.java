/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
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
 * DatePicker component with FlatLaf styling applied and automatically maintained.
 *
 * <p>The internal text field and calendar colors are styled according to UIManager defaults. A
 * listener ensures styling is reapplied when font, background, or foreground changes.
 *
 * <p><b>Note:</b> The thin border shown when hovering over calendar labels (days) is applied
 * internally by LGoodDatePicker and cannot be removed via the public API. Only the hover background
 * color can be customized.
 */
public class FlatLafDatePicker extends DatePicker implements IFlatLafStyledForLGoodDatePicker {

    private static final String TEXT_FIELD_BACKGROUND = "TextField.background";
    private static final String BUTTON_FOREGROUND = "Button.foreground";
    private static final String BUTTON_BACKGROUND = "Button.background";

    /** Creates a new FlatLafDatePicker with styling applied and listener installed. */
    public FlatLafDatePicker() {
        super(createSettings());
        applyTheme();
        installThemeListener();
    }

    /** Prepares DatePickerSettings with FlatLaf colors and borders. */
    private static DatePickerSettings createSettings() {
        DatePickerSettings settings = new DatePickerSettings();
        settings.setColor(
                DatePickerSettings.DateArea.BackgroundOverallCalendarPanel,
                UIManager.getColor("Panel.background"));
        settings.setColorBackgroundWeekdayLabels(UIManager.getColor("Panel.background"), false);
        settings.setColor(
                DatePickerSettings.DateArea.CalendarTextWeekdays,
                UIManager.getColor("TextField.foreground"));
        settings.setColor(
                DatePickerSettings.DateArea.CalendarTextNormalDates,
                UIManager.getColor("TextField.foreground"));
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

    @Override
    public JTextField getInternalTextField() {
        return getComponentDateTextField();
    }

    @Override
    public Dimension getPreferredSize() {
        return computePreferredSize(super.getPreferredSize());
    }
}
