/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;

import com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme;

import fr.softsf.canscan.model.Mode;
import fr.softsf.canscan.model.QrConfig;
import fr.softsf.canscan.model.QrDataResult;
import fr.softsf.canscan.model.QrInput;
import fr.softsf.canscan.service.DataBuilderService;
import fr.softsf.canscan.service.GenerateAndSaveService;
import fr.softsf.canscan.service.VersionService;
import fr.softsf.canscan.ui.DynamicQrCodePreview;
import fr.softsf.canscan.ui.DynamicQrCodeResize;
import fr.softsf.canscan.ui.Loader;
import fr.softsf.canscan.ui.Popup;
import fr.softsf.canscan.ui.QrCodeBufferedImage;
import fr.softsf.canscan.ui.QrCodeColor;
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
    private static final int DEFAULT_LABEL_WIDTH = 110;
    private static final String ADD_ROW = "addRow";
    private static final int GENERATE_BUTTON_EXTRA_HEIGHT = 35;
    private static final int VERTICAL_SCROLL_UNIT_INCREMENT = 16;
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
    private final JPanel northPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    private Mode currentMode = Mode.MECARD;
    JRadioButton mecardRadio = new JRadioButton(Mode.MECARD.text());
    JRadioButton freeRadio = new JRadioButton(Mode.FREE.text());
    // Update
    JButton update = new JButton("\uD83D\uDD04");
    // Champs MECARD
    JTextField nameField = new JTextField(TEXT_FIELDS_COLUMNS);
    final JTextField phoneField = new JTextField(TEXT_FIELDS_COLUMNS);
    final JTextField emailField = new JTextField(TEXT_FIELDS_COLUMNS);
    final JTextField orgField = new JTextField(TEXT_FIELDS_COLUMNS);
    final JTextField adrField = new JTextField(TEXT_FIELDS_COLUMNS);
    final JTextField urlField = new JTextField(TEXT_FIELDS_COLUMNS);
    // Champs FREE
    final JTextArea freeField = new JTextArea("");
    final FontMetrics fm = freeField.getFontMetrics(freeField.getFont());
    final JScrollPane freeScrollPane = new JScrollPane(freeField);
    final int charHeight = fm.getHeight();
    final int charWidth = fm.charWidth('W');
    // CardLayout pour basculer entre MECARD et FREE
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    // Champs commmuns
    final JTextField logoField = new JTextField(TEXT_FIELDS_COLUMNS);
    final JTextField sizeField = new JTextField(SIZE_FIELD_DEFAULT, TEXT_FIELDS_COLUMNS);
    final JSlider marginSlider = new JSlider(0, MINIMUM_QR_CODE_SIZE, margin);
    final JSlider ratioSlider = new JSlider(0, MAX_PERCENTAGE, (int) (imageRatio * MAX_PERCENTAGE));
    final JCheckBox roundedModulesCheckBox = new JCheckBox();
    // Rendu dynamique
    private final JLabel qrCodeLabel = new JLabel("", SwingConstants.CENTER);
    private final transient Loader loader = new Loader(qrCodeLabel);
    private final transient QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
    private final transient DynamicQrCodeResize qrCodeResize =
            new DynamicQrCodeResize(qrCodeBufferedImage, qrCodeLabel, loader);
    private final transient DynamicQrCodePreview qrCodePreview =
            new DynamicQrCodePreview(qrCodeBufferedImage, qrCodeResize, qrCodeLabel, loader);
    private final transient QrCodeColor qrCodeColor = new QrCodeColor();
    private final transient GenerateAndSaveService generateAndSaveService =
            new GenerateAndSaveService(qrCodeBufferedImage);
    // SOUTH
    private final JPanel southSpacer = new JPanel();

    // Buttons
    @SuppressWarnings("FieldCanBeLocal")
    private final JButton browseButton = new JButton("\uD83D\uDCC1 Parcourir");

    private final JButton qrColorButton = new JButton("#000000");
    private final JButton bgColorButton = new JButton("#FFFFFF");

    @SuppressWarnings("FieldCanBeLocal")
    private final JButton generateButton = new JButton("\uD83D\uDCBE Enregistrer");

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
        addRow(
                northPanel,
                grid,
                "<html><b>Mode</b></html>",
                "Format du code QR à générer.",
                modePanel);
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
        addRow(northPanel, grid, "Logo", "Chemin du fichier logo (PNG, JPG, ou JPEG).", logoField);
        browseButton.addActionListener(this::browseLogo);
        grid.gridx = 2;
        grid.weightx = 0;
        northPanel.add(browseButton, grid);
        grid.gridy += 1;
        addRow(
                northPanel,
                grid,
                "Taille du logo ⚠",
                "<html>Pourcentage du logo dans le code QR."
                        + "<br>⚠ Peut gêner la détection.</html>",
                ratioSlider);
        addRow(
                northPanel,
                grid,
                "Marge ⚠",
                "<html>La marge extérieure entre 0 et 10."
                        + "<br>⚠ Peut gêner la détection.</html>",
                marginSlider);
        JPanel colorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        qrCodeColor.initializeColorButton(qrColorButton, Color.BLACK, true);
        qrCodeColor.initializeColorButton(bgColorButton, Color.WHITE, false);
        qrColorButton.addActionListener(
                e -> {
                    Color newColor = qrCodeColor.chooseColor(qrColorButton, qrColor, true);
                    if (newColor != null) {
                        qrColor = newColor;
                        qrCodePreview.updateQrCodePreview(getQrInput());
                    }
                });
        bgColorButton.addActionListener(
                e -> {
                    Color newColor = qrCodeColor.chooseColor(bgColorButton, bgColor, false);
                    if (newColor != null) {
                        bgColor = newColor;
                        qrCodePreview.updateQrCodePreview(getQrInput());
                    }
                });
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
                "Le code QR ne fonctionnera que si le contraste est suffisant.",
                colorPanel);
        addRow(
                northPanel,
                grid,
                "Dimension ⚠",
                "<html>Côté du code QR en pixels."
                        + "<br>⚠ Peut dégrader les performances de l'application.</html>",
                sizeField);
        addRow(
                northPanel,
                grid,
                "Modules ronds ⚠",
                "<html>Arrondir les modules.<br>⚠ Peut gêner la détection.</html>",
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
        generateButton.addActionListener(this::generateQrCode);
        generateButton.setEnabled(false);
        northPanel.add(generateButton, grid);
        // CENTER (code QR dynamique)
        qrCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        qrCodeLabel.setVerticalAlignment(SwingConstants.CENTER);
        addWindowStateListener(
                e ->
                        SwingUtilities.invokeLater(
                                () -> qrCodeResize.updateQrCodeResize(getQrInput())));
        addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        SwingUtilities.invokeLater(
                                () -> qrCodeResize.updateQrCodeResize(getQrInput()));
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
     * Disposes of the CanScan window, ensuring all resources are explicitly released.
     *
     * <p>Stops timers, cancels background workers, and frees image resources before delegating to
     * {@link JFrame#dispose()} to prevent memory leaks and lingering threads.
     */
    @Override
    public void dispose() {
        disposeAllResourcesOnExit();
        super.dispose();
    }

    /**
     * Releases all resources used by the QR code system.
     *
     * <p>Stops timers, cancels workers, and frees image data to ensure a clean shutdown and prevent
     * memory leaks when closing the application.
     */
    private void disposeAllResourcesOnExit() {
        qrCodeResize.disposeAllResourcesOnExit();
        qrCodePreview.disposeAllResourcesOnExit();
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
        qrCodePreview.updateQrCodePreview(getQrInput());
        updateGenerateButtonState();
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
        addRow(
                mecardPanel,
                grid,
                "<html><b>Nom, prénom</b></html>",
                "Mettre une virgule pour une organisation.",
                nameField);
        addRow(mecardPanel, grid, "Organisation", "Le nom de l'entreprise.", orgField);
        addRow(
                mecardPanel,
                grid,
                "Téléphone",
                "Préférer le format international (+33…).",
                phoneField);
        addRow(mecardPanel, grid, "Courriel", "", emailField);
        addRow(mecardPanel, grid, "Adresse", "L'adresse postale.", adrField);
        addRow(mecardPanel, grid, "Lien", "Une URL (site web, profil, etc.).", urlField);
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
                "<html><b>Texte</b></html>",
                "Données brutes à encoder.",
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
        DocumentListener docListener =
                simpleDocumentListener(() -> qrCodePreview.updateQrCodePreview(getQrInput()));
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
        marginSlider.addChangeListener(e -> qrCodePreview.updateQrCodePreview(getQrInput()));
        ratioSlider.addChangeListener(e -> qrCodePreview.updateQrCodePreview(getQrInput()));
        roundedModulesCheckBox.addActionListener(
                e -> qrCodePreview.updateQrCodePreview(getQrInput()));
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
     * Adds a labeled component row to a panel using GridBagLayout.
     *
     * <p>The row consists of a JLabel with optional tooltip text and the specified JComponent. The
     * label is created with the provided text, and if a non-blank tooltip is given, it is set on
     * the label. Both the label and the component are then added to the panel using the provided
     * GridBagConstraints.
     *
     * @param panel the container panel to which the row will be added
     * @param gbc the GridBagConstraints used for layout configuration
     * @param labelText the text to display in the label (must not be null)
     * @param tooltipText the tooltip text for the label (optional, may be null or blank)
     * @param component the JComponent to add next to the label (must not be null)
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
                || Checker.INSTANCE.checkNPE(labelText, ADD_ROW, "labelText")) {
            return;
        }
        gbc.gridx = 0;
        gbc.gridy += 1;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        if (StringUtils.isNotBlank(tooltipText)) {
            label.setToolTipText(tooltipText);
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
        chooser.setDialogTitle("Sélectionner le logo (PNG, JPG, ou JPEG)");
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "Images (PNG, JPG, ou JPEG)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Builds the QR code data and configuration from the current input fields, then delegates the
     * generation and saving of the QR code to {@link GenerateAndSaveService}.
     *
     * <p>Collects input from MECARD or FREE fields, applies visual settings (colors, size, margin,
     * logo, rounded modules), and passes them to the QR service for QR code generation and file
     * saving.
     *
     * @param e the triggering {@link ActionEvent}
     */
    private void generateQrCode(ActionEvent e) {
        if (Checker.INSTANCE.checkNPE(e, StringConstants.GENERATE_QR_CODE.getValue(), "e")) {
            return;
        }
        try {
            QrDataResult qrData = DataBuilderService.INSTANCE.buildData(currentMode, getQrInput());
            if (Checker.INSTANCE.checkNPE(
                    qrData,
                    StringConstants.GENERATE_QR_CODE.getValue(),
                    StringConstants.QR_DATA.getValue())) {
                return;
            }
            QrConfig config =
                    new QrConfig(
                            logoField.getText().isBlank() ? null : new File(logoField.getText()),
                            validateAndGetSize(),
                            validateAndGetRatio(),
                            qrColor,
                            bgColor,
                            roundedModulesCheckBox.isSelected(),
                            validateAndGetMargin());
            generateAndSaveService.generateAndSave(qrData, config);
        } catch (Exception ex) {
            Popup.INSTANCE.showDialog(
                    "Erreur inattendue lors de la génération du QR Code",
                    ex.getMessage(),
                    StringConstants.ERREUR.getValue());
        }
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
