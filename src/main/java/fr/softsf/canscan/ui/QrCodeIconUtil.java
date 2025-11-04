/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import fr.softsf.canscan.util.Checker;

/**
 * Utility for releasing memory used by QR code icons. All methods must be called from the Event
 * Dispatch Thread (EDT).
 */
public enum QrCodeIconUtil {
    INSTANCE;

    /**
     * Releases memory used by the QR code icon. Must be called from the EDT.
     *
     * @param label the {@link JLabel} containing the icon to dispose
     */
    public void disposeIcon(JLabel label) {
        if (Checker.checkStaticNPE(label, "disposeIcon", "label")) {
            return;
        }
        Icon icon = label.getIcon();
        if (icon instanceof ImageIcon imageIcon) {
            Image img = imageIcon.getImage();
            if (img != null) {
                img.flush();
                if (img instanceof BufferedImage bi) {
                    bi.flush();
                }
            }
        }
        label.setIcon(null);
    }
}
