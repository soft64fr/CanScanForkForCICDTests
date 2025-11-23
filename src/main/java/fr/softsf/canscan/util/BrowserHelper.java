/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import fr.softsf.canscan.ui.MyPopup;

/**
 * Singleton service for browser operations in Swing applications. Provides robust URL opening with
 * timeout protection using the Desktop API.
 */
public enum BrowserHelper {
    INSTANCE;

    private static final int TIMEOUT_SECONDS = 2;
    private static final String ERROR = "ERROR";

    /**
     * Opens a URL in the system's default browser with timeout protection. Requires Desktop API
     * support on the platform.
     *
     * @param url the URL to open (must be valid URI format)
     * @throws NullPointerException if url is null
     */
    public void openInBrowser(String url) {
        Objects.requireNonNull(url, "URL cannot be null");

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> future = executor.submit(() -> tryOpenBrowser(url));

            try {
                future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                handleError("Browser opening timed out", e, url);
            } catch (ExecutionException e) {
                handleError("Error opening browser", e, url);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                handleError("Browser opening interrupted", e, url);
            }
        }
    }

    /**
     * Attempts to open the URL using Java's Desktop API.
     *
     * @param url the URL to open
     */
    private void tryOpenBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
            } else {
                handleError(
                        "Desktop API not supported on this platform",
                        new UnsupportedOperationException(),
                        url);
            }
        } catch (IOException e) {
            handleError("Failed to open URL", e, url);
        } catch (IllegalArgumentException e) {
            handleError("Invalid URL format", e, url);
        }
    }

    /**
     * Displays error dialog to user with failure details.
     *
     * @param message error description
     * @param e exception that occurred
     * @param url the URL that failed to open
     */
    private void handleError(String message, Exception e, String url) {
        MyPopup.INSTANCE.showDialog(message + "\n", e.getMessage() + "\nURL: " + url, ERROR);
    }
}
