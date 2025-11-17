/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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

import com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme;

import fr.softsf.canscan.model.MecardJFields;
import fr.softsf.canscan.model.Mode;
import fr.softsf.canscan.model.QrConfig;
import fr.softsf.canscan.model.QrDataResult;
import fr.softsf.canscan.model.QrInput;
import fr.softsf.canscan.service.DataBuilderService;
import fr.softsf.canscan.service.GenerateAndSaveService;
import fr.softsf.canscan.service.VersionService;
import fr.softsf.canscan.ui.DynamicQrCodePreview;
import fr.softsf.canscan.ui.DynamicQrCodeResize;
import fr.softsf.canscan.ui.MyPopup;
import fr.softsf.canscan.ui.QrCodeBufferedImage;
import fr.softsf.canscan.ui.QrCodeColor;
import fr.softsf.canscan.ui.UiComponentsConfiguration;
import fr.softsf.canscan.util.BrowserHelper;
import fr.softsf.canscan.util.Checker;
import fr.softsf.canscan.util.DoubleConstants;
import fr.softsf.canscan.util.FloatConstants;
import fr.softsf.canscan.util.IntConstants;
import fr.softsf.canscan.util.StringConstants;
import fr.softsf.canscan.util.UseLucioleFont;
import fr.softsf.canscan.util.ValidationFieldHelper;

/**
 * CanScan — Swing application for QR code generation.
 *
 * <p>Provides two modes:
 *
 * <ul>
 *   <li><b>MECARD</b> — structured contact data.
 *   <li><b>Free</b> — arbitrary text or URLs.
 * </ul>
 */
public class CanScan extends JFrame {

    private static final int VERTICAL_SCROLL_UNIT_INCREMENT = 16;
    private static final String LATEST_RELEASES_REPO_URL =
            "https://github.com/Lob2018/CanScan/releases/latest";
    private static final int MINIMUM_QR_CODE_SIZE = 10;
    private static final int QR_CODE_LABEL_DEFAULT_SIZE = 50;
    private Color qrColor = Color.BLACK;
    private Color bgColor = Color.WHITE;
    private int margin = 3;
    private double imageRatio = DoubleConstants.DEFAULT_IMAGE_RATIO.getValue();
    private static String version;
    private static String name;
    private static String organization;
    private Mode currentMode = Mode.MECARD;
    // UI Components
    private final JRadioButton mecardRadio =
            new JRadioButton("<html><b>" + Mode.MECARD.text() + "</b></html>");
    private final JRadioButton freeRadio =
            new JRadioButton("<html><b>" + Mode.FREE.text() + "</b></html>");
    private final JButton update = new JButton("\uD83D\uDD04");
    private final JTextField nameField =
            new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField phoneField =
            new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField emailField =
            new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField orgField = new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField adrField = new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField urlField = new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextArea freeField = new JTextArea("");
    private final JScrollPane freeScrollPane = new JScrollPane(freeField);
    private final JTextField logoField =
            new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField sizeField =
            new JTextField(
                    StringConstants.DEFAULT_QR_CODE_DIMENSION_FIELD.getValue(),
                    IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JSlider marginSlider = new JSlider(0, MINIMUM_QR_CODE_SIZE, margin);
    private final JSlider ratioSlider =
            new JSlider(
                    0,
                    IntConstants.MAX_PERCENTAGE.getValue(),
                    (int) (imageRatio * IntConstants.MAX_PERCENTAGE.getValue()));
    private final JCheckBox roundedModulesCheckBox = new JCheckBox();
    private final JProgressBar loader = new JProgressBar();
    // Containers
    private final JPanel northPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final JLabel qrCodeLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel southSpacer = new JPanel();
    private final JButton browseButton = new JButton("\uD83D\uDCC1 Parcourir");
    private final JButton qrColorButton = new JButton("#000000");
    private final JButton bgColorButton = new JButton("#FFFFFF");
    private final JButton generateButton = new JButton("\uD83D\uDCBE Enregistrer");
    // Services
    private final transient QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
    private final transient DynamicQrCodeResize qrCodeResize =
            new DynamicQrCodeResize(qrCodeBufferedImage, qrCodeLabel, loader);
    private final transient DynamicQrCodePreview qrCodePreview =
            new DynamicQrCodePreview(qrCodeBufferedImage, qrCodeResize, qrCodeLabel, loader);
    private final transient QrCodeColor qrCodeColor = new QrCodeColor();
    private final transient GenerateAndSaveService generateAndSaveService =
            new GenerateAndSaveService(qrCodeBufferedImage);

    /**
     * Initializes the CanScan GUI.
     *
     * <p>Builds the main window and delegates setup to dedicated initialization methods,
     * configuring layout, panels, input fields, QR preview, and window behavior.
     */
    public CanScan() {
        super(initializeTitle());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(
                new BorderLayout(
                        IntConstants.DEFAULT_GAP.getValue(), IntConstants.DEFAULT_GAP.getValue()));
        setResizable(true);
        initializeSliders();
        JPanel northPanel = initializeNorthPanel();
        northPanelWrapper.add(northPanel);
        JPanel mainPanel = initializeMainPanel();
        JScrollPane scrollPane = initializeScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);
        initializeWindow();
        initializeComponentNames();
    }

    /** Sets the name field text for testing. */
    void setNameFieldTextForTests() {
        nameField.setText("John");
    }

    /** Sets the phone field text for testing. */
    void setPhoneFieldTextForTests() {
        phoneField.setText("0123456789");
    }

    /** Sets the email field text for testing. */
    void setEmailFieldTextForTests() {
        emailField.setText("john@example.com");
    }

    /** Sets the organization field text for testing. */
    void setOrgFieldTextForTests() {
        orgField.setText("Org");
    }

    /** Sets the address field text for testing. */
    void setAdrFieldTextForTests() {
        adrField.setText("Addr");
    }

    /** Sets the URL field text for testing. */
    void setUrlFieldTextForTests() {
        urlField.setText("https://example.com");
    }

    /** Returns the logo field text for testing. */
    String getLogoFieldTextForTests() {
        return logoField.getText();
    }

    /** Sets the logo field text for testing. */
    void setLogoFieldTextForTests() {
        logoField.setText("");
    }

    /** Sets the size field text for testing. */
    void setSizeFieldTextForTests(String text) {
        sizeField.setText(text);
    }

    /** Returns the margin field int for testing. */
    int getMarginFieldIntForTests() {
        return marginSlider.getValue();
    }

    /** Sets the margin slider value for testing. */
    void setMarginSliderValueForTests(int value) {
        marginSlider.setValue(value);
    }

    /** Sets the ratio slider value for testing. */
    void setRatioSliderValueForTests(int value) {
        ratioSlider.setValue(value);
    }

    /** Sets the rounded modules checkbox state for testing. */
    void setRoundedModulesSelectedForTests() {
        roundedModulesCheckBox.setSelected(true);
    }

    /**
     * Loads application metadata from `version.properties`.
     *
     * <p>Initializes static fields for version, name, and organization.
     */
    private static void getManifestKeys() {
        Properties props = new Properties();
        try (InputStream in =
                CanScan.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            MyPopup.INSTANCE.showDialog(
                    "Le fichier version.properties est illisible\n",
                    e.getMessage(),
                    StringConstants.ERREUR.getValue());
        }
        version = props.getProperty("app.version");
        name = props.getProperty("app.name");
        organization = props.getProperty("app.organization");
    }

    /**
     * Builds the application window title from version metadata.
     *
     * <p>Combines the application name, version, and organization into a formatted string.
     *
     * @return the formatted title, e.g., "📱 CanScan v1.0.0.0 • Soft64.fr"
     */
    private static String initializeTitle() {
        getManifestKeys();
        return String.format("\uD83D\uDCF1 %s v%s • %s", name, version, organization);
    }

    /**
     * Configures sliders for logo margin and ratio.
     *
     * <p>Sets tick spacing, labels, and listeners to update tooltips dynamically.
     */
    protected void initializeSliders() {
        UiComponentsConfiguration.INSTANCE.configureMarginSlider(marginSlider, margin);
        UiComponentsConfiguration.INSTANCE.configureRatioSlider(ratioSlider, imageRatio);
    }

    /**
     * Builds the north panel containing mode selection, card panels, common fields, and the
     * generate button.
     *
     * <p>Uses a GridBagLayout with consistent spacing and borders.
     */
    private JPanel initializeNorthPanel() {
        JPanel northPanel = new JPanel(new GridBagLayout());
        northPanel.setMaximumSize(
                new Dimension(
                        IntConstants.DEFAULT_LABEL_WIDTH.getValue() * 3, northPanel.getHeight()));
        northPanel.setBorder(
                new EmptyBorder(
                        IntConstants.DEFAULT_GAP.getValue(),
                        IntConstants.DEFAULT_GAP.getValue(),
                        IntConstants.DEFAULT_GAP.getValue(),
                        IntConstants.DEFAULT_GAP.getValue()));
        GridBagConstraints grid = northPanelGridBagConstraints();
        GridBagLayout layout = (GridBagLayout) northPanel.getLayout();
        layout.columnWidths = new int[] {IntConstants.DEFAULT_LABEL_WIDTH.getValue(), 0};
        addNorthPanelModeSelection(northPanel, grid);
        addNorthPanelCardPanels(northPanel, grid);
        addNorthPanelCommonFields(northPanel, grid);
        addNorthPanelGenerateButton(northPanel, grid);
        return northPanel;
    }

    /**
     * Creates default GridBagConstraints for consistent layout configuration.
     *
     * <p>Defines insets, fill behavior, and initial grid positions.
     */
    private GridBagConstraints northPanelGridBagConstraints() {
        return UiComponentsConfiguration.INSTANCE.createDefaultConstraints();
    }

    /**
     * Adds mode selection controls (MECARD/Free) to the north panel.
     *
     * <p>Configures radio buttons, update button, and mode switching listeners.
     */
    private void addNorthPanelModeSelection(JPanel northPanel, GridBagConstraints grid) {
        ButtonGroup group = new ButtonGroup();
        JPanel modePanel =
                UiComponentsConfiguration.INSTANCE.createModePanel(
                        mecardRadio, freeRadio, update, group);
        configureUpdateButton();
        mecardRadio.addActionListener(e -> switchMode(Mode.MECARD));
        freeRadio.addActionListener(e -> switchMode(Mode.FREE));
        UiComponentsConfiguration.INSTANCE.addRow(
                northPanel,
                grid,
                "<html><b>Mode</b></html>",
                "<html>Le format du code QR à générer :<br>"
                        + "Un contact MeCard ou la saisie libre.</html>",
                modePanel);
    }

    /**
     * Configures the update button for version checking.
     *
     * <p>Sets tooltip, browser action, and background worker execution.
     */
    private void configureUpdateButton() {
        update.setEnabled(false);
        update.setToolTipText(
                "<html>Recherche de mise à jour<br>" + LATEST_RELEASES_REPO_URL + "</html>");
        update.addActionListener(
                e -> BrowserHelper.INSTANCE.openInBrowser(LATEST_RELEASES_REPO_URL));
        SwingWorker<Boolean, Void> worker =
                VersionService.INSTANCE.checkLatestVersion(version, update);
        worker.execute();
    }

    /**
     * Adds MECARD and Free card panels to the north panel.
     *
     * <p>Initializes both panels and registers them in the card layout.
     */
    private void addNorthPanelCardPanels(JPanel northPanel, GridBagConstraints grid) {
        JPanel freePanel = new JPanel(new GridBagLayout());
        freeCard(freePanel, new GridBagConstraints());
        JPanel mecardPanel = new JPanel(new GridBagLayout());
        mecard(mecardPanel, new GridBagConstraints());
        grid.gridy += 1;
        grid.gridx = 0;
        grid.weightx = 1.0;
        grid.gridwidth = GridBagConstraints.HORIZONTAL;
        cardPanel.add(mecardPanel, Mode.MECARD.text());
        cardPanel.add(freePanel, Mode.FREE.text());
        northPanel.add(cardPanel, grid);
        grid.gridwidth = GridBagConstraints.BOTH;
    }

    /**
     * Adds common input fields to the north panel.
     *
     * <p>Includes logo path, margin, ratio, colors, size, and rounded modules options.
     */
    private void addNorthPanelCommonFields(JPanel northPanel, GridBagConstraints grid) {
        UiComponentsConfiguration.INSTANCE.addRow(
                northPanel,
                grid,
                "Logo",
                "Le chemin du fichier logo (PNG, JPG, ou JPEG).",
                logoField);
        browseButton.addActionListener(this::browseLogo);
        grid.gridx = 2;
        grid.weightx = 0;
        northPanel.add(browseButton, grid);
        grid.gridy += 1;
        UiComponentsConfiguration.INSTANCE.addRow(
                northPanel,
                grid,
                "Taille du logo ⚠",
                "<html>Le pourcentage du logo dans le code QR.<br>⚠ Peut gêner la"
                        + " détection.</html>",
                ratioSlider);
        UiComponentsConfiguration.INSTANCE.addRow(
                northPanel,
                grid,
                "Marge ⚠",
                "<html>La marge extérieure entre 0 et 10.<br>⚠ Peut gêner la détection.</html>",
                marginSlider);
        JPanel colorPanel = colorPanel();
        UiComponentsConfiguration.INSTANCE.addRow(
                northPanel,
                grid,
                "Couleurs ⚠",
                "Le code QR ne fonctionnera que si le contraste est suffisant.",
                colorPanel);
        UiComponentsConfiguration.INSTANCE.addRow(
                northPanel,
                grid,
                "Dimension ⚠",
                "<html>Le côté du code QR en pixels.<br>⚠ Une trop grande taille, peut dégrader les"
                        + " performances de l'application.</html>",
                sizeField);
        UiComponentsConfiguration.INSTANCE.addRow(
                northPanel,
                grid,
                "Modules ronds ⚠",
                "<html>Arrondir les modules.<br>⚠ Peut gêner la détection.</html>",
                roundedModulesCheckBox);
    }

    /**
     * Creates the color selection panel for QR code and background colors.
     *
     * <p>Initializes buttons, listeners, and updates preview on color changes.
     */
    private JPanel colorPanel() {
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
        return UiComponentsConfiguration.INSTANCE.createColorPanel(qrColorButton, bgColorButton);
    }

    /**
     * Adds the generate button to the north panel.
     *
     * <p>Configures size, action listener, and initial disabled state.
     */
    private void addNorthPanelGenerateButton(JPanel northPanel, GridBagConstraints grid) {
        grid.gridy += 1;
        UiComponentsConfiguration.INSTANCE.configureGenerateButton(
                generateButton, this::generateQrCode);
        northPanel.add(generateButton, grid);
    }

    /**
     * Builds the main panel with header, QR preview overlay, and spacer.
     *
     * <p>Registers resize listeners and enables automatic QR preview updates.
     *
     * <p><strong>Layout:</strong> NORTH: header • CENTER: preview overlay • SOUTH: spacer
     *
     * @return the main {@link JPanel}
     */
    private JPanel initializeMainPanel() {
        qrCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        qrCodeLabel.setVerticalAlignment(SwingConstants.CENTER);
        qrCodeLabel.setOpaque(false);
        qrCodeLabel.setAlignmentX(FloatConstants.OVERLAY_PANEL_ALIGNMENT.getValue());
        qrCodeLabel.setAlignmentY(FloatConstants.OVERLAY_PANEL_ALIGNMENT.getValue());
        JPanel overlayPanelForQrCodeLabelAndLoader =
                UiComponentsConfiguration.INSTANCE.createQrCodeOverlayPanel(loader, qrCodeLabel);
        Runnable resize = () -> qrCodeResize.updateQrCodeResize(getQrInput());
        addWindowStateListener(e -> SwingUtilities.invokeLater(resize));
        addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        SwingUtilities.invokeLater(resize);
                    }
                });
        southSpacer.setPreferredSize(new Dimension(0, IntConstants.DEFAULT_GAP.getValue()));
        automaticQRCodeRenderingForFieldsAndControls();
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(overlayPanelForQrCodeLabelAndLoader);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(northPanelWrapper, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(southSpacer, BorderLayout.SOUTH);
        return mainPanel;
    }

    /**
     * Sets the loader size so it matches the QR code label height and remains centered. A small
     * offset is applied to correct a slight visual misalignment.
     */
    private void setLoaderSize() {
        int h =
                Math.max(
                        calculateAvailableQrCodeLabelHeight()
                                + IntConstants.LOADER_SIZE_OFFSET.getValue(),
                        QR_CODE_LABEL_DEFAULT_SIZE + IntConstants.LOADER_SIZE_OFFSET.getValue());
        Dimension size = new Dimension(h, h);
        loader.setPreferredSize(size);
        loader.setMaximumSize(size);
        Container parent = loader.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    /**
     * Wraps the main panel in a scrollable container.
     *
     * <p>Removes borders and configures smooth vertical scrolling.
     */
    private JScrollPane initializeScrollPane(JPanel mainPanel) {
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(VERTICAL_SCROLL_UNIT_INCREMENT);
        return scrollPane;
    }

    /**
     * Finalizes window layout and appearance.
     *
     * <p>Packs components, adjusts size for QR code display, loader, and centers the window on
     * screen.
     */
    private void initializeWindow() {
        pack();
        setSize(getWidth(), getHeight() + QR_CODE_LABEL_DEFAULT_SIZE);
        setLoaderSize();
        setLocationRelativeTo(null);
    }

    /**
     * Assigns component names for identification.
     *
     * <p>Used in testing and configuration generation to reference GUI elements reliably.
     */
    private void initializeComponentNames() {
        UiComponentsConfiguration.INSTANCE.assignComponentNames(
                nameField, browseButton, ratioSlider, qrColorButton, freeRadio, freeField);
    }

    /**
     * Closes the CanScan window and releases all QR code–related resources.
     *
     * <p>Stops background tasks and frees image data to ensure a clean shutdown without memory
     * leaks, then delegates to {@link JFrame#dispose()}.
     */
    @Override
    public void dispose() {
        qrCodeResize.disposeAllResourcesOnExit();
        qrCodePreview.disposeAllResourcesOnExit();
        super.dispose();
    }

    /**
     * Switches the application between MECARD and FREE modes.
     *
     * <p>Updates the visible input panel and refreshes the QR preview accordingly.
     *
     * @param mode the selected {@link Mode}; ignored if null
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
     * Populates the MECARD panel with structured contact fields.
     *
     * <p>Includes name, organization, phone, email, address, and URL, arranged with {@link
     * GridBagLayout}.
     *
     * @param mecardPanel the panel to populate
     * @param grid layout constraints
     */
    private void mecard(JPanel mecardPanel, GridBagConstraints grid) {
        if (Checker.INSTANCE.checkNPE(mecardPanel, "mecard", "mecardPanel")
                || Checker.INSTANCE.checkNPE(grid, "mecard", "grid")) {
            return;
        }
        UiComponentsConfiguration.INSTANCE.populateMecardPanel(
                mecardPanel,
                grid,
                new MecardJFields(nameField, orgField, phoneField, emailField, adrField, urlField));
    }

    /**
     * Populates the FREE panel with a multiline text area for arbitrary text or URLs.
     *
     * <p>Uses a scroll pane with line wrapping and {@link GridBagLayout} for layout.
     *
     * @param freePanel the panel to populate
     * @param grid layout constraints
     */
    private void freeCard(JPanel freePanel, GridBagConstraints grid) {
        if (Checker.INSTANCE.checkNPE(freePanel, "freeCard", "freePanel")
                || Checker.INSTANCE.checkNPE(grid, "freeCard", "grid")) {
            return;
        }
        UiComponentsConfiguration.INSTANCE.populateFreePanel(
                freePanel, grid, freeField, freeScrollPane);
    }

    /**
     * Attaches automatic QR code preview updates to all input fields and controls.
     *
     * <p>Any change in text fields, sliders, or the "rounded modules" checkbox refreshes the QR
     * code preview and validates input to update the generate button state.
     */
    private void automaticQRCodeRenderingForFieldsAndControls() {
        DocumentListener docListener =
                UiComponentsConfiguration.createDocumentListener(
                        () -> qrCodePreview.updateQrCodePreview(getQrInput()));
        DocumentListener validationListener =
                UiComponentsConfiguration.createDocumentListener(this::updateGenerateButtonState);
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
     * Updates the generate button state based on input validation.
     *
     * <p>Disables the button if the required fields are empty: MECARD → {@code nameField}, FREE →
     * {@code freeField}.
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
     * Opens a file chooser for PNG, JPG, or JPEG logos.
     *
     * <p>Package-private for testing; can be overridden or mocked.
     *
     * @return the selected file, or {@code null} if canceled
     */
    File chooseLogoFile() {
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
     * Generates a QR code from current input fields and visual settings, then saves it via {@link
     * GenerateAndSaveService}.
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
            generateAndSaveService.generateAndSave(qrData, config, loader);
        } catch (Exception ex) {
            MyPopup.INSTANCE.showDialog(
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
    private int calculateAvailableQrCodeLabelHeight() {
        return getHeight()
                - northPanelWrapper.getHeight()
                - southSpacer.getHeight()
                - IntConstants.DEFAULT_GAP.getValue() * 3;
    }

    /**
     * Collects current input and visual settings into a {@link QrInput} for QR code generation.
     *
     * @return a populated {@link QrInput} instance
     */
    private QrInput getQrInput() {
        return new QrInput(
                calculateAvailableQrCodeLabelHeight(),
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

    /** Validates and returns the current image-to-QR ratio from the ratio slider. */
    double validateAndGetRatio() {
        imageRatio = ValidationFieldHelper.INSTANCE.validateAndGetRatio(ratioSlider);
        return imageRatio;
    }

    /** Validates and returns the current QR code margin from the margin slider. */
    int validateAndGetMargin() {
        margin = ValidationFieldHelper.INSTANCE.validateAndGetMargin(marginSlider);
        return margin;
    }

    /** Validates and returns the QR code size from the corresponding text field. */
    int validateAndGetSize() {
        return ValidationFieldHelper.INSTANCE.validateAndGetSize(sizeField);
    }

    /**
     * Application entry point. Initializes the UI theme, font, and launches the GUI on the EDT.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        FlatCobalt2IJTheme.setup();
        UseLucioleFont.INSTANCE.initialize();
        SwingUtilities.invokeLater(() -> new CanScan().setVisible(true));
    }
}
