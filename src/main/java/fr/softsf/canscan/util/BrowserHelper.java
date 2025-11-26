/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
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
     * Attempts to open a URL in the default browser asynchronously with a 2-second timeout.
     *
     * @param url The URL to open.
     * @return {@code true} if the launch command completed successfully within the timeout; {@code
     *     false} otherwise (error details are displayed via a dialog).
     * @throws NullPointerException if url is {@code null}.
     */
    public boolean openInBrowser(String url) {
        Objects.requireNonNull(url, "URL cannot be null");

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<Boolean> future = executor.submit(() -> tryOpenBrowser(url));
            try {
                return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                handleError("Browser opening timed out", e, url);
                return false;
            } catch (ExecutionException e) {
                handleError("Error opening browser", e, url);
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                handleError("Browser opening interrupted", e, url);
                return false;
            }
        }
    }

    /**
     * Executes the {@code Desktop.browse} operation. Handles and logs internal failures.
     *
     * @param url The URL to open.
     * @return {@code true} if {@code Desktop.browse} was successfully called; {@code false} on
     *     internal error (e.g., IO, unsupported API).
     */
    private boolean tryOpenBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
                return true;
            } else {
                handleError(
                        "Desktop API not supported on this platform",
                        new UnsupportedOperationException(),
                        url);
                return false;
            }
        } catch (IOException e) {
            handleError("Failed to open URL", e, url);
            return false;
        } catch (IllegalArgumentException e) {
            handleError("Invalid URL format", e, url);
            return false;
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
