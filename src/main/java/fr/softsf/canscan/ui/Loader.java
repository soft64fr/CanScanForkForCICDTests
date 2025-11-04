/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JLabel;
import javax.swing.Timer;

/**
 * Utility for displaying and managing a QR code loading animation. Must be initialized once and
 * used from the Event Dispatch Thread (EDT).
 */
public enum Loader {
    INSTANCE;

    private static final int WAIT_ICON_DELAY_MS = 100;
    private static final String[] WAIT_FRAMES = {"◒", "◐", "◓", "◑"};

    private JLabel qrCodeLabel;

    private transient Timer waitIconTimer;
    private transient FontMetrics qrCodeLabelFontMetrics;
    private transient Font qrCodeLabelFont;

    /** Injects UI dependencies. Must be called once during initialization. */
    public void init(JLabel qrCodeLabel) {
        this.qrCodeLabel = qrCodeLabel;
        this.qrCodeLabelFont = qrCodeLabel.getFont();
        this.qrCodeLabelFontMetrics = qrCodeLabel.getFontMetrics(qrCodeLabel.getFont());
    }

    /** Displays the waiting icon and scales its font to fit the label height. */
    public void startAndAdjustWaitIcon() {
        stopWaitIcon();
        float scale = (float) qrCodeLabel.getHeight() / qrCodeLabelFontMetrics.getHeight();
        Font scaledFont = qrCodeLabelFont.deriveFont(qrCodeLabelFont.getSize2D() * scale);
        qrCodeLabel.setFont(scaledFont);
        final int[] step = {0};
        qrCodeLabel.setText(WAIT_FRAMES[step[0]]);
        waitIconTimer =
                new Timer(
                        WAIT_ICON_DELAY_MS,
                        e -> {
                            step[0] = (step[0] + 1) % WAIT_FRAMES.length;
                            qrCodeLabel.setText(WAIT_FRAMES[step[0]]);
                        });
        waitIconTimer.start();
    }

    /** Stops the wait icon animation and clears the label. */
    public void stopWaitIcon() {
        disposeWaitIconTimer();
        qrCodeLabel.setText("");
    }

    /** Stops and disposes the wait icon timer. */
    public void disposeWaitIconTimer() {
        if (waitIconTimer != null) {
            waitIconTimer.stop();
            waitIconTimer = null;
        }
    }
}
