/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.Component;
import javax.swing.JOptionPane;

import fr.softsf.canscan.util.Checker;
import fr.softsf.canscan.util.FrameHelper;

/**
 * Utility for displaying standardized dialogs.
 *
 * <p>Provides error, confirmation, and custom message dialogs for UI feedback and validation
 * reporting.
 */
public enum MyPopup {
    INSTANCE;

    private static final String SHOW_ARGUMENT_ERROR_DIALOG = "showArgumentErrorDialog";

    /**
     * Displays a standardized error dialog for invalid arguments. Returns immediately if any
     * parameter is null or invalid.
     *
     * @param parent the parent component for the dialog (can be {@code null})
     * @param methodName the name of the calling method
     * @param name the name of the invalid parameter
     * @param typeDescription a short description of the invalidity (e.g., {@code "null"}, {@code
     *     "blank"})
     */
    public void showArgumentErrorDialog(
            Component parent, String methodName, String name, String typeDescription) {
        if (Checker.INSTANCE.checkNPE(methodName, SHOW_ARGUMENT_ERROR_DIALOG, "methodName")
                || Checker.INSTANCE.checkNPE(name, SHOW_ARGUMENT_ERROR_DIALOG, "name")
                || Checker.INSTANCE.checkNPE(
                        typeDescription, SHOW_ARGUMENT_ERROR_DIALOG, "typeDescription")) {
            return;
        }
        String prefix = "❌ Argument " + typeDescription + " detecté dans " + methodName + ":\n";
        String message = name + " est " + typeDescription + ".";
        String title = "Erreur d'argument";
        JOptionPane.showMessageDialog(parent, prefix + message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays a dialog with a custom prefix and message.
     *
     * @param prefix the message prefix (e.g. "❌ QR Code generation error:\n")
     * @param message the details to display
     * @param title the dialog title (defaults to "Soft64.fr" if null or blank)
     */
    public void showDialog(String prefix, String message, String title) {
        if (message == null || message.isBlank()) {
            message = "\nAucun détail disponible.";
        }
        if (prefix == null) {
            prefix = "";
        }
        if (title == null || title.isBlank()) {
            title = "Soft64.fr";
        }
        JOptionPane.showMessageDialog(
                FrameHelper.INSTANCE.getParentFrame(),
                prefix.isBlank() ? "" : prefix + " " + message,
                title,
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Displays a modal Yes/No confirmation dialog with a warning icon and the given message.
     *
     * @param message the message to display in the dialog
     * @return {@link JOptionPane#YES_OPTION} or {@link JOptionPane#NO_OPTION} based on user
     *     selection
     */
    public int showYesNoConfirmDialog(String message) {
        Checker.INSTANCE.checkNPE(message, "showYesNoConfirmDialog", "message");
        return JOptionPane.showConfirmDialog(
                FrameHelper.INSTANCE.getParentFrame(),
                message,
                "Soft64.fr",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
    }
}
