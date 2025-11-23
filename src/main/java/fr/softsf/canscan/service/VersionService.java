/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.swing.JButton;
import javax.swing.SwingWorker;

import fr.softsf.canscan.constant.StringConstants;
import fr.softsf.canscan.ui.MyPopup;
import fr.softsf.canscan.util.Checker;

/**
 * Singleton service for checking application version against GitHub releases. Uses the GitHub API
 * to compare the current version with the latest tagged release.
 */
public enum VersionService {
    INSTANCE;

    private static final String GITHUB_TAGS_API_URL =
            "https://api.github.com/repos/Lob2018/CanScan/tags";
    private static final Pattern TAG_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
    private static final int HTTP_STATUS_CODE_OK = 200;
    private static final String LATEST_RELEASES_REPO_URL =
            "https://github.com/Lob2018/CanScan/releases/latest";
    private static final String CLOSE_HTML = "</html>";
    private final HttpClient httpClient;

    /**
     * Initializes the HTTP client with TLS 1.3 and 5-second timeout. Sets httpClient to null if
     * initialization fails.
     */
    VersionService() {
        HttpClient client = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(null, null, null);
            client =
                    HttpClient.newBuilder()
                            .sslContext(sslContext)
                            .connectTimeout(Duration.ofSeconds(5))
                            .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            MyPopup.INSTANCE.showDialog(
                    "TLS 1.3 est requis mais non disponible.\n",
                    e.getMessage(),
                    StringConstants.ERREUR.getValue());
        } catch (UncheckedIOException | SecurityException e) {
            MyPopup.INSTANCE.showDialog(
                    "Impossible de créer HTTP client.\n",
                    e.getMessage(),
                    StringConstants.ERREUR.getValue());
        }
        this.httpClient = client;
    }

    /**
     * Asynchronously checks whether the current application version matches the latest GitHub
     * release tag. If an update is available, the specified button is made enabled; otherwise, it
     * remains disabled.
     *
     * @param currentVersion the current version of the application (e.g., "1.0.0.0")
     * @param updateButton the button to toggle enabled state based on update status
     * @return a {@code SwingWorker} that returns {@code true} if the version is up-to-date, {@code
     *     false} if an update is available
     */
    public SwingWorker<Boolean, Void> checkLatestVersion(
            String currentVersion, JButton updateButton) {
        SwingWorker<Boolean, Void> npe = npeCheckLatestVersion(currentVersion, updateButton);
        if (npe != null) {
            return npe;
        }
        return new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                if (httpClient == null) {
                    return true;
                }
                try {
                    return requestAndVerifiy(currentVersion);
                } catch (IOException e) {
                    return true;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean isUpToDate = get();
                    updateButtonState(
                            updateButton,
                            !isUpToDate,
                            isUpToDate
                                    ? "<html>Votre version est à jour.<br>"
                                            + LATEST_RELEASES_REPO_URL
                                            + CLOSE_HTML
                                    : "<html>Cliquer pour télécharger la nouvelle version.<br>"
                                            + LATEST_RELEASES_REPO_URL
                                            + CLOSE_HTML);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    updateButtonState(
                            updateButton,
                            false,
                            "<html>Vérification de la mise à jour interrompue.<br>"
                                    + LATEST_RELEASES_REPO_URL
                                    + CLOSE_HTML);
                } catch (ExecutionException e) {
                    updateButtonState(
                            updateButton,
                            false,
                            "<html>Vérification de la mise à jour non réalisée.<br>"
                                    + LATEST_RELEASES_REPO_URL
                                    + CLOSE_HTML);
                }
            }
        };
    }

    /**
     * Sends an HTTP request to the GitHub tags API and verifies if the latest release tag matches
     * the current version.
     *
     * @param currentVersion the current version of the application
     * @return {@code true} if the version is up-to-date or if verification fails; {@code false} if
     *     an update is available
     * @throws IOException if the request fails due to network issues
     * @throws InterruptedException if the thread is interrupted during the request
     */
    private Boolean requestAndVerifiy(String currentVersion)
            throws IOException, InterruptedException {
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(GITHUB_TAGS_API_URL))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();
        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == HTTP_STATUS_CODE_OK) {
            Matcher matcher = TAG_PATTERN.matcher(response.body());
            if (matcher.find()) {
                String latestTag = matcher.group(1).trim();
                return latestTag.equalsIgnoreCase("v" + currentVersion);
            }
        }
        return true;
    }

    /**
     * Validates input for version check. If null, returns a fallback {@code SwingWorker} that
     * disables the update button; otherwise returns {@code null}.
     *
     * @param currentVersion current app version
     * @param updateButton button to toggle state
     * @return fallback worker if input is null; {@code null} otherwise
     */
    private static SwingWorker<Boolean, Void> npeCheckLatestVersion(
            String currentVersion, JButton updateButton) {
        if (Checker.INSTANCE.checkNPE(currentVersion, "checkLatestVersion", "currentVersion")
                || Checker.INSTANCE.checkNPE(updateButton, "checkLatestVersion", "updateButton")) {
            return new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return true;
                }

                @Override
                protected void done() {
                    updateButtonState(
                            updateButton,
                            false,
                            "<html>Il manque un paramètre pour vérifier la mise à jour.<br>"
                                    + LATEST_RELEASES_REPO_URL
                                    + CLOSE_HTML);
                }
            };
        }
        return null;
    }

    /**
     * Updates the state and tooltip of the given button based on application logic.
     *
     * @param button the JButton to update
     * @param enabled whether the button should be enabled
     * @param tooltip the tooltip text to display on hover
     */
    private static void updateButtonState(JButton button, boolean enabled, String tooltip) {
        button.setEnabled(enabled);
        button.setToolTipText(tooltip);
    }
}
