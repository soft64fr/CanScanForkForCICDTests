/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.model;

/**
 * Structured input for QR code generation.
 *
 * @param name full name of the contact
 * @param tel telephone number
 * @param email email address
 * @param org organization or company name
 * @param adr postal address
 * @param url website or profile URL
 * @param free free-form content for non-MECARD encoding
 */
public record QrInput(
        String name, String tel, String email, String org, String adr, String url, String free) {}
