/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.event.MouseEvent;
import javax.swing.JTextField;

/**
 * Custom text field implementation designed to extend standard {@link JTextField} functionality by
 * intercepting and consuming mouse events with a click count greater than two. This prevents UI
 * layout freezes and excessive Event Dispatch Thread saturation caused by rendering selection
 * bounds on extremely long, continuous, non-whitespace character strings.
 */
public class MyTextField extends JTextField {

    /**
     * Constructs a new text field with the specified initial text and columns.
     *
     * @param text the initial text to display
     * @param columns the number of columns to use to calculate preferred width
     */
    public MyTextField(String text, int columns) {
        super(text, columns);
    }

    /**
     * Constructs a new text field with the specified number of columns.
     *
     * @param columns the number of columns to use to calculate preferred width
     */
    public MyTextField(int columns) {
        super(columns);
    }

    /**
     * Processes mouse events to intercept and drop click counts greater than two.
     *
     * @param e the mouse event to process
     */
    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (e.getClickCount() > 2) {
            e.consume();
            return;
        }
        super.processMouseEvent(e);
    }
}
