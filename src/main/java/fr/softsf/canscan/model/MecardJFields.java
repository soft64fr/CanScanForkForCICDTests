/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

import javax.swing.JTextField;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Encapsulates all MECARD input fields for QR code generation.
 *
 * <p>NOTE: This record intentionally stores direct references to mutable UI JTextField. Copying
 * these components is impractical, and they are expected to be treated as shared UI state.
 *
 * @param nameField text field for the contact name
 * @param orgField text field for the organization
 * @param phoneField text field for the phone number
 * @param emailField text field for the email
 * @param adrField text field for the postal address
 * @param urlField text field for the website URL
 */
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public record MecardJFields(
        JTextField nameField,
        JTextField orgField,
        JTextField phoneField,
        JTextField emailField,
        JTextField adrField,
        JTextField urlField) {}
