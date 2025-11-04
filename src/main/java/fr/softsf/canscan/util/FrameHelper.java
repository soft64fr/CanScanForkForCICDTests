/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import java.awt.Component;
import java.awt.Frame;

/** Singleton utility for accessing active application frames. */
public enum FrameHelper {
    INSTANCE;

    /**
     * Gets the active frame or null if none exists.
     *
     * @return the first active frame or null
     */
    public Component getParentFrame() {
        Frame[] frames = Frame.getFrames();
        return (frames.length > 0) ? frames[0] : null;
    }
}
