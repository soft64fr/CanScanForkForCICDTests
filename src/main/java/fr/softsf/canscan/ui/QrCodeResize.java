/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.apache.commons.lang3.StringUtils;

import fr.softsf.canscan.model.QrDataResult;
import fr.softsf.canscan.model.QrInput;
import fr.softsf.canscan.service.BuildQRDataService;
import fr.softsf.canscan.util.Checker;
import fr.softsf.canscan.util.StringConstants;

/**
 * Handles asynchronous resizing of generated QR code images in a Swing UI.
 *
 * <p>Resize operations are executed off the Event Dispatch Thread (EDT) to preserve interface
 * responsiveness. Built-in debounce control, worker cancellation, and resource management ensure
 * efficient, flicker-free updates during frequent layout or configuration changes.
 *
 * <p>Each instance manages resizing for a specific {@link JLabel}, enabling multiple independent
 * resizable QR components within the same application. The optional {@link Loader} allows the UI to
 * display a loading indicator while the resize operation is in progress.
 */
public class QrCodeResize {

    private static final int QR_CODE_LABEL_DEFAULT_SIZE = 50;
    private static final int LARGE_IMAGE_THRESHOLD = 1000;
    private static final int RESIZE_DEBOUNCE_DELAY_MS = 200;

    private Timer resizeDebounceTimer;
    private SwingWorker<ImageIcon, Void> resizeWorker;
    private QrInput qrInput;
    private final QrCodeBufferedImage qrCodeBufferedImage;
    private final JLabel qrCodeLabel;
    private final Loader loader;

    /**
     * Creates a new asynchronous QR code resize manager for the specified label.
     *
     * @param qrCodeBufferedImage the source {@link QrCodeBufferedImage} used for resizing; must not
     *     be {@code null}
     * @param qrCodeLabel the Swing {@link JLabel} that displays the resized QR code; must not be
     *     {@code null}
     * @param loader optional {@link Loader} used to indicate ongoing background processing; can be
     *     {@code null}
     */
    public QrCodeResize(
            QrCodeBufferedImage qrCodeBufferedImage, JLabel qrCodeLabel, Loader loader) {
        this.qrCodeBufferedImage = qrCodeBufferedImage;
        this.qrCodeLabel = qrCodeLabel;
        this.loader = loader;
    }

    /**
     * Returns the current debounce timer.
     *
     * @return the active {@link Timer}, or {@code null} if none
     */
    public Timer getResizeDebounceTimer() {
        return resizeDebounceTimer;
    }

    /**
     * Updates the debounce timer with a new instance.
     *
     * @param resizeDebounceTimer the timer to assign
     */
    public void updateResizeDebounceTimer(Timer resizeDebounceTimer) {
        this.resizeDebounceTimer = resizeDebounceTimer;
    }

    /** Stops the current debounce timer and clears its reference. */
    public void stop() {
        if (resizeDebounceTimer != null) {
            resizeDebounceTimer.stop();
            resizeDebounceTimer = null;
        }
    }

    /**
     * Checks if a resize debounce timer is currently running.
     *
     * @return {@code true} if active, otherwise {@code false}
     */
    public boolean isRunning() {
        return resizeDebounceTimer != null && resizeDebounceTimer.isRunning();
    }

    /**
     * Launches a background worker that resizes the QR code image to the specified height.
     *
     * @param height the desired image height in pixels
     */
    public void launchResizeWorker(int height) {
        resizeWorker = createResizeWorker(height);
        resizeWorker.execute();
    }

    /**
     * Creates a {@link SwingWorker} to perform image resizing off the EDT and update the UI when
     * the task completes successfully. Ensures the wait icon is stopped in all cases.
     *
     * @param height the target height for resizing
     * @return a new SwingWorker instance configured for resizing
     */
    private SwingWorker<ImageIcon, Void> createResizeWorker(int height) {
        return new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                Thread.currentThread().setName("ResizeWorker");
                return resizeImageInBackground(height);
            }

            @Override
            protected void done() {
                loader.stopWaitIcon();
                handleResizeWorkerCompletion(this);
            }
        };
    }

    /**
     * Performs image resizing logic off the EDT. Uses interpolation based on target size for
     * optimal quality and speed. Cancels promptly if the thread is interrupted.
     *
     * @param squareSize the target height for the resized QR code
     * @return an {@link ImageIcon} containing the resized image, or {@code null} if cancelled or
     *     invalid
     */
    private ImageIcon resizeImageInBackground(int squareSize) {
        if (squareSize <= 0
                || qrCodeBufferedImage.getQrOriginal() == null
                || Thread.currentThread().isInterrupted()) {
            return null;
        }
        BufferedImage scaled =
                new BufferedImage(squareSize, squareSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        try {
            g2d.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    squareSize > LARGE_IMAGE_THRESHOLD
                            ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                            : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(qrCodeBufferedImage.getQrOriginal(), 0, 0, squareSize, squareSize, null);
        } finally {
            g2d.dispose();
        }
        if (Thread.currentThread().isInterrupted()) {
            scaled.flush();
            return null;
        }
        return new ImageIcon(scaled);
    }

    /**
     * Handles resize worker completion, ensuring that cancellation and stale worker checks are
     * performed before updating the UI.
     *
     * <p>Flushes obsolete icons and reports any errors encountered during execution.
     *
     * @param worker the completed SwingWorker instance
     */
    private void handleResizeWorkerCompletion(SwingWorker<ImageIcon, Void> worker) {
        if (Checker.INSTANCE.checkNPE(worker, "handleResizeWorkerCompletion", "worker")) {
            return;
        }
        boolean cancelledOrStale = worker.isCancelled() || worker != resizeWorker;
        try {
            ImageIcon icon = worker.get();
            if (cancelledOrStale) {
                if (icon != null) {
                    icon.getImage().flush();
                }
                return;
            }
            if (icon != null) {
                QrCodeIconUtil.INSTANCE.disposeIcon(qrCodeLabel);
                qrCodeLabel.setIcon(icon);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (CancellationException | ExecutionException ex) {
            if (cancelledOrStale) {
                return;
            }
            Popup.INSTANCE.showDialog(
                    "Pas de redimensionnement\n",
                    ex.getMessage(),
                    StringConstants.ERREUR.getValue());
        }
    }

    /**
     * Cancels any active resize worker and waits for proper termination. Prevents multiple
     * concurrent resize operations.
     */
    public void cancelPreviousResizeWorker() {
        if (resizeWorker == null || resizeWorker.isDone()) {
            return;
        }
        resizeWorker.cancel(true);
        try {
            resizeWorker.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | CancellationException ignored) {
            // Expected: CancellationException if cancelled, ExecutionException if task failed
        }
        resizeWorker = null;
        loader.stopWaitIcon();
    }

    /**
     * Updates internal {@link QrInput} and schedules a debounced QR code resize. Cancels any active
     * resize and triggers a new one after a short delay.
     *
     * @param qrInput latest QR code configuration
     */
    public void updateQrCodeSize(QrInput qrInput) {
        this.qrInput = qrInput;
        if (isRunning()) {
            getResizeDebounceTimer().restart();
            return;
        }
        updateResizeDebounceTimer(new Timer(RESIZE_DEBOUNCE_DELAY_MS, e -> handleResize()));
        getResizeDebounceTimer().setRepeats(false);
        getResizeDebounceTimer().start();
    }

    /**
     * Handles the QR code resize process after the debounce delay. Validates input, available
     * space, and launches a new resize task.
     */
    private void handleResize() {
        int squareSize = qrInput.availableHeightForQrCode();
        if (squareSize < QR_CODE_LABEL_DEFAULT_SIZE) {
            squareSize = QR_CODE_LABEL_DEFAULT_SIZE;
        }
        qrCodeLabel.setPreferredSize(new Dimension(squareSize, squareSize));
        QrDataResult qrData =
                BuildQRDataService.INSTANCE.buildQrData(qrInput.currentMode(), qrInput);
        if (isInvalidQrData(qrData)) {
            return;
        }
        if (qrCodeBufferedImage.getQrOriginal() == null) {
            return;
        }
        resetAndStartResizeWorker(squareSize);
    }

    /**
     * Checks whether {@link QrDataResult} is invalid or null, and performs cleanup if necessary.
     *
     * @param qrData the QR data to validate
     * @return true if invalid, false otherwise
     */
    private boolean isInvalidQrData(QrDataResult qrData) {
        if (Checker.INSTANCE.checkNPE(qrData, "isInvalidQrData", StringConstants.QR_DATA.getValue())
                || StringUtils.isBlank(qrData.data())) {
            loader.stopWaitIcon();
            qrCodeBufferedImage.freeQrOriginal();
            QrCodeIconUtil.INSTANCE.disposeIcon(qrCodeLabel);
            return true;
        }
        return false;
    }

    /**
     * Cancels any pending task, clears the current icon, starts the wait animation, and launches a
     * new resize worker for the given height.
     *
     * @param height target height for the resized image
     * @see #resetAndStartResizeWorker(int)
     */
    private void resetAndStartResizeWorker(int height) {
        cancelPreviousResizeWorker();
        QrCodeIconUtil.INSTANCE.disposeIcon(qrCodeLabel);
        qrCodeLabel.setIcon(null);
        SwingUtilities.invokeLater(loader::startAndAdjustWaitIcon);
        launchResizeWorker(height);
    }
}
