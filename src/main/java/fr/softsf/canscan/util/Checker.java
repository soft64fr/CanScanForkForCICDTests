/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import java.awt.Component;

import org.apache.commons.lang3.StringUtils;

import fr.softsf.canscan.ui.Popup;

/**
 * Utility for validating arguments and reporting null or blank values.
 *
 * <p>Provides instance and static methods for null and blank checks with standardized error
 * dialogs. Designed for use in defensive programming and audit-friendly workflows.
 */
public enum Checker {
    INSTANCE;

    /**
     * Checks if the given object is null within an instance context and displays an error dialog if
     * so.
     *
     * <p>Delegates to {@link #checkNullOrBlankInternal(Object, String, String, Component)}, using
     * the current instance as the parent component.
     *
     * @param obj the object to validate
     * @param methodName the calling method's name (for context in the dialog)
     * @param name the object's name (used in the dialog)
     * @return true if the object is null, false otherwise
     */
    public boolean checkNPE(Object obj, String methodName, String name) {
        return checkNullOrBlankInternal(obj, methodName, name, null);
    }

    /**
     * Checks if the given object is null in a static context and displays an error dialog if so.
     *
     * <p>Delegates to {@link #checkNullOrBlankInternal(Object, String, String, Component)} with a
     * null parent.
     *
     * @param obj the object to validate
     * @param methodName the calling method's name (for context in the dialog)
     * @param name the object's name (used in the dialog)
     * @return true if the object is null, false otherwise
     */
    public static boolean checkStaticNPE(Object obj, String methodName, String name) {
        return checkNullOrBlankInternal(obj, methodName, name, null);
    }

    /**
     * Validates that the given object is non-null and, if a String, not blank. Displays a
     * standardized error dialog if invalid.
     *
     * @param obj the object to validate
     * @param methodName the name of the calling method (used in the dialog)
     * @param name the name of the parameter (used in the dialog)
     * @param parent the parent component for the dialog (can be {@code null})
     * @return {@code true} if the object is invalid; {@code false} otherwise
     */
    private static boolean checkNullOrBlankInternal(
            Object obj, String methodName, String name, Component parent) {
        if (obj == null) {
            Popup.INSTANCE.showArgumentErrorDialog(parent, methodName, name, "null");
            return true;
        } else if (obj instanceof String s && StringUtils.isBlank(s)) {
            Popup.INSTANCE.showArgumentErrorDialog(parent, methodName, name, "blank");
            return true;
        }
        return false;
    }
}
