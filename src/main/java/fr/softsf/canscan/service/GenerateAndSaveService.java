/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import fr.softsf.canscan.constant.StringConstants;
import fr.softsf.canscan.model.CommonFields;
import fr.softsf.canscan.model.EncodedData;
import fr.softsf.canscan.ui.EncodedImage;
import fr.softsf.canscan.ui.MyPopup;
import fr.softsf.canscan.ui.worker.GenerateAndSaveWorker;
import fr.softsf.canscan.util.Checker;

/** Service dedicated to QR code generation and saving. */
@SuppressWarnings("ClassCanBeRecord")
public class GenerateAndSaveService {

    private final EncodedImage encodedImage;

    /**
     * Constructs a GenerateAndSaveService with the given QR code image generator.
     *
     * @param encodedImage the QR code image generator; must not be null
     */
    public GenerateAndSaveService(EncodedImage encodedImage) {
        this.encodedImage = Objects.requireNonNull(encodedImage, "encodedImage must not be null");
    }

    /**
     * Generates and saves a QR code as a PNG file using the provided data and configuration.
     *
     * <p>Validates input data, applies visual settings, generates the QR code image, and saves it
     * to a user-selected file location. All operations are performed asynchronously to prevent UI
     * blocking.
     *
     * @param qrData the QR code data; must not be null
     * @param config the visual configuration; must not be null
     * @param loader the progress indicator to display during generation
     */
    public void generateAndSave(EncodedData qrData, CommonFields config, JProgressBar loader) {
        if (checkNPEInputs(qrData, config, loader)) {
            if (qrData.data().isBlank()) {
                MyPopup.INSTANCE.showDialog(
                        "Aucune donnée à encoder",
                        "Compléter les champs correspondants",
                        "Information");
                return;
            }
            File outputFile = chooseOutputFile(qrData);
            if (outputFile == null) {
                return;
            }
            loader.setVisible(true);
            executeQrGeneration(qrData, config, loader, outputFile);
        }
    }

    /**
     * Validates that all required inputs are non-null.
     *
     * @param qrData the QR code data to validate
     * @param config the configuration to validate
     * @param loader the progress bar to validate
     * @return true if all inputs are valid, false otherwise
     */
    private boolean checkNPEInputs(EncodedData qrData, CommonFields config, JProgressBar loader) {
        return !Checker.INSTANCE.checkNPE(
                        qrData,
                        StringConstants.GENERATE_AND_SAVE_QR_CODE.getValue(),
                        StringConstants.QR_DATA.getValue())
                && !Checker.INSTANCE.checkNPE(
                        config, StringConstants.GENERATE_AND_SAVE_QR_CODE.getValue(), "config")
                && !Checker.INSTANCE.checkNPE(
                        loader, StringConstants.GENERATE_AND_SAVE_QR_CODE.getValue(), "loader");
    }

    /**
     * Executes QR code generation asynchronously using a SwingWorker.
     *
     * @param qrData the QR code data
     * @param config the visual configuration
     * @param loader the progress bar to hide after completion
     * @param outputFile the file where the QR code will be saved
     */
    private void executeQrGeneration(
            EncodedData qrData, CommonFields config, JProgressBar loader, File outputFile) {
        SwingWorker<BufferedImage, Void> worker =
                new GenerateAndSaveWorker(qrData, config, loader, outputFile, encodedImage);
        worker.execute();
    }

    /**
     * Opens a JFileChooser to select the output PNG file and handles file name conflicts.
     *
     * @param qrData the QR code data used to generate the default file name
     * @return the selected file ready for writing, or null if the user cancels
     */
    private File chooseOutputFile(EncodedData qrData) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
        chooser.setDialogTitle("Enregistrer votre code QR en tant que PNG");
        chooser.setSelectedFile(new File(qrData.defaultFileName()));
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return resolveFileNameConflict(getSelectedPngFile(chooser));
    }

    /**
     * Ensures the selected file has a ".png" extension.
     *
     * @param chooser the file chooser
     * @return a File guaranteed to have a ".png" extension
     */
    private File getSelectedPngFile(JFileChooser chooser) {
        if (Checker.INSTANCE.checkNPE(chooser, "getSelectedPngFile", "chooser")) {
            return null;
        }
        File output = chooser.getSelectedFile();
        String fileName = output.getName().toLowerCase();
        if (fileName.endsWith(".png")) {
            return output;
        }
        return new File(output.getParentFile(), output.getName() + ".png");
    }

    /**
     * Resolves potential file name conflicts by checking if the specified file already exists.
     *
     * <p>If the file exists, the user is prompted to overwrite it. If the user declines, a new file
     * name is generated by appending a numeric suffix to avoid overwriting existing files.
     *
     * @param file the initial File to check for conflicts
     * @return a File ready for writing, either the original, user-approved, or auto-renamed, or
     *     null if input file is null
     */
    private File resolveFileNameConflict(File file) {
        if (Checker.INSTANCE.checkNPE(file, "resolveFileNameConflict", "file")) {
            return null;
        }
        if (file.exists()) {
            int choice =
                    MyPopup.INSTANCE.showYesNoConfirmDialog(
                            "Un fichier \""
                                    + file.getName()
                                    + "\" existe déjà.\nÉcraser ce fichier ?");
            if (choice == 0) {
                return file;
            }
            String baseName = file.getName().replaceFirst("\\.png$", "");
            File parent = file.getParentFile();
            int counter = 1;
            File candidate;
            do {
                candidate = new File(parent, baseName + "(" + counter + ").png");
                counter++;
            } while (candidate.exists());
            return candidate;
        }
        return file;
    }

    /** Returns the selected PNG file for testing. */
    File getSelectedFileForTests(JFileChooser chooser) {
        return getSelectedPngFile(chooser);
    }

    /** Resolves file name conflicts for testing. */
    File resolveFileNameConflictForTests(File file) {
        return resolveFileNameConflict(file);
    }
}
