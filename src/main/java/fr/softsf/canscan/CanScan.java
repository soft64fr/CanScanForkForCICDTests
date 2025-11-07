/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;

import com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import fr.softsf.canscan.model.Mode;
import fr.softsf.canscan.model.QrDataResult;
import fr.softsf.canscan.model.QrInput;
import fr.softsf.canscan.service.BuildQRDataService;
import fr.softsf.canscan.service.VersionService;
import fr.softsf.canscan.ui.Loader;
import fr.softsf.canscan.ui.Popup;
import fr.softsf.canscan.ui.QrCodeBufferedImage;
import fr.softsf.canscan.ui.QrCodeIconUtil;
import fr.softsf.canscan.util.BrowserHelper;
import fr.softsf.canscan.util.Checker;
import fr.softsf.canscan.util.UseLucioleFont;

/**
 * CanScan - Swing application to generate QR Codes.
 *
 * <p>Supports two modes:
 *
 * <ul>
 *   <li>MECARD: generates a QR code from structured contact info (name, phone, email, etc.).
 *   <li>Free: generates a QR code from arbitrary text or URLs.
 * </ul>
 *
 * <p>Features live preview, optional logo embedding, customizable size, colors, margin, and module
 * style (rounded or square). Uses ZXing for QR code generation and FlatLaf for GUI styling.
 */
public class CanScan extends JFrame {

    private static final int DEFAULT_GAP = 15;
    private static final double DEFAULT_IMAGE_RATIO = 0.27;
    private static final int DEFAULT_QR_CODE_SIZE = 400;
    private static final double DEFAULT_GAP_BETWEEN_LOGO_AND_MODULES = 0.9;
    private static final int MAX_PERCENTAGE = 100;
    private static final int MAJOR_TICK_SPACING = 25;
    private static final int QR_CODE_LABEL_DEFAULT_SIZE = 50;
    private static final int MINIMUM_QR_CODE_SIZE = 10;
    private static final int TEXT_FIELDS_COLUMNS = 25;
    private static final int MULTILINE_TEXT_FIELDS_ROWS = 10;
    private static final int RADIO_BUTTON_GAP = 20;
    private static final int DEFAULT_LABEL_WIDTH = 140;
    private static final String ADD_ROW = "addRow";
    private static final String CONFIG = "config";
    private static final String DRAW_SQUARE_FINDER_PATTERN_AT_PIXEL =
            "drawSquareFinderPatternAtPixel";
    private static final String BG_COLOR = "bgColor";
    private static final String DRAW_ROUNDED_FINDER_PATTERN_AT_PIXEL =
            "drawRoundedFinderPatternAtPixel";
    private static final String SHOULD_SKIP_MODULE = "shouldSkipModule";
    private static final String DRAW_MODULES = "drawModules";
    private static final String MATRIX = "matrix";
    private static final String GENERATE_QR_CODE_IMAGE = "generateQrCodeImage";
    private static final String GENERATE_QR_CODE = "generateQrCode";
    private static final int GENERATE_BUTTON_EXTRA_HEIGHT = 35;
    private static final int VERTICAL_SCROLL_UNIT_INCREMENT = 16;
    private static final int RESIZE_DEBOUNCE_DELAY_MS = 200;
    private static final int PREVIEW_DEBOUNCE_DELAY_MS = 200;
    private static final String ERREUR = "Erreur";
    private static final int BUTTON_ICON_COLOR_SIZE = 14;
    private static final int BUTTON_COLOR_ICON_TEXT_GAP = 10;
    private static final int LARGE_IMAGE_THRESHOLD = 1000;
    private static final String SIZE_FIELD_DEFAULT = "400";
    private static final int AVAILABLE_MEMORY_TO_GENERATE_IMAGE = 50;
    private static final int BYTES_PER_KILOBYTE = 1024;
    private static final String QR_DATA = "qrData";
    private static final String LATEST_RELEASES_REPO_URL =
            "https://github.com/Lob2018/CanScan/releases/latest";
    private static final int COLOR_BUTTONS_GAP = 10;
    private static final int MARGE_MAXIMUM_VALUE = 10;
    private Color qrColor = Color.BLACK;
    private Color bgColor = Color.WHITE;
    private int margin = 3;
    private double imageRatio = DEFAULT_IMAGE_RATIO;
    private static String version;
    private static String name;
    private static String organization;
    // NORTH wrapper
    private final transient JPanel northPanelWrapper =
            new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    private Mode currentMode = Mode.MECARD;
    transient JRadioButton mecardRadio = new JRadioButton(Mode.MECARD.text());
    transient JRadioButton freeRadio = new JRadioButton(Mode.FREE.text());
    // Update
    JButton update = new JButton("\uD83D\uDD04");
    // Champs MECARD
    transient JTextField nameField = new JTextField(TEXT_FIELDS_COLUMNS);
    final transient JTextField phoneField = new JTextField(TEXT_FIELDS_COLUMNS);
    final transient JTextField emailField = new JTextField(TEXT_FIELDS_COLUMNS);
    final transient JTextField orgField = new JTextField(TEXT_FIELDS_COLUMNS);
    final transient JTextField adrField = new JTextField(TEXT_FIELDS_COLUMNS);
    final transient JTextField urlField = new JTextField(TEXT_FIELDS_COLUMNS);
    // Champs FREE
    final transient JTextArea freeField = new JTextArea("");
    final transient FontMetrics fm = freeField.getFontMetrics(freeField.getFont());
    final transient JScrollPane freeScrollPane = new JScrollPane(freeField);
    final int charHeight = fm.getHeight();
    final int charWidth = fm.charWidth('W');
    // CardLayout pour basculer entre MECARD et FREE
    private final transient CardLayout cardLayout = new CardLayout();
    private final transient JPanel cardPanel = new JPanel(cardLayout);
    // Champs commmuns
    final transient JTextField logoField = new JTextField(TEXT_FIELDS_COLUMNS);
    final transient JTextField sizeField = new JTextField(SIZE_FIELD_DEFAULT, TEXT_FIELDS_COLUMNS);
    final transient JSlider marginSlider = new JSlider(0, MINIMUM_QR_CODE_SIZE, margin);
    final transient JSlider ratioSlider =
            new JSlider(0, MAX_PERCENTAGE, (int) (imageRatio * MAX_PERCENTAGE));
    final transient JCheckBox roundedModulesCheckBox = new JCheckBox();
    // Rendu dynamique
    private final transient JLabel qrCodeLabel = new JLabel("", SwingConstants.CENTER);

    // SOUTH
    private final transient JPanel southSpacer = new JPanel();

    // Buttons
    @SuppressWarnings("FieldCanBeLocal")
    private final transient JButton browseButton = new JButton("\uD83D\uDCC1 Parcourir");

    private final transient JButton qrColorButton = new JButton("#000000");
    private final transient JButton bgColorButton = new JButton("#FFFFFF");

    @SuppressWarnings("FieldCanBeLocal")
    private final transient JButton generateButton = new JButton("\uD83D\uDCBE Enregistrer");

    private transient SwingWorker<ImageIcon, Void> resizeWorker;
    private transient Timer resizeDebounceTimer;
    private transient SwingWorker<BufferedImage, Void> previewWorker;
    private transient Timer previewDebounceTimer;

    /** Configuration parameters used to generate a QR code. */
    public record QrConfig(
            File logoFile,
            int size,
            double imageRatio,
            Color qrColor,
            Color bgColor,
            boolean roundedModules,
            int margin) {}

    /**
     * Encapsulates contextual data for module rendering decisions.
     *
     * <p>Includes the QR configuration, matrix dimensions, and logo area boundaries.
     */
    private record ModuleContext(
            QrConfig config,
            int matrixWidth,
            int matrixHeight,
            int whiteBoxX,
            int whiteBoxY,
            int whiteBoxSize) {}

    /**
     * Initializes the CanScan GUI: sets up input fields, mode selection, QR code parameters,
     * preview area, and action buttons. Configures layouts, listeners, and default window behavior.
     */
    public CanScan() {
        super(buildTitle());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(DEFAULT_GAP, DEFAULT_GAP));
        setResizable(true);
        // Init
        Loader.INSTANCE.init(qrCodeLabel);
        // Sliders
        marginSlider.setMajorTickSpacing(1);
        marginSlider.setPaintTicks(true);
        marginSlider.setPaintLabels(true);
        ratioSlider.setMajorTickSpacing(MAJOR_TICK_SPACING);
        ratioSlider.setMinorTickSpacing(1);
        ratioSlider.setPaintTicks(true);
        ratioSlider.setPaintLabels(true);
        ratioSlider.addChangeListener(e -> setRatioSliderTooltipValue());
        setRatioSliderTooltipValue();
        // NORTH WRAPPED (mode + card (MECARD + FREE) + common)
        JPanel northPanel = new JPanel(new GridBagLayout());
        northPanelWrapper.add(northPanel);
        northPanel.setMaximumSize(new Dimension(DEFAULT_LABEL_WIDTH * 3, northPanel.getHeight()));
        northPanel.setBorder(new EmptyBorder(DEFAULT_GAP, DEFAULT_GAP, DEFAULT_GAP, DEFAULT_GAP));
        GridBagConstraints grid = new GridBagConstraints();
        grid.insets = new Insets(3, 3, 3, 3);
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.gridy = -1;
        grid.weightx = 1;
        GridBagLayout layout = (GridBagLayout) northPanel.getLayout();
        layout.columnWidths = new int[] {DEFAULT_LABEL_WIDTH, 0};
        // CHOOSE QR CODE TYPE
        JPanel modePanel = new JPanel(new BorderLayout());
        mecardRadio.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(mecardRadio);
        group.add(freeRadio);
        JPanel radioButtonsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcRadio = new GridBagConstraints();
        gbcRadio.gridx = 0;
        gbcRadio.gridy = 0;
        gbcRadio.insets = new Insets(0, 0, 0, RADIO_BUTTON_GAP);
        gbcRadio.anchor = GridBagConstraints.CENTER;
        radioButtonsPanel.add(mecardRadio, gbcRadio);
        gbcRadio.gridx = 1;
        gbcRadio.insets = new Insets(0, 0, 0, 0);
        radioButtonsPanel.add(freeRadio, gbcRadio);
        modePanel.add(radioButtonsPanel, BorderLayout.WEST);
        modePanel.add(update, BorderLayout.EAST);
        update.setEnabled(false);
        update.setToolTipText(
                "<html>Recherche de mise à jour<br>" + LATEST_RELEASES_REPO_URL + "</html>");
        update.addActionListener(
                e -> BrowserHelper.INSTANCE.openInBrowser(LATEST_RELEASES_REPO_URL));
        SwingWorker<Boolean, Void> worker =
                VersionService.INSTANCE.checkLatestVersion(version, update);
        worker.execute();
        mecardRadio.addActionListener(e -> switchMode(Mode.MECARD));
        freeRadio.addActionListener(e -> switchMode(Mode.FREE));
        addRow(northPanel, grid, "Code QR", "Choisir le type de code QR à générer.", modePanel);
        JPanel freePanel = new JPanel(new GridBagLayout());
        initFreeCard(freePanel, new GridBagConstraints());
        JPanel mecardPanel = new JPanel(new GridBagLayout());
        initMecard(mecardPanel, new GridBagConstraints());
        // CARD (MECARD et FREE)
        grid.gridy += 1;
        grid.gridx = 0;
        grid.weightx = 1.0;
        grid.gridwidth = GridBagConstraints.HORIZONTAL;
        cardPanel.add(mecardPanel, Mode.MECARD.text());
        cardPanel.add(freePanel, Mode.FREE.text());
        northPanel.add(cardPanel, grid);
        // COMMON
        grid.gridwidth = GridBagConstraints.BOTH;
        addRow(
                northPanel,
                grid,
                "Chemin du logo",
                "Saisir le chemin vers votre logo ou ne rien mettre.",
                logoField);
        browseButton.addActionListener(this::browseLogo);
        grid.gridx = 2;
        grid.weightx = 0;
        northPanel.add(browseButton, grid);
        grid.gridy += 1;
        addRow(
                northPanel,
                grid,
                "Dimension en pixels ⚠",
                "<html>Saisir la dimension du côté du code QR en pixels.<br>⚠ Une dimension trop"
                        + " grande surcharge la mémoire et dégrade les performances de"
                        + " l'application.</html>",
                sizeField);
        addRow(
                northPanel,
                grid,
                "Marge ⚠",
                "<html>Marge extérieure entre 0 et 10."
                        + "<br>⚠ Risque de gêner la détection.</html>",
                marginSlider);
        addRow(
                northPanel,
                grid,
                "Visibilité du logo ⚠",
                "<html>Pourcentage de visibilité du logo par rapport au code QR."
                        + "<br>⚠ Risque de gêner la détection.</html>",
                ratioSlider);
        JPanel colorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        qrColorButton.setIconTextGap(BUTTON_COLOR_ICON_TEXT_GAP);
        bgColorButton.setIconTextGap(BUTTON_COLOR_ICON_TEXT_GAP);
        qrColorButton.setIcon(createColorIcon(qrColorButton, Color.BLACK));
        bgColorButton.setIcon(createColorIcon(bgColorButton, Color.WHITE));
        qrColorButton.addActionListener(e -> chooseColor(qrColorButton, true));
        bgColorButton.addActionListener(e -> chooseColor(bgColorButton, false));
        qrColorButton.setToolTipText(
                "<html>Couleur des modules.<br>⚠ Le code QR ne fonctionnera que"
                        + " si le contraste avec le fond est suffisant.</html>");
        bgColorButton.setToolTipText(
                "<html>Couleur du fond.<br>⚠ Le code QR ne fonctionnera que"
                        + " si le contraste avec les modules est suffisant.</html>");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, COLOR_BUTTONS_GAP);
        colorPanel.add(bgColorButton, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        colorPanel.add(qrColorButton, gbc);
        addRow(
                northPanel,
                grid,
                "Couleurs ⚠",
                "<html>Saisir la couleur du fond et des modules.<br>⚠ Le code QR ne fonctionnera"
                        + " que si le contraste est suffisant.</html>",
                colorPanel);

        addRow(
                northPanel,
                grid,
                "Modules ronds ⚠",
                "<html>Activer les modules arrondis du code QR.<br>⚠ Risque de gêner la"
                        + " détection.</html>",
                roundedModulesCheckBox);
        grid.gridy += 1;
        // Generate
        Dimension d =
                new Dimension(
                        generateButton.getWidth(),
                        generateButton.getHeight() + GENERATE_BUTTON_EXTRA_HEIGHT);
        generateButton.setMinimumSize(d);
        generateButton.setPreferredSize(d);
        generateButton.setMaximumSize(d);
        generateButton.setToolTipText("Enregistrer le code QR");
        generateButton.addActionListener(this::generateQrCode);
        generateButton.setEnabled(false);
        northPanel.add(generateButton, grid);
        // CENTER (code QR dynamique)
        qrCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        qrCodeLabel.setVerticalAlignment(SwingConstants.CENTER);
        addWindowStateListener(e -> SwingUtilities.invokeLater(this::updateQrCodeSize));
        addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        SwingUtilities.invokeLater(() -> updateQrCodeSize());
                    }
                });
        // SOUTH (marge)
        southSpacer.setPreferredSize(new Dimension(0, DEFAULT_GAP));
        initializeAutomaticQRCodeRendering();
        // MAIN (with scroll)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(northPanelWrapper, BorderLayout.NORTH);
        mainPanel.add(qrCodeLabel, BorderLayout.CENTER);
        mainPanel.add(southSpacer, BorderLayout.SOUTH);
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(VERTICAL_SCROLL_UNIT_INCREMENT);
        add(scrollPane, BorderLayout.CENTER);
        // Ajuste et centre la fenêtre
        pack();
        setSize(getWidth(), getHeight() + QR_CODE_LABEL_DEFAULT_SIZE);
        setLocationRelativeTo(null);
        // setName pour la simulation pendant la génération de configuration Native Image
        nameField.setName("nameField");
        browseButton.setName("browseButton");
        ratioSlider.setName("ratioSlider");
        qrColorButton.setName("qrColorButton");
        freeRadio.setName("freeRadio");
        freeField.setName("freeField");
    }

    /**
     * Releases all runtime resources associated with the QR code preview and generation workflow.
     *
     * <p>Stops active timers, cancels background workers, and flushes the original QR code image to
     * ensure memory is released and no lingering tasks remain.
     */
    private void cleanupResources() {
        stopAllTimers();
        cancelAllWorkers();
        freeQrOriginalAndQrCodeLabel();
    }

    /**
     * Releases the QR code image and its label icon to free resources. Must be called on the EDT.
     */
    private void freeQrOriginalAndQrCodeLabel() {
        QrCodeBufferedImage.INSTANCE.freeQrOriginal();
        QrCodeIconUtil.INSTANCE.disposeIcon(qrCodeLabel);
    }

    /**
     * Stops and clears all active Swing timers used in the QR code workflow.
     *
     * <p>Ensures memory is released and prevents lingering scheduled tasks by nullifying timer
     * references.
     */
    private void stopAllTimers() {
        Loader.INSTANCE.disposeWaitIconTimer();
        if (resizeDebounceTimer != null) {
            resizeDebounceTimer.stop();
            resizeDebounceTimer = null;
        }
        if (previewDebounceTimer != null) {
            previewDebounceTimer.stop();
            previewDebounceTimer = null;
        }
    }

    /**
     * Cancels all active background workers and ensures proper cleanup.
     *
     * <p>Interrupts ongoing tasks, triggers {@code done()} for resource release, and nullifies
     * worker references to prevent memory leaks or thread persistence.
     */
    private void cancelAllWorkers() {
        cancelPreviousResizeWorker();
        cancelActivePreviewWorker();
    }

    /**
     * Overrides {@code dispose()} to ensure explicit resource cleanup before window disposal.
     *
     * <p>Stops timers, cancels background workers, and flushes image resources to prevent memory
     * leaks and lingering threads.
     */
    @Override
    public void dispose() {
        cleanupResources();
        super.dispose();
    }

    /**
     * Constructs the application window title using the name, version, and organization as
     * retrieved from the version properties.
     *
     * @return a formatted title string for display in the UI
     */
    private static String buildTitle() {
        getManifestKeys();
        return String.format("\uD83D\uDCF1 %s v%s • %s", name, version, organization);
    }

    /**
     * Schedules a debounced QR code resize operation based on available vertical space. Cancels any
     * ongoing resize task and triggers a new one after a short delay to avoid excessive redraws
     * during rapid layout changes (e.g. window resizing).
     */
    private void updateQrCodeSize() {
        if (resizeDebounceTimer != null && resizeDebounceTimer.isRunning()) {
            resizeDebounceTimer.restart();
            return;
        }
        resizeDebounceTimer = new Timer(RESIZE_DEBOUNCE_DELAY_MS, e -> handleResize());
        resizeDebounceTimer.setRepeats(false);
        resizeDebounceTimer.start();
    }

    /**
     * Handles the actual QR code resize logic after debounce delay. Validates available space,
     * input data, and worker state before launching a new resize task.
     */
    private void handleResize() {
        int squareSize =
                getHeight()
                        - northPanelWrapper.getHeight()
                        - southSpacer.getHeight()
                        - DEFAULT_GAP * 3;
        if (squareSize < QR_CODE_LABEL_DEFAULT_SIZE) {
            squareSize = QR_CODE_LABEL_DEFAULT_SIZE;
        }
        qrCodeLabel.setPreferredSize(new Dimension(squareSize, squareSize));
        QrDataResult qrData = BuildQRDataService.INSTANCE.buildQrData(currentMode, buildQrInput());
        if (isInvalidQrData(qrData)) {
            return;
        }
        if (QrCodeBufferedImage.INSTANCE.getQrOriginal() == null) {
            return;
        }
        cancelPreviousResizeWorker();
        prepareForResize();
        launchResizeWorker(squareSize);
    }

    /** Determines whether the QR data is invalid or null and handles cleanup if needed. */
    private boolean isInvalidQrData(QrDataResult qrData) {
        if (Checker.INSTANCE.checkNPE(qrData, "isInvalidQrData", QR_DATA)
                || StringUtils.isBlank(qrData.data())) {
            Loader.INSTANCE.stopWaitIcon();
            qrCodeLabel.setIcon(null);
            freeQrOriginalAndQrCodeLabel();
            return true;
        }
        return false;
    }

    /** Clears previous QR code visuals and starts wait icon animation. */
    private void prepareForResize() {
        QrCodeIconUtil.INSTANCE.disposeIcon(qrCodeLabel);
        qrCodeLabel.setIcon(null);
        SwingUtilities.invokeLater(Loader.INSTANCE::startAndAdjustWaitIcon);
    }

    /**
     * Cancels any ongoing resize worker if active and waits for its proper termination. Ensures no
     * overlapping resize tasks run concurrently.
     */
    private void cancelPreviousResizeWorker() {
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
        Loader.INSTANCE.stopWaitIcon();
    }

    /**
     * Performs the image resizing operation on a background thread. Handles the creation and
     * drawing of the scaled image with proper resource management. Responds to cancellation
     * requests promptly.
     *
     * @param squareSize the target height for resizing the QR code image
     * @return an ImageIcon containing the resized image or null if cancelled or parameters invalid
     */
    private ImageIcon resizeImageInBackground(int squareSize) {
        if (squareSize <= 0
                || QrCodeBufferedImage.INSTANCE.getQrOriginal() == null
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
            g2d.drawImage(
                    QrCodeBufferedImage.INSTANCE.getQrOriginal(),
                    0,
                    0,
                    squareSize,
                    squareSize,
                    null);
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
     * Creates a background task to resize the QR code image to the specified height. Ensures the
     * resize is performed off the EDT and updates the label only if the task completes
     * successfully. Disposes of obsolete icons and guarantees wait icon cleanup.
     *
     * @param height the target height for the resized image
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
                Loader.INSTANCE.stopWaitIcon();
                handleResizeWorkerCompletion(this);
            }
        };
    }

    /**
     * Handles resize worker completion: checks cancellation, disposes obsolete icons, updates UI,
     * and reports errors.
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
            Popup.INSTANCE.showDialog("Pas de redimensionnement\n", ex.getMessage(), ERREUR);
        }
    }

    /**
     * Manages the lifecycle of the resize worker by cancelling any existing worker, creating a new
     * SwingWorker to resize the image asynchronously, and updating the UI upon completion or
     * failure.
     *
     * @param height the target height for the resized image
     */
    private void launchResizeWorker(int height) {
        cancelPreviousResizeWorker();
        resizeWorker = createResizeWorker(height);
        resizeWorker.execute();
    }

    /** Switches between MECARD and FREE modes and updates the QR code preview accordingly. */
    private void switchMode(Mode mode) {
        if (Checker.INSTANCE.checkNPE(mode, "switchMode", "mode")) {
            return;
        }
        currentMode = mode;
        cardLayout.show(cardPanel, mode.text());
        updatePreviewQRCode();
    }

    /** Initializes the MECARD input panel with labeled text fields for contact information. */
    private void initMecard(JPanel mecardPanel, GridBagConstraints grid) {
        if (Checker.INSTANCE.checkNPE(mecardPanel, "initMecard", "mecardPanel")
                || Checker.INSTANCE.checkNPE(grid, "initMecard", "grid")) {
            return;
        }
        GridBagLayout layout = (GridBagLayout) mecardPanel.getLayout();
        layout.columnWidths = new int[] {DEFAULT_LABEL_WIDTH, 0};
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.weightx = 1;
        grid.gridy = -1;
        addRow(mecardPanel, grid, "Nom, prénom", "Saisir le nom et prénom.", nameField);
        addRow(mecardPanel, grid, "Organisation", "Saisir le nom de l'entreprise.", orgField);
        addRow(mecardPanel, grid, "Téléphone", "Saisir le numéro de téléphone.", phoneField);
        addRow(mecardPanel, grid, "Email", "Saisir le mail.", emailField);
        addRow(mecardPanel, grid, "Adresse", "Saisir l'adresse postale.", adrField);
        addRow(
                mecardPanel,
                grid,
                "Lien / URL",
                "Saisir le lien (site web, profil, portfolio, réseau social, etc.).",
                urlField);
    }

    /**
     * Sets up the FREE mode panel with a multiline text area wrapped in a scroll pane, and
     * configures layout, size, and line wrapping.
     */
    private void initFreeCard(JPanel freePanel, GridBagConstraints grid) {
        if (Checker.INSTANCE.checkNPE(freePanel, "initFreeCard", "freePanel")
                || Checker.INSTANCE.checkNPE(grid, "initFreeCard", "grid")) {
            return;
        }
        GridBagLayout layout = (GridBagLayout) freePanel.getLayout();
        layout.columnWidths = new int[] {DEFAULT_LABEL_WIDTH, 0};
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.weightx = 1;
        grid.gridy = -1;
        freeField.setWrapStyleWord(true);
        freeField.setLineWrap(true);
        Dimension size =
                new Dimension(
                        charWidth * TEXT_FIELDS_COLUMNS, charHeight * MULTILINE_TEXT_FIELDS_ROWS);
        freeScrollPane.setPreferredSize(size);
        freeScrollPane.setMinimumSize(size);
        freeScrollPane.setMaximumSize(size);
        addRow(
                freePanel,
                grid,
                "Saisie libre",
                "Saisir le texte correspondant au code QR.",
                freeScrollPane);
    }

    /**
     * Initializes automatic QR code rendering for all input fields and controls.
     *
     * <p>Any change in text fields, sliders, or the rounded modules checkbox will trigger an
     * immediate preview update of the QR code without saving to a file.
     */
    private void initializeAutomaticQRCodeRendering() {
        DocumentListener docListener = simpleDocumentListener(this::updatePreviewQRCode);
        DocumentListener validationListener =
                simpleDocumentListener(this::updateGenerateButtonState);
        JTextField[] textFields = {
            nameField, phoneField, emailField, orgField, adrField, urlField, logoField, sizeField
        };
        for (JTextField field : textFields) {
            field.getDocument().addDocumentListener(docListener);
        }
        freeField.getDocument().addDocumentListener(docListener);
        nameField.getDocument().addDocumentListener(validationListener);
        freeField.getDocument().addDocumentListener(validationListener);
        marginSlider.addChangeListener(e -> updatePreviewQRCode());
        ratioSlider.addChangeListener(e -> updatePreviewQRCode());
        roundedModulesCheckBox.addActionListener(e -> updatePreviewQRCode());
    }

    /**
     * Validates input requirements and toggles the generate button accordingly.
     *
     * <p>Disables the button if {@code freeField} is empty in {@code FREE} mode, or {@code
     * nameField} is empty in {@code MECARD} mode.
     */
    private void updateGenerateButtonState() {
        boolean isFreeFieldEmpty = freeField.getText().trim().isEmpty();
        boolean isNameFieldEmpty = nameField.getText().trim().isEmpty();
        boolean disable =
                currentMode == Mode.MECARD && isNameFieldEmpty
                        || currentMode == Mode.FREE && isFreeFieldEmpty;
        generateButton.setEnabled(!disable);
    }

    /**
     * Creates a minimal DocumentListener that executes the given action on any document change.
     *
     * @param action the Runnable to execute on insert, remove, or change events
     * @return a DocumentListener that triggers the action, or null if the action is null
     */
    private static DocumentListener simpleDocumentListener(Runnable action) {
        if (Checker.INSTANCE.checkNPE(action, "simpleDocumentListener", "action")) {
            return null;
        }
        return new javax.swing.event.DocumentListener() {
            private void update() {
                SwingUtilities.invokeLater(action);
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }
        };
    }

    /**
     * Updates the tooltip of the ratio slider to reflect its current value as a percentage. This
     * provides immediate feedback to the user on the selected ratio when hovering over the slider.
     */
    private void setRatioSliderTooltipValue() {
        ratioSlider.setToolTipText(ratioSlider.getValue() + "%");
    }

    /**
     * Opens a color chooser dialog and updates the selected color.
     *
     * @param button the button representing the color to update
     * @param isQrColor true if updating the QR code color; false for the background color
     */
    private void chooseColor(JButton button, boolean isQrColor) {
        if (Checker.INSTANCE.checkNPE(button, "chooseColor", "button")) {
            return;
        }
        Color initial = isQrColor ? qrColor : bgColor;
        Color chosen = JColorChooser.showDialog(this, "Choisir la couleur", initial);
        if (chosen != null) {
            if (isQrColor) {
                qrColor = chosen;
            } else {
                bgColor = chosen;
            }
            button.setIcon(createColorIcon(button, chosen));
            button.setText(colorToHex(chosen));
            updatePreviewQRCode();
        }
    }

    /**
     * Returns an icon showing a filled rectangle of the given color with a visible border. Ensures
     * previous icon is flushed and graphics resources are released explicitly.
     *
     * @param button the button whose old icon will be flushed and reset
     * @param color the color to display
     * @return an Icon with a filled rectangle and a visible border, or {@code null} if {@code
     *     button} or {@code color} is {@code null}
     */
    private Icon createColorIcon(JButton button, Color color) {
        if (Checker.INSTANCE.checkNPE(button, "createColorIcon", "button")
                || Checker.INSTANCE.checkNPE(color, "createColorIcon", "color")) {
            return null;
        }
        ImageIcon oldIcon = (ImageIcon) button.getIcon();
        if (oldIcon != null) {
            Image oldImage = oldIcon.getImage();
            if (oldImage != null) {
                oldImage.flush();
            }
            button.setIcon(null);
        }
        final int size = BUTTON_ICON_COLOR_SIZE;
        final float strokeWidth = 2f;
        final int offset = (int) (strokeWidth / 2);
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = null;
        try {
            g2 = image.createGraphics();
            g2.setColor(color);
            g2.fillRect(0, 0, size, size);
            g2.setColor(Color.decode("#003f5e"));
            g2.setStroke(new BasicStroke(strokeWidth));
            g2.drawRect(offset, offset, size - (int) strokeWidth, size - (int) strokeWidth);
        } finally {
            if (g2 != null) {
                g2.dispose();
            }
        }
        return new ImageIcon(image);
    }

    /**
     * Adds a labeled text field row to a panel using GridBagLayout.
     *
     * @param panel the container panel
     * @param gbc the GridBagConstraints to configure layout
     * @param labelText the text for the label
     * @param tooltipText the tooltip text for the label
     * @param component the JComponent to add
     */
    private void addRow(
            JPanel panel,
            GridBagConstraints gbc,
            String labelText,
            String tooltipText,
            JComponent component) {
        if (Checker.INSTANCE.checkNPE(panel, ADD_ROW, "panel")
                || Checker.INSTANCE.checkNPE(gbc, ADD_ROW, "gbc")
                || Checker.INSTANCE.checkNPE(component, ADD_ROW, "component")
                || Checker.INSTANCE.checkNPE(labelText, ADD_ROW, "labelText")
                || Checker.INSTANCE.checkNPE(tooltipText, ADD_ROW, "tooltipText")) {
            return;
        }
        gbc.gridx = 0;
        gbc.gridy += 1;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setToolTipText(tooltipText);
        if (component == nameField || component == freeScrollPane) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        }
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }

    /**
     * Opens a file chooser to select a logo image and updates the logo text field.
     *
     * @param e the action event triggering the file chooser
     */
    void browseLogo(ActionEvent e) {
        if (Checker.INSTANCE.checkNPE(e, "browseLogo", "e")) {
            return;
        }
        File selected = chooseLogoFile();
        if (selected != null) {
            logoField.setText(selected.getAbsolutePath());
        }
    }

    /**
     * Chooses a file via JFileChooser. Protected to allow mocking in tests.
     *
     * @return the selected file or null if canceled
     */
    protected File chooseLogoFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Sélectionner le logo");
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "Images", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Generates and saves a QR code as a PNG file using the current input and configuration.
     * Ensures resource cleanup, user feedback, and file conflict resolution.
     *
     * @param e the triggering {@link ActionEvent}
     */
    private void generateQrCode(ActionEvent e) {
        if (Checker.INSTANCE.checkNPE(e, GENERATE_QR_CODE, "e")) {
            return;
        }
        try {
            QrDataResult qrData =
                    BuildQRDataService.INSTANCE.buildQrData(currentMode, buildQrInput());
            if (Checker.INSTANCE.checkNPE(qrData, GENERATE_QR_CODE, QR_DATA)) {
                return;
            }
            Objects.requireNonNull(qrData, "Dans generateQrCode qrData ne doit pas être null");
            if (StringUtils.isBlank(qrData.data())) {
                Popup.INSTANCE.showDialog("", "Aucune donnée à encoder", "Information");
                return;
            }
            File logoFile = logoField.getText().isBlank() ? null : new File(logoField.getText());
            int size = sizeFieldCheck();
            marginFieldCheck();
            ratioFieldCheck();
            boolean roundedModules = roundedModulesCheckBox.isSelected();
            QrConfig config =
                    new QrConfig(
                            logoFile, size, imageRatio, qrColor, bgColor, roundedModules, margin);
            BufferedImage qr = generateQrCodeImage(qrData.data(), config);
            QrCodeBufferedImage.INSTANCE.updateQrOriginal(qr);
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Enregistrer votre code QR en tant que PNG");
            chooser.setSelectedFile(new File(qrData.defaultFileName()));
            chooser.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File selectedFile = getSelectedPngFile(chooser);
            Objects.requireNonNull(
                    selectedFile, "Dans generateQrCode selectedFile ne doit pas être null");
            File output = resolveFileNameConflict(selectedFile);
            if (Checker.INSTANCE.checkNPE(output, GENERATE_QR_CODE, "output")
                    || Checker.INSTANCE.checkNPE(qr, GENERATE_QR_CODE, "qr")) {
                return;
            }
            Objects.requireNonNull(qr, "Dans generateQrCode qr ne doit pas être null");
            Objects.requireNonNull(output, "Dans generateQrCode output ne doit pas être null");
            try (OutputStream os = new FileOutputStream(output)) {
                ImageIO.write(qr, "png", os);
            }
            Popup.INSTANCE.showDialog(
                    "Code QR enregistré dans\n", output.getAbsolutePath(), "Confirmation");
        } catch (OutOfMemoryError oom) {
            SwingUtilities.invokeLater(
                    () ->
                            Popup.INSTANCE.showDialog(
                                    "Manque de mémoire\n", oom.getMessage(), ERREUR));

        } catch (WriterException we) {
            SwingUtilities.invokeLater(
                    () ->
                            Popup.INSTANCE.showDialog(
                                    "Pas de génération du QR Code\n", we.getMessage(), ERREUR));

        } catch (IOException ioe) {
            SwingUtilities.invokeLater(
                    () ->
                            Popup.INSTANCE.showDialog(
                                    "Pas de lecture/écriture de fichier\n",
                                    ioe.getMessage(),
                                    ERREUR));
        }
    }

    /**
     * Schedules a debounced QR code preview generation based on current input fields and
     * configuration. Restarts the debounce timer if already running to avoid excessive regeneration
     * during rapid user input. Only performs cleanup and cancellation when actually launching a new
     * worker.
     */
    private void updatePreviewQRCode() {
        if (previewDebounceTimer != null && previewDebounceTimer.isRunning()) {
            previewDebounceTimer.restart();
            return;
        }
        cancelActivePreviewWorker();
        freeQrOriginalAndQrCodeLabel();
        previewDebounceTimer = new Timer(PREVIEW_DEBOUNCE_DELAY_MS, e -> launchPreviewWorker());
        previewDebounceTimer.setRepeats(false);
        previewDebounceTimer.start();
    }

    /**
     * Launches a new preview worker to generate the QR code image in the background. Clears the
     * current display and starts the wait icon animation before execution.
     */
    private void launchPreviewWorker() {
        qrCodeLabel.setIcon(null);
        SwingUtilities.invokeLater(Loader.INSTANCE::startAndAdjustWaitIcon);
        previewWorker = createPreviewWorker();
        previewWorker.execute();
    }

    /** Cancels and flushes any active preview worker to ensure clean restart. */
    private void cancelActivePreviewWorker() {
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

    /**
     * Creates a background task to generate a QR code image using current input and configuration.
     * Ensures the image is generated off the EDT and updates the label only if the task completes
     * successfully. Flushes cancelled result and guarantees wait icon cleanup.
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
     * Handles preview worker completion: checks cancellation, flushes result, updates UI, and
     * reports errors.
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
            QrCodeBufferedImage.INSTANCE.updateQrOriginal(img);
            if (QrCodeBufferedImage.INSTANCE.getQrOriginal() != null) {
                updateQrCodeSize();
            } else {
                qrCodeLabel.setIcon(null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (CancellationException | ExecutionException ex) {
            if (cancelledOrStale) {
                return;
            }
            Popup.INSTANCE.showDialog("Pas d'affichage\n", ex.getMessage(), ERREUR);
        }
    }

    /**
     * Builds and returns the QR code image based on current input and configuration. Returns null
     * if the thread is interrupted or if input is invalid. Performs cancellation checks only at
     * critical points to minimize overhead while ensuring responsiveness.
     *
     * @return a BufferedImage containing the QR code, or null if cancelled or invalid
     */
    private BufferedImage buildPreviewImage() {
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }
        try {
            QrDataResult qrData =
                    BuildQRDataService.INSTANCE.buildQrData(currentMode, buildQrInput());
            if (Thread.currentThread().isInterrupted()
                    || Checker.INSTANCE.checkNPE(qrData, GENERATE_QR_CODE, QR_DATA)) {
                return null;
            }
            Objects.requireNonNull(qrData, "Dans buildPreviewImage qrData ne doit pas être null");
            String data = qrData.data();
            if (StringUtils.isBlank(data)) {
                return null;
            }
            File logoFile = logoField.getText().isBlank() ? null : new File(logoField.getText());
            int size = sizeFieldCheck();
            marginFieldCheck();
            ratioFieldCheck();
            QrConfig config =
                    new QrConfig(
                            logoFile,
                            size,
                            imageRatio,
                            qrColor,
                            bgColor,
                            roundedModulesCheckBox.isSelected(),
                            margin);
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            return generateQrCodeImage(qrData.data(), config);
        } catch (Exception ex) {
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            SwingUtilities.invokeLater(
                    () ->
                            Popup.INSTANCE.showDialog(
                                    "Pas de rendu du code QR\n", ex.getMessage(), ERREUR));
            return null;
        }
    }

    /**
     * Builds a {@link QrInput} from the current field values.
     *
     * @return a populated {@link QrInput} instance
     */
    private QrInput buildQrInput() {
        return new QrInput(
                nameField.getText(),
                phoneField.getText(),
                emailField.getText(),
                orgField.getText(),
                adrField.getText(),
                urlField.getText(),
                freeField.getText());
    }

    /**
     * Returns the selected file from the chooser, ensuring it has a .png extension.
     *
     * @param chooser the JFileChooser to get the file from
     * @return a File ending with .png
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
     * Resolves file name conflicts by asking the user to overwrite or automatically renaming.
     *
     * @param file the initial file
     * @return a File ready to be written
     */
    private File resolveFileNameConflict(File file) {
        if (Checker.INSTANCE.checkNPE(file, "resolveFileNameConflict", "file")) {
            return null;
        }
        if (file.exists()) {
            int choice =
                    Popup.INSTANCE.showYesNoConfirmDialog(
                            this,
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

    /**
     * Validates and updates the image ratio from the corresponding text field. Resets to the
     * default value (0.27) if the input is invalid or out of range [0,1].
     */
    void ratioFieldCheck() {
        try {
            imageRatio = (double) ratioSlider.getValue() / MAX_PERCENTAGE;
            if (imageRatio < 0 || imageRatio > 1) {
                imageRatio = DEFAULT_IMAGE_RATIO;
            }
        } catch (NumberFormatException ex) {
            imageRatio = DEFAULT_IMAGE_RATIO;
        }
    }

    /**
     * Validates and updates the QR code margin from the corresponding text field. Clamps the value
     * to the range [0, 10] and defaults to 3 if the input is invalid.
     */
    void marginFieldCheck() {
        try {
            margin = marginSlider.getValue();
            if (margin < 0) {
                margin = 0;
            }
            if (margin > MARGE_MAXIMUM_VALUE) {
                margin = MARGE_MAXIMUM_VALUE;
            }
        } catch (NumberFormatException ex) {
            margin = 3;
        }
    }

    /**
     * Validates and returns the QR code size from the corresponding text field. Defaults to 400 if
     * the input is invalid or non-positive.
     *
     * @return the validated QR code size in pixels
     */
    int sizeFieldCheck() {
        int size;
        try {
            size = Integer.parseInt(sizeField.getText());
            if (size < MINIMUM_QR_CODE_SIZE) {
                size = MINIMUM_QR_CODE_SIZE;
            }
        } catch (NumberFormatException ex) {
            size = DEFAULT_QR_CODE_SIZE;
            sizeField.setText(SIZE_FIELD_DEFAULT);
        }
        return size;
    }

    /**
     * Generates a QR code image with optional logo and custom styling.
     *
     * <p>The QR code is generated using the ZXing library with high error correction, allowing a
     * logo to be embedded without breaking scanability. Modules, colors, margin, and rounded or
     * square style are applied according to the provided configuration.
     *
     * @param data The formatted string to encode in the QR code.
     * @param config QR code configuration containing size, colors, margin, module style, and
     *     optional logo.
     * @return A BufferedImage containing the generated QR code.
     * @throws WriterException If encoding the MECARD text into a QR code fails.
     * @throws IOException If reading the logo file fails.
     * @throws OutOfMemoryError If the requested size exceeds available memory.
     */
    static BufferedImage generateQrCodeImage(String data, QrConfig config)
            throws WriterException, IOException {
        if (Checker.checkStaticNPE(config, GENERATE_QR_CODE_IMAGE, CONFIG)
                || Checker.checkStaticNPE(data, GENERATE_QR_CODE_IMAGE, "data")) {
            return null;
        }
        final int size = config.size();
        validateMemoryForImageSize(size);
        BitMatrix matrix = createMatrix(data, config.margin());
        if (Checker.checkStaticNPE(matrix, GENERATE_QR_CODE_IMAGE, MATRIX)) {
            return null;
        }
        BufferedImage qrImage = null;
        Graphics2D g = null;
        try {
            qrImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            g = qrImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            fillBackground(g, size, config.bgColor());
            drawModules(g, matrix, config);
            Objects.requireNonNull(matrix, "Dans generateQrCodeImage matrix ne doit pas être null");
            drawFinderPatterns(g, matrix.getWidth(), config);
            drawLogoIfPresent(g, config);
        } catch (OutOfMemoryError oom) {
            if (g != null) {
                g.dispose();
            }
            if (qrImage != null) {
                qrImage.flush();
            }
            throw new OutOfMemoryError(
                    String.format(
                            "Mémoire insuffisante pour générer une image de %dx%d pixels.%nTaille"
                                    + " estimée %d Mo.%nMémoire disponible %d Mo.",
                            size, size, estimateImageMemoryMB(size), getAvailableMemoryMB()));
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
        return qrImage;
    }

    /**
     * Validates that sufficient memory is available to generate a square image of the given size.
     * Applies a hard limit of 200M px and ensures a minimum memory margin before allocation.
     *
     * @param size the width and height of the square image in pixels
     * @throws OutOfMemoryError if the image exceeds pixel limits or available memory is
     *     insufficient
     */
    private static void validateMemoryForImageSize(int size) {
        final long MAX_PIXELS = 200_000_000L;
        long totalPixels = (long) size * size;
        if (totalPixels > MAX_PIXELS) {
            throw new OutOfMemoryError(
                    String.format(
                            """

                            Dimension trop grande:
                            %dx%d pixels (%,d pixels).
                            Maximum autorisé: %,d pixels.
                            """,
                            size, size, totalPixels, MAX_PIXELS));
        }
        long estimatedMB = estimateImageMemoryMB(size);
        long availableMB = getAvailableMemoryMB();
        if (estimatedMB > availableMB) {
            throw new OutOfMemoryError(
                    String.format(
                            "Mémoire insuffisante pour générer une image de %dx%d pixels.%nMémoire"
                                    + " nécessaire %d Mo.%nMémoire disponible %d Mo.%nRéduire la"
                                    + " dimension souhaitée.",
                            size, size, estimatedMB, availableMB));
        }
    }

    /**
     * Estimates the memory required to create a BufferedImage.
     *
     * @param size The width and height of the square image.
     * @return Estimated memory in megabytes.
     */
    private static long estimateImageMemoryMB(int size) {
        long totalPixels = (long) size * size;
        long bytesRequired = totalPixels * 4;
        return bytesRequired / (BYTES_PER_KILOBYTE * BYTES_PER_KILOBYTE);
    }

    /**
     * Gets the available memory in the JVM with safety margin for image generation.
     *
     * @return Available memory in megabytes, minus the reserved safety margin. Returns 0 if not
     *     enough memory is available.
     */
    private static long getAvailableMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = allocatedMemory - freeMemory;
        long availableMemory = maxMemory - usedMemory;
        long safeAvailableMemory =
                availableMemory
                        - (AVAILABLE_MEMORY_TO_GENERATE_IMAGE
                                * BYTES_PER_KILOBYTE
                                * BYTES_PER_KILOBYTE);
        return Math.max(0, safeAvailableMemory) / (BYTES_PER_KILOBYTE * BYTES_PER_KILOBYTE);
    }

    /**
     * Creates a QR code matrix for the given text.
     *
     * @param text The string to encode in the QR code.
     * @param margin The outer margin of the QR code in modules.
     * @return A BitMatrix representing the encoded QR code.
     * @throws WriterException If encoding fails.
     */
    private static BitMatrix createMatrix(String text, int margin) throws WriterException {
        if (Checker.checkStaticNPE(text, "createMatrix", "text")) {
            return null;
        }
        Map<EncodeHintType, Object> hints =
                Map.of(
                        EncodeHintType.CHARACTER_SET,
                        "UTF-8",
                        EncodeHintType.ERROR_CORRECTION,
                        ErrorCorrectionLevel.H,
                        EncodeHintType.MARGIN,
                        margin);
        return new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints);
    }

    /**
     * Fills the entire QR code area with the specified background color.
     *
     * @param g The graphics context used for drawing.
     * @param size The width and height of the QR code area in pixels.
     * @param bgColor The background color to fill.
     */
    private static void fillBackground(Graphics2D g, int size, Color bgColor) {
        if (Checker.checkStaticNPE(g, "fillBackground", "g")
                || Checker.checkStaticNPE(bgColor, "fillBackground", BG_COLOR)) {
            return;
        }
        g.setColor(bgColor);
        g.fillRect(0, 0, size, size);
    }

    /**
     * Renders all QR code modules onto the provided graphics context.
     *
     * <p>Modules that are part of finder patterns or the central logo area are skipped. Supports
     * rounded or square modules according to configuration.
     *
     * @param g the graphics context used for drawing
     * @param matrix the QR code bit matrix representing module positions
     * @param config the QR code configuration including size, colors, module shape, margin, and
     *     logo ratio
     */
    private static void drawModules(Graphics2D g, BitMatrix matrix, QrConfig config) {
        if (Checker.checkStaticNPE(g, DRAW_MODULES, "g")
                || Checker.checkStaticNPE(matrix, DRAW_MODULES, MATRIX)
                || Checker.checkStaticNPE(config, DRAW_MODULES, CONFIG)) {
            return;
        }
        int matrixWidth = matrix.getWidth();
        int matrixHeight = matrix.getHeight();
        double moduleSizeX = (double) config.size() / matrixWidth;
        double moduleSizeY = (double) config.size() / matrixHeight;
        int whiteBoxSize = (int) (config.size() * config.imageRatio());
        int whiteBoxX = (config.size() - whiteBoxSize) / 2;
        int whiteBoxY = (config.size() - whiteBoxSize) / 2;
        g.setColor(config.qrColor());
        ModuleContext ctx =
                new ModuleContext(
                        config, matrixWidth, matrixHeight, whiteBoxX, whiteBoxY, whiteBoxSize);
        for (int y = 0; y < matrixHeight; y++) {
            for (int x = 0; x < matrixWidth; x++) {
                if (shouldSkipModule(x, y, matrix, ctx)) {
                    continue;
                }
                drawModule(g, x, y, moduleSizeX, moduleSizeY, config);
            }
        }
    }

    /**
     * Determines whether a QR code module should be skipped during rendering.
     *
     * <p>A module is skipped if it is unset, part of a finder pattern, or overlaps the central logo
     * area.
     *
     * @param x the module's x-coordinate in the QR matrix
     * @param y the module's y-coordinate in the QR matrix
     * @param matrix the QR code bit matrix
     * @param ctx the module context containing configuration, matrix dimensions, and logo box
     * @return true if the module should be skipped, false otherwise
     */
    private static boolean shouldSkipModule(int x, int y, BitMatrix matrix, ModuleContext ctx) {
        if (Checker.checkStaticNPE(matrix, SHOULD_SKIP_MODULE, MATRIX)
                || Checker.checkStaticNPE(ctx, SHOULD_SKIP_MODULE, "ctx")
                || Checker.checkStaticNPE(ctx.config(), SHOULD_SKIP_MODULE, "ctx.config")) {
            return true;
        }
        if (matrix.get(x, y)) {
            double scaleX = (double) ctx.config.size() / ctx.matrixWidth;
            double scaleY = (double) ctx.config.size() / ctx.matrixHeight;
            double cx = x * scaleX;
            double cy = y * scaleY;
            if (isInPositionPattern(x, y, ctx.matrixWidth, ctx.matrixHeight, ctx.config.margin())) {
                return true;
            }
            return cx + scaleX > ctx.whiteBoxX
                    && cx < ctx.whiteBoxX + ctx.whiteBoxSize
                    && cy + scaleY > ctx.whiteBoxY
                    && cy < ctx.whiteBoxY + ctx.whiteBoxSize;
        }
        return true;
    }

    /**
     * Draws a single QR code module at the specified coordinates with configured size and shape.
     *
     * <p>Supports square or rounded modules depending on configuration.
     *
     * @param g the graphics context used for rendering
     * @param x the module's x-coordinate in the matrix
     * @param y the module's y-coordinate in the matrix
     * @param moduleSizeX width of the module in pixels
     * @param moduleSizeY height of the module in pixels
     * @param config the QR code configuration
     */
    private static void drawModule(
            Graphics2D g, int x, int y, double moduleSizeX, double moduleSizeY, QrConfig config) {
        if (Checker.checkStaticNPE(g, "drawModule", "g")
                || Checker.checkStaticNPE(config, "drawModule", CONFIG)) {
            return;
        }
        double cx = x * moduleSizeX;
        double cy = y * moduleSizeY;
        if (config.roundedModules()) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.fill(new Ellipse2D.Double(cx, cy, moduleSizeX, moduleSizeY));
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.fillRect(
                    (int) cx, (int) cy, (int) Math.ceil(moduleSizeX), (int) Math.ceil(moduleSizeY));
        }
    }

    /**
     * Renders the three QR code finder patterns at the corners using the specified style.
     *
     * @param g The graphics context for rendering.
     * @param matrixWidth Width of the QR code matrix.
     * @param config Configuration containing size, colors, module shape, and margin.
     */
    static void drawFinderPatterns(Graphics2D g, int matrixWidth, QrConfig config) {
        if (Checker.checkStaticNPE(g, "drawFinderPatterns", "g")
                || Checker.checkStaticNPE(config, "drawFinderPatterns", CONFIG)) {
            return;
        }
        double moduleSizeX = (double) config.size() / matrixWidth;
        double marginPixels = config.margin() * moduleSizeX;
        double diameter = 7 * moduleSizeX;
        if (config.roundedModules()) {
            drawRoundedFinderPatternAtPixel(
                    g, marginPixels, marginPixels, diameter, config.qrColor(), config.bgColor());
            drawRoundedFinderPatternAtPixel(
                    g,
                    marginPixels,
                    config.size() - marginPixels - diameter,
                    diameter,
                    config.qrColor(),
                    config.bgColor());
            drawRoundedFinderPatternAtPixel(
                    g,
                    config.size() - marginPixels - diameter,
                    marginPixels,
                    diameter,
                    config.qrColor(),
                    config.bgColor());
        } else {
            drawSquareFinderPatternAtPixel(
                    g, marginPixels, marginPixels, diameter, config.qrColor(), config.bgColor());
            drawSquareFinderPatternAtPixel(
                    g,
                    marginPixels,
                    config.size() - marginPixels - diameter,
                    diameter,
                    config.qrColor(),
                    config.bgColor());
            drawSquareFinderPatternAtPixel(
                    g,
                    config.size() - marginPixels - diameter,
                    marginPixels,
                    diameter,
                    config.qrColor(),
                    config.bgColor());
        }
    }

    /**
     * Draws the logo at the center of the QR code if a valid logo file is provided.
     *
     * <p>The logo is scaled to fit within 90% of the designated white box area, which is determined
     * by the QR code size and configured image ratio.
     *
     * @param g The Graphics2D context used for rendering the QR code.
     * @param config QR code configuration containing size, logo file, and image ratio.
     * @throws IOException If reading the logo file fails or the file is not a valid image.
     */
    static void drawLogoIfPresent(Graphics2D g, QrConfig config) throws IOException {
        if (Checker.checkStaticNPE(g, "drawLogoIfPresent", "g")
                || Checker.checkStaticNPE(config, "drawLogoIfPresent", CONFIG)
                || config.logoFile() == null
                || !config.logoFile().exists()
                || config.imageRatio() == 0) {
            return;
        }
        final int size = config.size();
        final int whiteBoxSize = (int) (size * config.imageRatio());
        final int whiteBoxX = (size - whiteBoxSize) / 2;
        final int whiteBoxY = (size - whiteBoxSize) / 2;
        final int logoMaxSize = (int) (whiteBoxSize * DEFAULT_GAP_BETWEEN_LOGO_AND_MODULES);
        final int logoX = whiteBoxX + (whiteBoxSize - logoMaxSize) / 2;
        final int logoY = whiteBoxY + (whiteBoxSize - logoMaxSize) / 2;
        BufferedImage logo = null;
        BufferedImage scaledLogo = null;
        Graphics2D gLogo = null;
        try (InputStream in = new FileInputStream(config.logoFile())) {
            logo = ImageIO.read(in);
            scaledLogo = new BufferedImage(logoMaxSize, logoMaxSize, BufferedImage.TYPE_INT_ARGB);
            gLogo = scaledLogo.createGraphics();
            gLogo.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gLogo.drawImage(logo, 0, 0, logoMaxSize, logoMaxSize, null);
        } finally {
            if (gLogo != null) {
                gLogo.dispose();
            }
            if (logo != null) {
                logo.flush();
            }
            if (scaledLogo != null) {
                g.drawImage(scaledLogo, logoX, logoY, null);
                scaledLogo.flush();
            }
        }
    }

    /**
     * Determines whether the given coordinates fall within any of the three QR code position
     * patterns.
     *
     * @param x X-coordinate in the QR matrix.
     * @param y Y-coordinate in the QR matrix.
     * @param matrixWidth Width of the QR matrix.
     * @param matrixHeight Height of the QR matrix.
     * @param margin QR code margin in modules.
     * @return {@code true} if the coordinate is inside a position pattern; {@code false} otherwise.
     */
    private static boolean isInPositionPattern(
            int x, int y, int matrixWidth, int matrixHeight, int margin) {
        return isInTopLeftPattern(x, y, margin)
                || isInTopRightPattern(x, y, matrixWidth, margin)
                || isInBottomLeftPattern(x, y, matrixHeight, margin);
    }

    /**
     * Checks if coordinates are within the top-left position pattern.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param margin QR code margin
     * @return {@code true} if inside top-left pattern; {@code false} otherwise
     */
    private static boolean isInTopLeftPattern(int x, int y, int margin) {
        return x >= margin && x < margin + 7 && y >= margin && y < margin + 7;
    }

    /**
     * Checks if coordinates are within the top-right position pattern.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param matrixWidth Width of the QR matrix
     * @param margin QR code margin
     * @return {@code true} if inside top-right pattern; {@code false} otherwise
     */
    private static boolean isInTopRightPattern(int x, int y, int matrixWidth, int margin) {
        return x >= matrixWidth - margin - 7
                && x < matrixWidth - margin
                && y >= margin
                && y < margin + 7;
    }

    /**
     * Checks if coordinates are within the bottom-left position pattern.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param matrixHeight Height of the QR matrix
     * @param margin QR code margin
     * @return {@code true} if inside bottom-left pattern; {@code false} otherwise
     */
    private static boolean isInBottomLeftPattern(int x, int y, int matrixHeight, int margin) {
        return x >= margin
                && x < margin + 7
                && y >= matrixHeight - margin - 7
                && y < matrixHeight - margin;
    }

    /**
     * Draws a QR code finder pattern with rounded corners at the specified pixel coordinates.
     *
     * @param g Graphics2D context to draw on.
     * @param x X-coordinate of the top-left corner.
     * @param y Y-coordinate of the top-left corner.
     * @param diameter Diameter of the finder pattern.
     * @param qrColor Color of the QR modules.
     * @param bgColor Background color inside the pattern.
     */
    private static void drawRoundedFinderPatternAtPixel(
            Graphics2D g, double x, double y, double diameter, Color qrColor, Color bgColor) {
        if (Checker.checkStaticNPE(g, DRAW_ROUNDED_FINDER_PATTERN_AT_PIXEL, "g")
                || Checker.checkStaticNPE(qrColor, DRAW_ROUNDED_FINDER_PATTERN_AT_PIXEL, "qrColor")
                || Checker.checkStaticNPE(
                        bgColor, DRAW_ROUNDED_FINDER_PATTERN_AT_PIXEL, BG_COLOR)) {
            return;
        }
        double arc = diameter / 4.0;
        g.setColor(qrColor);
        g.fill(new RoundRectangle2D.Double(x, y, diameter, diameter, arc, arc));
        double innerMargin = diameter / 7.0;
        g.setColor(bgColor);
        g.fill(
                new RoundRectangle2D.Double(
                        x + innerMargin,
                        y + innerMargin,
                        diameter - 2 * innerMargin,
                        diameter - 2 * innerMargin,
                        arc,
                        arc));
        double centerMargin = diameter / 7.0 * 2;
        g.setColor(qrColor);
        g.fill(
                new RoundRectangle2D.Double(
                        x + centerMargin,
                        y + centerMargin,
                        diameter - 2 * centerMargin,
                        diameter - 2 * centerMargin,
                        arc,
                        arc));
    }

    /**
     * Draws a standard square QR code finder pattern at the specified pixel coordinates.
     *
     * @param g Graphics2D context to draw on.
     * @param x X-coordinate of the top-left corner.
     * @param y Y-coordinate of the top-left corner.
     * @param diameter Diameter of the finder pattern.
     * @param qrColor Color of the QR modules.
     * @param bgColor Background color inside the pattern.
     */
    static void drawSquareFinderPatternAtPixel(
            Graphics2D g, double x, double y, double diameter, Color qrColor, Color bgColor) {
        if (Checker.checkStaticNPE(g, DRAW_SQUARE_FINDER_PATTERN_AT_PIXEL, "g")
                || Checker.checkStaticNPE(qrColor, DRAW_SQUARE_FINDER_PATTERN_AT_PIXEL, "qrColor")
                || Checker.checkStaticNPE(bgColor, DRAW_SQUARE_FINDER_PATTERN_AT_PIXEL, BG_COLOR)) {
            return;
        }
        g.setColor(qrColor);
        g.fillRect((int) x, (int) y, (int) diameter, (int) diameter);
        double innerMargin = diameter / 7.0;
        g.setColor(bgColor);
        g.fillRect(
                (int) (x + innerMargin),
                (int) (y + innerMargin),
                (int) (diameter - 2 * innerMargin),
                (int) (diameter - 2 * innerMargin));
        double centerMargin = diameter / 7.0 * 2;
        g.setColor(qrColor);
        g.fillRect(
                (int) (x + centerMargin),
                (int) (y + centerMargin),
                (int) (diameter - 2 * centerMargin),
                (int) (diameter - 2 * centerMargin));
    }

    /**
     * Converts a {@link Color} object to its hexadecimal RGB string representation.
     *
     * <p>If the input color is null, returns a default color "#FFFFFF".
     *
     * @param c the Color to convert
     * @return Hex string in the format "#RRGGBB", e.g. "#FF00AA"
     */
    private String colorToHex(Color c) {
        if (Checker.checkStaticNPE(c, "colorToHex", "c")) {
            return "#FFFFFF";
        }
        return "#" + Integer.toHexString(c.getRGB()).substring(2).toUpperCase();
    }

    /**
     * Loads application metadata from `version.properties`. Sets static fields: app version, name,
     * and organization. Shows an error dialog if the file cannot be read.
     */
    private static void getManifestKeys() {
        Properties props = new Properties();
        try (InputStream in =
                CanScan.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            Popup.INSTANCE.showDialog(
                    "Le fichier version.properties est illisible\n", e.getMessage(), ERREUR);
        }
        version = props.getProperty("app.version");
        name = props.getProperty("app.name");
        organization = props.getProperty("app.organization");
    }

    /**
     * Application entry point. Sets up the FlatCobalt2 theme with the Luciole font and launches the
     * CanScan GUI on the EDT.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        FlatCobalt2IJTheme.setup();
        UseLucioleFont.INSTANCE.initialize();
        SwingUtilities.invokeLater(() -> new CanScan().setVisible(true));
    }
}
