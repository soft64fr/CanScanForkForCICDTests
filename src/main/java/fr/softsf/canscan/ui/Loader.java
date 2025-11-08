/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.Timer;

/**
 * Manages an animated loading indicator for a JLabel component. Must be used from the Event
 * Dispatch Thread (EDT).
 */
public class Loader {

    private static final int WAIT_ICON_DELAY_MS = 100;
    private static final String[] WAIT_FRAMES = {"◒", "◐", "◓", "◑"};

    private final JLabel qrCodeLabel;
    private final FontMetrics qrCodeLabelFontMetrics;
    private final Font qrCodeLabelFont;
    private Timer waitIconTimer;

    /**
     * Creates a loader for the specified label.
     *
     * @param qrCodeLabel the label to display the animation
     * @throws NullPointerException if qrCodeLabel is null
     */
    public Loader(JLabel qrCodeLabel) {
        this.qrCodeLabel = Objects.requireNonNull(qrCodeLabel);
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
