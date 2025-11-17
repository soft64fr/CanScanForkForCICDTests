/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import com.google.zxing.WriterException;

import fr.softsf.canscan.model.QrConfig;
import fr.softsf.canscan.model.QrDataResult;
import fr.softsf.canscan.ui.MyPopup;
import fr.softsf.canscan.ui.QrCodeBufferedImage;
import fr.softsf.canscan.util.StringConstants;

/**
 * SwingWorker that generates and saves QR codes in a background thread. Handles UI updates and
 * error reporting on the Event Dispatch Thread.
 */
public class QrGenerateAndSaveWorker extends SwingWorker<BufferedImage, Void> {
    private final QrDataResult qrData;
    private final QrConfig config;
    private final JProgressBar loader;
    private final File outputFile;
    private final QrCodeBufferedImage qrCodeBufferedImage;
    private String errorTitle = null;
    private String errorMessage = null;

    /**
     * Creates a new QR generation worker.
     *
     * @param qrData the QR code data
     * @param config the visual configuration
     * @param loader the progress bar to hide after completion
     * @param outputFile the target file for saving the QR code
     * @param qrCodeBufferedImage the service to generate QR code images
     */
    public QrGenerateAndSaveWorker(
            QrDataResult qrData,
            QrConfig config,
            JProgressBar loader,
            File outputFile,
            QrCodeBufferedImage qrCodeBufferedImage) {
        this.qrData = qrData;
        this.config = config;
        this.loader = loader;
        this.outputFile = outputFile;
        this.qrCodeBufferedImage = qrCodeBufferedImage;
    }

    /**
     * Generates the QR code image and saves it to the output file. Executed in a background thread.
     *
     * @return the generated QR code image
     * @throws Exception if generation or file saving fails
     */
    @Override
    protected BufferedImage doInBackground() throws Exception {
        try {
            BufferedImage qr = qrCodeBufferedImage.generateQrCodeImage(qrData.data(), config);
            saveQrCodeToFile(qr, outputFile);
            return qr;
        } catch (WriterException | IOException | OutOfMemoryError e) {
            handleBackgroundError(e);
            throw e;
        }
    }

    /**
     * Saves the QR code image to a PNG file.
     *
     * @param qr the QR code image
     * @param file the target file
     * @throws IOException if file writing fails
     */
    private void saveQrCodeToFile(BufferedImage qr, File file) throws IOException {
        try (OutputStream os = new FileOutputStream(file)) {
            ImageIO.write(qr, "png", os);
        }
    }

    /**
     * Captures error details for later display to the user.
     *
     * @param e the exception that occurred
     */
    private void handleBackgroundError(Throwable e) {
        errorTitle = StringConstants.ERREUR.getValue();
        if (e instanceof WriterException) {
            errorMessage =
                    e.getMessage().equals("Data too big")
                            ? "Réduire la quantité de données, pour pouvoir enregistrer\n"
                            : "Pas de génération du QR Code\n" + e.getMessage();
        } else if (e instanceof IOException) {
            errorMessage = "Pas de lecture/écriture de fichier\n" + e.getMessage();
        } else if (e instanceof OutOfMemoryError) {
            errorMessage = "Manque de mémoire\n" + e.getMessage();
        }
    }

    /**
     * Handles the completion of the background task. Updates UI and displays success or error
     * messages on the EDT.
     */
    @Override
    protected void done() {
        try {
            handleSuccess();
        } catch (InterruptedException ie) {
            handleInterruption();
        } catch (ExecutionException ee) {
            handleExecutionError(ee);
        } finally {
            loader.setVisible(false);
        }
    }

    /**
     * Handles successful QR code generation by updating the original image and displaying a
     * confirmation dialog.
     *
     * @throws InterruptedException if the thread was interrupted
     * @throws ExecutionException if the background task threw an exception
     */
    private void handleSuccess() throws InterruptedException, ExecutionException {
        if (isCancelled()) {
            return;
        }
        BufferedImage qr = get();
        qrCodeBufferedImage.updateQrOriginal(qr);
        MyPopup.INSTANCE.showDialog(
                "Code QR enregistré dans\n", outputFile.getAbsolutePath(), "Confirmation");
    }

    /**
     * Handles thread interruption by restoring the interrupt status and displaying an error dialog.
     */
    private void handleInterruption() {
        Thread.currentThread().interrupt();
        MyPopup.INSTANCE.showDialog(
                "Opération interrompue\n",
                "La génération du QR code a été interrompue",
                StringConstants.ERREUR.getValue());
    }

    /**
     * Handles execution errors by displaying the appropriate error message.
     *
     * @param ee the execution exception containing the underlying error
     */
    private void handleExecutionError(ExecutionException ee) {
        if (errorTitle != null && errorMessage != null) {
            MyPopup.INSTANCE.showDialog("", errorMessage, errorTitle);
        } else {
            Throwable cause = ee.getCause();
            MyPopup.INSTANCE.showDialog(
                    "Erreur inattendue\n",
                    cause != null ? cause.getMessage() : ee.getMessage(),
                    StringConstants.ERREUR.getValue());
        }
    }
}
