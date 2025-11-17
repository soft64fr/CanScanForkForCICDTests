/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

import javax.swing.JTextField;

/**
 * Encapsulates all MECARD input fields for QR code generation.
 *
 * @param nameField text field for the contact name
 * @param orgField text field for the organization
 * @param phoneField text field for the phone number
 * @param emailField text field for the email
 * @param adrField text field for the postal address
 * @param urlField text field for the website URL
 */
public record MecardJFields(
        JTextField nameField,
        JTextField orgField,
        JTextField phoneField,
        JTextField emailField,
        JTextField adrField,
        JTextField urlField) {}
