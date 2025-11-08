/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.apache.commons.lang3.StringUtils;

import fr.softsf.canscan.model.QrConfig;
import fr.softsf.canscan.model.QrDataResult;
import fr.softsf.canscan.model.QrInput;
import fr.softsf.canscan.service.BuildQRDataService;
import fr.softsf.canscan.util.Checker;
import fr.softsf.canscan.util.StringConstants;

/**
 * Handles asynchronous generation and display of QR code previews in a Swing UI.
 *
 * <p>Preview generation runs off the Event Dispatch Thread (EDT) to maintain interface
 * responsiveness. Built-in debounce control, worker cancellation, and background image generation
 * ensure efficient, flicker-free updates during frequent input or configuration changes.
 *
 * <p>Each instance manages the preview lifecycle for a specific {@link JLabel} and works with a
 * {@link QrCodeResize} instance to handle dynamic resizing. This design supports multiple
 * independent QR preview components within the same application.
 */
public class QrCodePreview {

    private Timer previewDebounceTimer;
    private SwingWorker<BufferedImage, Void> previewWorker;
    private QrInput qrInput;
    private final QrCodeBufferedImage qrCodeBufferedImage;
    private final QrCodeResize qrCodeResize;
    private final JLabel qrCodeLabel;

    /**
     * Creates a new asynchronous QR code preview manager for the specified label.
     *
     * @param qrCodeBufferedImage the {@link QrCodeBufferedImage} providing the source QR image;
     *     must not be {@code null}
     * @param qrCodeResize the {@link QrCodeResize} instance responsible for asynchronous resizing;
     *     must not be {@code null}
     * @param qrCodeLabel the Swing {@link JLabel} used to display the generated QR preview; must
     *     not be {@code null}
     */
    public QrCodePreview(
            QrCodeBufferedImage qrCodeBufferedImage,
            QrCodeResize qrCodeResize,
            JLabel qrCodeLabel) {
        this.qrCodeBufferedImage = qrCodeBufferedImage;
        this.qrCodeResize = qrCodeResize;
        this.qrCodeLabel = qrCodeLabel;
    }

    /**
     * Returns the currently active debounce timer.
     *
     * @return the active {@link Timer}, or {@code null} if none
     */
    public Timer getPreviewDebounceTimer() {
        return previewDebounceTimer;
    }

    /**
     * Updates the debounce timer and cancels any active preview worker to ensure a clean restart.
     *
     * @param previewDebounceTimer the new debounce timer to apply
     */
    public void updatePreviewDebounceTimer(Timer previewDebounceTimer) {
        cancelActivePreviewWorker();
        this.previewDebounceTimer = previewDebounceTimer;
    }

    /** Stops the debounce timer and clears internal references. */
    public void stop() {
        if (previewDebounceTimer != null) {
            previewDebounceTimer.stop();
            previewDebounceTimer = null;
        }
    }

    /**
     * Checks whether the debounce timer is currently active.
     *
     * @return {@code true} if running, {@code false} otherwise
     */
    public boolean isRunning() {
        return previewDebounceTimer != null && previewDebounceTimer.isRunning();
    }

    /**
     * Sets the current {@link QrInput} and launches a new asynchronous preview generation task.
     *
     * <p>Replaces any existing task and triggers QR code generation off the EDT.
     *
     * @param qrInput the latest QR code input data to render
     */
    public void launchPreviewWorker(QrInput qrInput) {
        this.qrInput = qrInput;
        previewWorker = createPreviewWorker();
        previewWorker.execute();
    }

    /**
     * Creates a {@link SwingWorker} responsible for generating the QR code image in a background
     * thread and updating the UI upon completion.
     *
     * <p>Ensures the wait icon is stopped once rendering finishes, whether successful or not.
     *
     * @return a new configured SwingWorker instance
     */
    private SwingWorker<BufferedImage, Void> createPreviewWorker() {
        return new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                Thread.currentThread().setName("PreviewWorker");
                return buildPreviewImage();
            }

            @Override
            protected void done() {
                Loader.INSTANCE.stopWaitIcon();
                handlePreviewWorkerCompletion(this);
            }
        };
    }

    /**
     * Handles completion of the preview worker by validating cancellation state, retrieving the
     * image, and updating the displayed QR code if appropriate.
     *
     * <p>Ensures resources are flushed on cancellation and errors are reported through the {@link
     * Popup} system.
     *
     * @param worker the completed SwingWorker instance
     */
    private void handlePreviewWorkerCompletion(SwingWorker<BufferedImage, Void> worker) {
        if (Checker.INSTANCE.checkNPE(worker, "handlePreviewWorkerCompletion", "worker")) {
            return;
        }
        boolean cancelledOrStale = worker.isCancelled() || worker != previewWorker;
        try {
            BufferedImage img = worker.get();
            if (cancelledOrStale) {
                if (img != null) {
                    img.flush();
                }
                return;
            }
            qrCodeBufferedImage.updateQrOriginal(img);
            if (qrCodeBufferedImage.getQrOriginal() != null) {
                qrCodeResize.updateQrCodeSize(qrInput);
            } else {
                qrCodeLabel.setIcon(null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (CancellationException | ExecutionException ex) {
            if (cancelledOrStale) {
                return;
            }
            Popup.INSTANCE.showDialog(
                    "Pas d'affichage\n", ex.getMessage(), StringConstants.ERREUR.getValue());
        }
    }

    /**
     * Builds and returns a QR code preview image using the current input and configuration.
     *
     * <p>Performs intermediate cancellation checks to maintain responsiveness while avoiding
     * unnecessary overhead. Returns {@code null} if cancelled, invalid, or if an exception occurs.
     *
     * @return a {@link BufferedImage} representing the QR preview, or {@code null} if cancelled or
     *     invalid
     */
    private BufferedImage buildPreviewImage() {
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }
        try {
            QrDataResult qrData =
                    BuildQRDataService.INSTANCE.buildQrData(qrInput.currentMode(), qrInput);
            if (Thread.currentThread().isInterrupted()
                    || Checker.INSTANCE.checkNPE(
                            qrData,
                            StringConstants.GENERATE_QR_CODE.getValue(),
                            StringConstants.QR_DATA.getValue())) {
                return null;
            }
            Objects.requireNonNull(qrData, "Dans buildPreviewImage qrData ne doit pas être null");
            String data = qrData.data();
            if (StringUtils.isBlank(data)) {
                return null;
            }
            File logoFile = qrInput.logoPath().isBlank() ? null : new File(qrInput.logoPath());
            QrConfig config =
                    new QrConfig(
                            logoFile,
                            qrInput.size(),
                            qrInput.ratio(),
                            qrInput.qrColor(),
                            qrInput.bgColor(),
                            qrInput.isRoundedModules(),
                            qrInput.margin());
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            return qrCodeBufferedImage.generateQrCodeImage(qrData.data(), config);
        } catch (Exception ex) {
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            SwingUtilities.invokeLater(
                    () ->
                            Popup.INSTANCE.showDialog(
                                    "Pas de rendu du code QR\n",
                                    ex.getMessage(),
                                    StringConstants.ERREUR.getValue()));
            return null;
        }
    }

    /**
     * Cancels and flushes any active preview worker to ensure a clean restart before a new preview
     * is generated.
     *
     * <p>Blocks until the worker terminates or throws a cancellation-related exception.
     */
    public void cancelActivePreviewWorker() {
        if (previewWorker == null || previewWorker.isDone()) {
            return;
        }
        previewWorker.cancel(true);
        try {
            previewWorker.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | CancellationException ignored) {
            // Expected: cancellation or execution failure
        }
        previewWorker = null;
    }
}
