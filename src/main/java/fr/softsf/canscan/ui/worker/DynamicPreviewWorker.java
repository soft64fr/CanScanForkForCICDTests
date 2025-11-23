/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui.worker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;

import fr.softsf.canscan.constant.StringConstants;
import fr.softsf.canscan.model.CommonFields;
import fr.softsf.canscan.model.EncodedData;
import fr.softsf.canscan.model.WholeFields;
import fr.softsf.canscan.service.DataBuilderService;
import fr.softsf.canscan.ui.EncodedImage;
import fr.softsf.canscan.ui.LabelIconUtil;
import fr.softsf.canscan.ui.MyPopup;
import fr.softsf.canscan.util.Checker;

/**
 * Asynchronously generates and displays a QR code preview in a Swing UI.
 *
 * <p>Preview generation runs off the Event Dispatch Thread (EDT) to maintain interface
 * responsiveness. This class uses a debounce mechanism and background {@link SwingWorker} to avoid
 * unnecessary regenerations when multiple input or configuration changes occur rapidly.
 *
 * <p>Each instance manages the lifecycle of a QR code preview for a specific {@link JLabel} and
 * collaborates with a {@link DynamicResizeWorker} instance for dynamic resizing. The optional
 * {@link JProgressBar} can show a wait/progress indicator during background processing.
 *
 * <p>Resources are properly managed: previous images are freed, background workers are cancelled,
 * and the loader is stopped to prevent memory leaks and ensure smooth UI updates.
 */
public class DynamicPreviewWorker extends AbstractDynamicWorker<BufferedImage> {

    private static final int PREVIEW_DEBOUNCE_DELAY_MS = 200;

    private final EncodedImage encodedImage;
    private final DynamicResizeWorker qrCodeResize;
    private final JLabel qrCodeLabel;

    /**
     * Constructs a new asynchronous QR code preview manager for the specified label.
     *
     * @param encodedImage the source QR code image; must not be {@code null}
     * @param qrCodeResize the {@link DynamicResizeWorker} instance responsible for asynchronous
     *     resizing; must not be {@code null}
     * @param qrCodeLabel the label where the generated QR code preview will be displayed; must not
     *     be {@code null}
     * @param loader loader to indicate background processing
     */
    public DynamicPreviewWorker(
            EncodedImage encodedImage,
            DynamicResizeWorker qrCodeResize,
            JLabel qrCodeLabel,
            JProgressBar loader) {
        super(loader);
        this.encodedImage = encodedImage;
        this.qrCodeResize = qrCodeResize;
        this.qrCodeLabel = qrCodeLabel;
    }

    /**
     * Updates and schedules a debounced QR code preview refresh.
     *
     * <p>Uses the unified workflow: cancel → stop → clear → start new worker.
     *
     * @param wholeFields the latest QR code configuration
     */
    public void updateQrCodePreview(WholeFields wholeFields) {
        Checker.INSTANCE.checkNPE(wholeFields, "updateQrCodePreview", "wholeFields");
        this.wholeFields = wholeFields;
        resetAndStartWorker(PREVIEW_DEBOUNCE_DELAY_MS);
    }

    /**
     * Clears the current preview image before generating a new one. Invoked automatically by the
     * {@link AbstractDynamicWorker} workflow.
     */
    @Override
    protected void clearResources() {
        encodedImage.freeQrOriginal();
        LabelIconUtil.INSTANCE.disposeIcon(qrCodeLabel);
        qrCodeLabel.setIcon(null);
    }

    /**
     * Creates a background {@link SwingWorker} that generates the QR code preview image.
     *
     * <p>The worker runs off the EDT and ensures the loader is stopped once execution finishes.
     *
     * @return a configured {@link SwingWorker} producing a {@link BufferedImage}
     */
    @Override
    protected SwingWorker<BufferedImage, Void> createWorker() {
        return new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                Thread.currentThread().setName("PreviewWorker");
                return buildPreviewImage();
            }

            @Override
            protected void done() {
                handleWorkerDone();
            }
        };
    }

    /**
     * Updates the preview image after successful worker completion.
     *
     * <p>If the generated image is valid, it updates the source image and triggers resizing.
     *
     * @param img the generated QR code preview, or {@code null} if cancelled or invalid
     */
    @Override
    protected void onWorkerSuccess(BufferedImage img) {
        if (img == null) {
            qrCodeLabel.setIcon(null);
            return;
        }
        encodedImage.updateQrOriginal(img);
        qrCodeResize.updateQrCodeResize(wholeFields);
    }

    /**
     * Builds and returns the QR code preview image based on the current {@link WholeFields}.
     *
     * <p>Performs intermediate cancellation checks to maintain responsiveness. Returns {@code null}
     * if cancelled, invalid, or if an exception occurs.
     *
     * @return a {@link BufferedImage} representing the QR preview, or {@code null} if
     *     cancelled/invalid
     */
    private BufferedImage buildPreviewImage() {
        if (Thread.currentThread().isInterrupted() || wholeFields == null) {
            return null;
        }
        try {
            EncodedData qrData =
                    DataBuilderService.INSTANCE.buildData(wholeFields.currentMode(), wholeFields);
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
            CommonFields config = getCommonFields();
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            return encodedImage.generateImage(data, config);
        } catch (Exception ex) {
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            showPreviewErrorMessage(ex);
            return null;
        }
    }

    /**
     * Creates and returns the shared QR generation settings derived from the current {@link
     * WholeFields}.
     *
     * @return a {@link CommonFields} instance populated with size, ratio, colors, margin, and
     *     optional logo
     */
    private CommonFields getCommonFields() {
        File logoFile = wholeFields.logoPath().isBlank() ? null : new File(wholeFields.logoPath());
        return new CommonFields(
                logoFile,
                wholeFields.size(),
                wholeFields.ratio(),
                wholeFields.qrColor(),
                wholeFields.bgColor(),
                wholeFields.isRoundedModules(),
                wholeFields.margin());
    }

    /**
     * Displays an error dialog on the EDT based on the given exception.
     *
     * <p>If the exception message is {@code "Data too big"}, a specific user-friendly hint is
     * shown. Otherwise, a generic QR rendering error message is displayed.
     *
     * @param ex the exception that caused the QR generation failure
     */
    private void showPreviewErrorMessage(Exception ex) {
        SwingUtilities.invokeLater(
                () ->
                        MyPopup.INSTANCE.showDialog(
                                ex.getMessage().equals("Data too big")
                                        ? "Réduire la quantité de données\n"
                                        : "Pas de rendu du code QR\n",
                                ex.getMessage(),
                                StringConstants.ERREUR.getValue()));
    }
}
