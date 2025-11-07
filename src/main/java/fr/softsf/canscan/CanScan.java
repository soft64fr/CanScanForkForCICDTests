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
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Properties;
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
import com.google.zxing.WriterException;

import fr.softsf.canscan.model.Mode;
import fr.softsf.canscan.model.QrConfig;
import fr.softsf.canscan.model.QrDataResult;
import fr.softsf.canscan.model.QrInput;
import fr.softsf.canscan.service.BuildQRDataService;
import fr.softsf.canscan.service.VersionService;
import fr.softsf.canscan.ui.Loader;
import fr.softsf.canscan.ui.Popup;
import fr.softsf.canscan.ui.QrCodeBufferedImage;
import fr.softsf.canscan.ui.QrCodeIconUtil;
import fr.softsf.canscan.ui.QrCodePreview;
import fr.softsf.canscan.ui.QrCodeResize;
import fr.softsf.canscan.util.BrowserHelper;
import fr.softsf.canscan.util.Checker;
import fr.softsf.canscan.util.IntConstants;
import fr.softsf.canscan.util.StringConstants;
import fr.softsf.canscan.util.UseLucioleFont;

/**
 * CanScan — a Swing-based application for generating QR codes.
 *
 * <p>Supports two modes:
 *
 * <ul>
 *   <li><b>MECARD</b> — generate a QR code from structured contact data (name, phone, email, etc.).
 *   <li><b>Free</b> — generate a QR code from arbitrary text or URLs.
 * </ul>
 *
 * <p>Includes live preview, optional logo embedding, adjustable size, colors, margins, and module
 * style (rounded or square). Uses ZXing for QR generation and FlatLaf for modern UI styling.
 */
public class CanScan extends JFrame {

    private static final double DEFAULT_IMAGE_RATIO = 0.27;
    private static final int DEFAULT_QR_CODE_SIZE = 400;
    private static final int MAX_PERCENTAGE = 100;
    private static final int MAJOR_TICK_SPACING = 25;
    private static final int QR_CODE_LABEL_DEFAULT_SIZE = 50;
    private static final int MINIMUM_QR_CODE_SIZE = 10;
    private static final int TEXT_FIELDS_COLUMNS = 25;
    private static final int MULTILINE_TEXT_FIELDS_ROWS = 10;
    private static final int RADIO_BUTTON_GAP = 20;
    private static final int DEFAULT_LABEL_WIDTH = 140;
    private static final String ADD_ROW = "addRow";
    private static final int GENERATE_BUTTON_EXTRA_HEIGHT = 35;
    private static final int VERTICAL_SCROLL_UNIT_INCREMENT = 16;
    private static final int PREVIEW_DEBOUNCE_DELAY_MS = 200;
    private static final int BUTTON_ICON_COLOR_SIZE = 14;
    private static final int BUTTON_COLOR_ICON_TEXT_GAP = 10;
    private static final String SIZE_FIELD_DEFAULT = "400";
    private static final String LATEST_RELEASES_REPO_URL =
            "https://github.com/Lob2018/CanScan/releases/latest";
    private static final int COLOR_BUTTONS_GAP = 10;
    private static final int MARGIN_MAXIMUM_VALUE = 10;
    private static final double GBC_COLOR_BUTTONS_WEIGHT_X = 0.5;
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

    /**
     * Initializes the CanScan GUI.
     *
     * <p>Sets up the input fields, mode selection (MECARD/Free), QR code parameters, preview area,
     * and action buttons. Configures layouts, event listeners, sliders, color choosers, and default
     * window behavior. Also initializes automatic QR code rendering on input changes.
     */
    public CanScan() {
        super(buildTitle());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(
                new BorderLayout(
                        IntConstants.DEFAULT_GAP.getValue(), IntConstants.DEFAULT_GAP.getValue()));
        setResizable(true);
        // Init
        Loader.INSTANCE.init(qrCodeLabel);
        QrCodeResize.INSTANCE.init(qrCodeLabel);
        QrCodePreview.INSTANCE.init(qrCodeLabel);
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
        northPanel.setBorder(
                new EmptyBorder(
                        IntConstants.DEFAULT_GAP.getValue(),
                        IntConstants.DEFAULT_GAP.getValue(),
                        IntConstants.DEFAULT_GAP.getValue(),
                        IntConstants.DEFAULT_GAP.getValue()));
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
        gbc.weightx = GBC_COLOR_BUTTONS_WEIGHT_X;
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
        addWindowStateListener(
                e ->
                        SwingUtilities.invokeLater(
                                () -> QrCodeResize.INSTANCE.updateQrCodeSize(getQrInput())));
        addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        SwingUtilities.invokeLater(
                                () -> QrCodeResize.INSTANCE.updateQrCodeSize(getQrInput()));
                    }
                });
        // SOUTH (margin)
        southSpacer.setPreferredSize(new Dimension(0, IntConstants.DEFAULT_GAP.getValue()));
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
     * Releases all runtime resources used by the QR code workflow.
     *
     * <p>Stops active timers, cancels background workers, and frees any allocated QR code images to
     * prevent memory leaks and ensure a clean shutdown of the preview and generation system.
     */
    private void cleanupResources() {
        stopAllTimers();
        cancelAllWorkers();
        freeQrOriginalAndQrCodeLabel();
    }

    /**
     * Frees the QR code image and its label icon to release memory.
     *
     * <p>This method must be called on the Event Dispatch Thread (EDT) to safely dispose of Swing
     * components and graphics resources.
     */
    private void freeQrOriginalAndQrCodeLabel() {
        QrCodeBufferedImage.INSTANCE.freeQrOriginal();
        QrCodeIconUtil.INSTANCE.disposeIcon(qrCodeLabel);
    }

    /**
     * Stops all active Swing timers used for QR code preview and resizing.
     *
     * <p>This ensures that scheduled tasks are canceled and associated resources are released,
     * preventing memory leaks and lingering timers.
     */
    private void stopAllTimers() {
        Loader.INSTANCE.disposeWaitIconTimer();
        QrCodeResize.INSTANCE.stop();
        QrCodePreview.INSTANCE.stop();
    }

    /**
     * Cancels all active background workers responsible for QR code preview and resizing.
     *
     * <p>This interrupts any ongoing tasks, triggers resource cleanup, and ensures no lingering
     * threads or memory leaks remain from unfinished background operations.
     */
    private void cancelAllWorkers() {
        QrCodeResize.INSTANCE.cancelPreviousResizeWorker();
        QrCodePreview.INSTANCE.cancelActivePreviewWorker();
    }

    /**
     * Disposes of the CanScan window, ensuring all resources are explicitly released.
     *
     * <p>Stops timers, cancels background workers, and frees image resources before delegating to
     * {@link JFrame#dispose()} to prevent memory leaks and lingering threads.
     */
    @Override
    public void dispose() {
        cleanupResources();
        super.dispose();
    }

    /**
     * Constructs the application window title using metadata from the version properties.
     *
     * <p>Combines the application name, version, and organization into a formatted string for
     * display in the window title.
     *
     * @return a formatted title string, e.g., "📱 CanScan v1.0.0.0 • SOFT64.FR"
     */
    private static String buildTitle() {
        getManifestKeys();
        return String.format("\uD83D\uDCF1 %s v%s • %s", name, version, organization);
    }

    /**
     * Switches the application between MECARD and FREE QR code modes.
     *
     * <p>Updates the displayed input panel and refreshes the QR code preview to reflect the
     * selected mode.
     *
     * @param mode the {@link Mode} to switch to; if null, no action is performed
     */
    private void switchMode(Mode mode) {
        if (Checker.INSTANCE.checkNPE(mode, "switchMode", "mode")) {
            return;
        }
        currentMode = mode;
        cardLayout.show(cardPanel, mode.text());
        updatePreviewQRCode();
    }

    /**
     * Initializes the MECARD input panel with labeled text fields for structured contact
     * information.
     *
     * <p>The panel will include fields for name, organization, phone, email, address, and URL,
     * arranged using {@link GridBagLayout}.
     *
     * @param mecardPanel the {@link JPanel} to populate with MECARD input fields
     * @param grid the {@link GridBagConstraints} used to position components within the panel
     */
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
     * Initializes the FREE mode input panel with a multiline text area for arbitrary text or URLs.
     *
     * <p>The text area is wrapped in a scroll pane, with line wrapping and preferred size
     * configured for comfortable editing. Uses {@link GridBagLayout} for layout management.
     *
     * @param freePanel the {@link JPanel} to populate with the free text input area
     * @param grid the {@link GridBagConstraints} used to position components within the panel
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
     * Sets up automatic QR code preview updates for all input fields and controls.
     *
     * <p>Any change in text fields, sliders, or the "rounded modules" checkbox triggers an
     * immediate update of the QR code preview without saving to a file. Also attaches a listener to
     * validate input and enable or disable the generate button accordingly.
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
     * Validates the current input fields and toggles the state of the generate button.
     *
     * <p>Disables the button if the required fields are empty:
     *
     * <ul>
     *   <li>MECARD mode: {@code nameField} must be non-empty.
     *   <li>FREE mode: {@code freeField} must be non-empty.
     * </ul>
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
     * Creates a minimal {@link DocumentListener} that executes the given action on any document
     * change.
     *
     * <p>The action is triggered on insertions, removals, or style/attribute changes in the
     * document.
     *
     * @param action the {@link Runnable} to execute on document updates
     * @return a {@link DocumentListener} that invokes the action, or {@code null} if the action is
     *     null
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
     * Updates the tooltip of the ratio slider to reflect its current value as a percentage.
     *
     * <p>Provides immediate feedback to the user on the selected logo visibility ratio when
     * hovering over the slider.
     */
    private void setRatioSliderTooltipValue() {
        ratioSlider.setToolTipText(ratioSlider.getValue() + "%");
    }

    /**
     * Opens a color chooser dialog and updates the selected color.
     *
     * <p>If {@code isQrColor} is true, updates the QR code modules color; otherwise, updates the
     * background color. The corresponding button icon and text are updated, and the QR code preview
     * is refreshed.
     *
     * @param button the JButton representing the color to update
     * @param isQrColor true to update the QR code color, false to update the background color
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
     * Creates a square icon filled with the specified color and a visible border.
     *
     * <p>The previous icon of the button, if any, is disposed to release graphics resources. The
     * resulting icon can be used to visually represent the color on a JButton.
     *
     * @param button the JButton whose previous icon will be disposed and replaced
     * @param color the color to display in the icon
     * @return an ImageIcon displaying the color with a border, or {@code null} if {@code button} or
     *     {@code color} is {@code null}
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
     * Adds a labeled component row to a panel using GridBagLayout.
     *
     * <p>The row consists of a JLabel with a tooltip and the specified JComponent. Optionally, the
     * label font is set to bold for specific components like the name field or free text area.
     *
     * @param panel the container panel to which the row will be added
     * @param gbc the GridBagConstraints used for layout configuration
     * @param labelText the text to display in the label
     * @param tooltipText the tooltip text for the label
     * @param component the JComponent to add next to the label
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
     * <p>If a file is selected, its absolute path is set in {@code logoField}.
     *
     * @param e the ActionEvent that triggered the file chooser
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
     * Opens a file chooser dialog to select a logo image.
     *
     * <p>Only PNG, JPG, and JPEG files are allowed. Can be overridden for testing purposes.
     *
     * @return the selected File, or {@code null} if the user cancels the selection
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
     *
     * <p>Validates input fields, applies visual settings (colors, margin, logo, module style), and
     * creates the QR code image. Opens a file chooser for saving and handles file name conflicts.
     * Displays user feedback dialogs for success, errors, or exceptions.
     *
     * @param e the triggering {@link ActionEvent}
     */
    private void generateQrCode(ActionEvent e) {
        if (Checker.INSTANCE.checkNPE(e, StringConstants.GENERATE_QR_CODE.getValue(), "e")) {
            return;
        }
        try {
            QrDataResult qrData =
                    BuildQRDataService.INSTANCE.buildQrData(currentMode, getQrInput());
            if (Checker.INSTANCE.checkNPE(
                    qrData,
                    StringConstants.GENERATE_QR_CODE.getValue(),
                    StringConstants.QR_DATA.getValue())) {
                return;
            }
            Objects.requireNonNull(qrData, "Dans generateQrCode qrData ne doit pas être null");
            if (StringUtils.isBlank(qrData.data())) {
                Popup.INSTANCE.showDialog("", "Aucune donnée à encoder", "Information");
                return;
            }
            File logoFile = logoField.getText().isBlank() ? null : new File(logoField.getText());
            int size = validateAndGetSize();
            validateAndGetMargin();
            validateAndGetRatio();
            boolean roundedModules = roundedModulesCheckBox.isSelected();
            QrConfig config =
                    new QrConfig(
                            logoFile, size, imageRatio, qrColor, bgColor, roundedModules, margin);
            BufferedImage qr =
                    QrCodeBufferedImage.INSTANCE.generateQrCodeImage(qrData.data(), config);
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
            if (Checker.INSTANCE.checkNPE(
                            output, StringConstants.GENERATE_QR_CODE.getValue(), "output")
                    || Checker.INSTANCE.checkNPE(
                            qr, StringConstants.GENERATE_QR_CODE.getValue(), "qr")) {
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
                                    "Manque de mémoire\n",
                                    oom.getMessage(),
                                    StringConstants.ERREUR.getValue()));

        } catch (WriterException we) {
            SwingUtilities.invokeLater(
                    () ->
                            Popup.INSTANCE.showDialog(
                                    "Pas de génération du QR Code\n",
                                    we.getMessage(),
                                    StringConstants.ERREUR.getValue()));

        } catch (IOException ioe) {
            SwingUtilities.invokeLater(
                    () ->
                            Popup.INSTANCE.showDialog(
                                    "Pas de lecture/écriture de fichier\n",
                                    ioe.getMessage(),
                                    StringConstants.ERREUR.getValue()));
        }
    }

    /**
     * Schedules a debounced QR code preview update based on the current input and settings.
     *
     * <p>If a preview generation is already in progress, restarts the debounce timer to avoid
     * excessive regeneration. Otherwise, clears the current preview and launches a new background
     * worker to generate the QR code preview asynchronously.
     */
    private void updatePreviewQRCode() {
        if (QrCodePreview.INSTANCE.isRunning()) {
            QrCodePreview.INSTANCE.getPreviewDebounceTimer().restart();
            return;
        }
        freeQrOriginalAndQrCodeLabel();
        QrCodePreview.INSTANCE.updatePreviewDebounceTimer(
                new Timer(PREVIEW_DEBOUNCE_DELAY_MS, e -> resetAndStartPreviewWorker()));
        QrCodePreview.INSTANCE.getPreviewDebounceTimer().setRepeats(false);
        QrCodePreview.INSTANCE.getPreviewDebounceTimer().start();
    }

    /**
     * Clears the current QR code display and starts a background worker to generate a new preview.
     *
     * <p>Resets the preview icon, starts the loading animation, and launches the asynchronous task
     * to render the QR code based on the latest input and configuration.
     */
    private void resetAndStartPreviewWorker() {
        qrCodeLabel.setIcon(null);
        SwingUtilities.invokeLater(Loader.INSTANCE::startAndAdjustWaitIcon);
        QrCodePreview.INSTANCE.launchPreviewWorker(getQrInput());
    }

    /**
     * Calculates the available height for QR code rendering.
     *
     * @return available height in pixels after removing header and footer space
     */
    private int calculateAvailableQrHeight() {
        return getHeight()
                - northPanelWrapper.getHeight()
                - southSpacer.getHeight()
                - IntConstants.DEFAULT_GAP.getValue() * 3;
    }

    /**
     * Builds and returns a {@link QrInput} object containing all current user inputs and settings.
     *
     * <p>Aggregates data from MECARD or FREE fields, visual settings (colors, size, margin, logo,
     * rounded modules), and layout parameters to configure QR code generation.
     *
     * @return a fully populated {@link QrInput} instance for QR code generation
     */
    private QrInput getQrInput() {
        return new QrInput(
                calculateAvailableQrHeight(),
                currentMode,
                freeField.getText(),
                nameField.getText(),
                orgField.getText(),
                phoneField.getText(),
                emailField.getText(),
                adrField.getText(),
                urlField.getText(),
                logoField.getText(),
                validateAndGetSize(),
                validateAndGetMargin(),
                validateAndGetRatio(),
                qrColor,
                bgColor,
                roundedModulesCheckBox.isSelected());
    }

    /**
     * Retrieves the selected file from a {@link JFileChooser} and ensures it has a ".png"
     * extension.
     *
     * <p>If the selected file does not end with ".png", the extension is automatically appended.
     *
     * @param chooser the {@link JFileChooser} to get the file from
     * @return a {@link File} guaranteed to have a ".png" extension, or {@code null} if the chooser
     *     is null
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
     * @param file the initial {@link File} to check for conflicts
     * @return a {@link File} ready for writing, either the original, user-approved, or
     *     auto-renamed, or {@code null} if the input file is null
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
     * Validates and returns the current image-to-QR ratio from the ratio slider.
     *
     * <p>If the slider value is out of the valid range [0, 1] or cannot be parsed, the default
     * ratio {@link #DEFAULT_IMAGE_RATIO} is used.
     *
     * @return the validated image ratio as a double between 0 and 1
     */
    double validateAndGetRatio() {
        try {
            imageRatio = (double) ratioSlider.getValue() / MAX_PERCENTAGE;
            if (imageRatio < 0 || imageRatio > 1) {
                imageRatio = DEFAULT_IMAGE_RATIO;
            }
        } catch (NumberFormatException ex) {
            imageRatio = DEFAULT_IMAGE_RATIO;
        }
        return imageRatio;
    }

    /**
     * Validates and returns the current QR code margin from the margin slider.
     *
     * <p>The value is clamped to the range [0, {@link #MARGIN_MAXIMUM_VALUE}]. If parsing fails,
     * the default margin {@link #margin} is used.
     *
     * @return the validated margin value in pixels
     */
    int validateAndGetMargin() {
        try {
            margin = marginSlider.getValue();
            if (margin < 0) {
                margin = 0;
            }
            if (margin > MARGIN_MAXIMUM_VALUE) {
                margin = MARGIN_MAXIMUM_VALUE;
            }
        } catch (NumberFormatException ex) {
            margin = 3;
        }
        return margin;
    }

    /**
     * Validates and returns the QR code size from the corresponding text field.
     *
     * <p>If the input is invalid or smaller than {@link #MINIMUM_QR_CODE_SIZE}, it defaults to
     * {@link #DEFAULT_QR_CODE_SIZE}. Updates the text field if parsing fails.
     *
     * @return the validated QR code size in pixels
     */
    int validateAndGetSize() {
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
     * Converts a {@link Color} object to its hexadecimal RGB string representation.
     *
     * <p>Returns a default color of "#FFFFFF" if the input is null.
     *
     * @param c the {@link Color} to convert
     * @return a hexadecimal string in the format "#RRGGBB", e.g., "#FF00AA"
     */
    private String colorToHex(Color c) {
        if (Checker.checkStaticNPE(c, "colorToHex", "c")) {
            return "#FFFFFF";
        }
        return "#" + Integer.toHexString(c.getRGB()).substring(2).toUpperCase();
    }

    /**
     * Loads application metadata from the `version.properties` file.
     *
     * <p>Sets the static fields for the application version, name, and organization. Displays an
     * error dialog if the properties file cannot be read.
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
                    "Le fichier version.properties est illisible\n",
                    e.getMessage(),
                    StringConstants.ERREUR.getValue());
        }
        version = props.getProperty("app.version");
        name = props.getProperty("app.name");
        organization = props.getProperty("app.organization");
    }

    /**
     * Application entry point.
     *
     * <p>Sets up the FlatCobalt2 theme, initializes the Luciole font, and launches the CanScan GUI
     * on the Event Dispatch Thread (EDT).
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        FlatCobalt2IJTheme.setup();
        UseLucioleFont.INSTANCE.initialize();
        SwingUtilities.invokeLater(() -> new CanScan().setVisible(true));
    }
}
