/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.Dimension;
import javax.swing.JTextField;

import com.github.lgooddatepicker.components.TimePicker;

/**
 * TimePicker component with FlatLaf styling applied and automatically maintained.
 *
 * <p>The internal text field is styled according to UIManager defaults, and a listener ensures the
 * styling is reapplied when font, background, or foreground changes.
 */
public class FlatLafTimePicker extends TimePicker implements IFlatLafStyledForLGoodDatePicker {

    /** Creates a new FlatLafTimePicker with styling applied and listener installed. */
    public FlatLafTimePicker() {
        super();
        applyTheme();
        installThemeListener();
    }

    /**
     * Returns the internal JTextField used by the TimePicker.
     *
     * @return the TimePicker's internal text field
     */
    @Override
    public JTextField getInternalTextField() {
        return getComponentTimeTextField();
    }

    /**
     * Returns the preferred size with the height adjusted to match a standard text field.
     *
     * @return the preferred dimension with adjusted height
     */
    @Override
    public Dimension getPreferredSize() {
        return computePreferredSize(super.getPreferredSize());
    }
}
