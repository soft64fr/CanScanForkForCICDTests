/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * Utility interface to apply FlatLaf styling to LGoodDatePicker components. Provides: - Standard
 * text field height for consistent sizing. - Access to the internal JTextField of the picker. -
 * Methods to adjust preferred size and reapply FlatLaf styling from UIManager defaults.
 */
public interface IFlatLafStyledForLGoodDatePicker {

    int TEXT_FIELD_HEIGHT = new JTextField().getPreferredSize().height;

    /** Returns the internal JTextField of the picker. */
    JTextField getInternalTextField();

    /**
     * Adjusts the given dimension to use the standard text field height.
     *
     * @param original the original dimension
     * @return dimension with height set to TEXT_FIELD_HEIGHT
     */
    default Dimension computePreferredSize(Dimension original) {
        original.height = TEXT_FIELD_HEIGHT;
        return original;
    }

    /**
     * Applies FlatLaf styling to the internal text field. Sets border, colors, font, and margins
     * from UIManager defaults.
     */
    default void applyTheme() {
        JTextField textField = getInternalTextField();
        if (textField != null) {
            textField.setBorder(UIManager.getBorder("TextField.border"));
            textField.setBackground(UIManager.getColor("TextField.background"));
            textField.setForeground(UIManager.getColor("TextField.foreground"));
            textField.setCaretColor(UIManager.getColor("TextField.caretForeground"));
            textField.setSelectionColor(UIManager.getColor("TextField.selectionBackground"));
            textField.setSelectedTextColor(UIManager.getColor("TextField.selectionForeground"));
            textField.setDisabledTextColor(UIManager.getColor("TextField.inactiveForeground"));
            textField.setFont(UIManager.getFont("TextField.font"));
            Insets margin = UIManager.getInsets("TextField.margin");
            if (margin != null) {
                textField.setMargin(margin);
            }
            textField.revalidate();
            textField.repaint();
        }
    }
}
