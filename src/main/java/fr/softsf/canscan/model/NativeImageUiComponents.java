/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.github.lgooddatepicker.components.TimePicker;

/**
 * Encapsulates GUI components used for simulating Native Image configuration behavior in UI tests
 * and previews.
 *
 * <p>Provides a method to assign unique names to each component to allow test frameworks to
 * reliably identify them.
 */
public record NativeImageUiComponents(
        JTextField nameField,
        JButton browseButton,
        JSlider ratioSlider,
        JButton qrColorButton,
        JRadioButton freeRadio,
        JRadioButton meetRadio,
        JTextArea freeField,
        TimePicker meetBeginTimePicker) {

    /**
     * Assigns unique names to all non-null components for automated tests.
     *
     * <p>This ensures that each component can be reliably identified by UI testing frameworks or
     * native image simulators.
     */
    public void assignNames() {
        if (nameField != null) {
            nameField.setName("nameField");
        }
        if (browseButton != null) {
            browseButton.setName("browseButton");
        }
        if (ratioSlider != null) {
            ratioSlider.setName("ratioSlider");
        }
        if (qrColorButton != null) {
            qrColorButton.setName("qrColorButton");
        }
        if (freeRadio != null) {
            freeRadio.setName("freeRadio");
        }
        if (meetRadio != null) {
            meetRadio.setName("meetRadio");
        }
        if (freeField != null) {
            freeField.setName("freeField");
        }
        if (meetBeginTimePicker != null) {
            meetBeginTimePicker.setName("meetBeginTimePicker");
        }
    }
}
