/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.image.BufferedImage;

/**
 * Thread-safe singleton to hold a shared QR code {@link BufferedImage}. All access is synchronized
 * for thread safety.
 */
public enum QrCodeBufferedImage {
    INSTANCE;

    private transient BufferedImage qrOriginal;
    private final Object imageLock = new Object();

    /**
     * @return the current QR code image, or null if not set.
     */
    public BufferedImage getQrOriginal() {
        synchronized (imageLock) {
            return qrOriginal;
        }
    }

    /**
     * Updates the QR code image. Should only be called by internal components.
     *
     * @param newImage the new image, can be null.
     */
    public void updateQrOriginal(BufferedImage newImage) {
        synchronized (imageLock) {
            this.qrOriginal = newImage;
        }
    }

    /**
     * Releases the current QR code image and its resources. Must be called on the EDT if Swing
     * components are involved.
     */
    public synchronized void freeQrOriginal() {
        synchronized (imageLock) {
            if (qrOriginal != null) {
                qrOriginal.flush();
                qrOriginal = null;
            }
        }
    }
}
