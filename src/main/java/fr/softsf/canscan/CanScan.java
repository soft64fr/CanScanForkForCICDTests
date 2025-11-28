/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
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
import java.util.Objects;
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
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;

import fr.softsf.canscan.constant.DoubleConstants;
import fr.softsf.canscan.constant.FloatConstants;
import fr.softsf.canscan.constant.IntConstants;
import fr.softsf.canscan.constant.StringConstants;
import fr.softsf.canscan.model.CommonFields;
import fr.softsf.canscan.model.EncodedData;
import fr.softsf.canscan.model.MecardJFields;
import fr.softsf.canscan.model.MeetJFields;
import fr.softsf.canscan.model.Mode;
import fr.softsf.canscan.model.NativeImageUiComponents;
import fr.softsf.canscan.model.WholeFields;
import fr.softsf.canscan.service.DataBuilderService;
import fr.softsf.canscan.service.GenerateAndSaveService;
import fr.softsf.canscan.service.VersionService;
import fr.softsf.canscan.ui.ColorOperation;
import fr.softsf.canscan.ui.EncodedImage;
import fr.softsf.canscan.ui.FlatLafDatePicker;
import fr.softsf.canscan.ui.FlatLafTimePicker;
import fr.softsf.canscan.ui.MyPopup;
import fr.softsf.canscan.ui.UiComponentsConfiguration;
import fr.softsf.canscan.ui.worker.DynamicPreviewWorker;
import fr.softsf.canscan.ui.worker.DynamicResizeWorker;
import fr.softsf.canscan.util.ApplicationMetadata;
import fr.softsf.canscan.util.BrowserHelper;
import fr.softsf.canscan.util.Checker;
import fr.softsf.canscan.util.CoordinateHelper;
import fr.softsf.canscan.util.DateHelper;
import fr.softsf.canscan.util.UseLucioleFont;
import fr.softsf.canscan.util.ValidationFieldHelper;

/** CanScan — Swing QR code generator with MECARD, MEET, and FREE modes. */
public class CanScan extends JFrame {

    private static final int VERTICAL_SCROLL_UNIT_INCREMENT = 16;
    private static final int MINIMUM_QR_CODE_SIZE = 10;
    private static final int QR_CODE_LABEL_DEFAULT_SIZE = 50;
    private static final String NORTH_PANEL = "northPanel";
    private static final String HTML_B_STRING_B_HTML = "<html><b>%s</b></html>";
    private static final int MAX_COORDINATE_LENGTH = 12;
    private Color qrColor = Color.BLACK;
    private Color bgColor = Color.WHITE;
    private int margin = 3;
    private double imageRatio = DoubleConstants.DEFAULT_IMAGE_RATIO.getValue();
    private Mode currentMode = Mode.MECARD;
    // UI COMPONENTS
    private final JProgressBar loader = new JProgressBar();
    // radio
    private final JRadioButton mecardRadio =
            new JRadioButton(String.format(HTML_B_STRING_B_HTML, Mode.MECARD.text()));
    private final JRadioButton meetRadio =
            new JRadioButton(String.format(HTML_B_STRING_B_HTML, Mode.MEET.text()));
    private final JRadioButton freeRadio =
            new JRadioButton(String.format(HTML_B_STRING_B_HTML, Mode.FREE.text()));
    // update
    private final JButton update = new JButton("\uD83D\uDD04");
    // MeCard
    private final JTextField nameField =
            new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField phoneField =
            new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField emailField =
            new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField orgField = new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField adrField = new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField urlField = new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    // Meet
    private final JTextField meetTitleField =
            new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField meetUIdField =
            new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final JTextField meetNameField =
            new JTextField(IntConstants.TEXT_FIELDS_COLUMNS.getValue());
    private final DatePicker meetBeginDatePicker = new FlatLafDatePicker();
    private final TimePicker meetBeginTimePicker = new FlatLafTimePicker();
    private final DatePicker meetEndDatePicker = new FlatLafDatePicker();
    private final TimePicker meetEndTimePicker = new FlatLafTimePicker();
    private final JTextField meetLatField = new JTextField();
    private final JTextField meetLongField = new JTextField();
    // Free
    private final JTextArea freeField = new JTextArea("");
    private final JScrollPane freeScrollPane = new JScrollPane(freeField);
    // common
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
    // CONTAINERS
    private final JPanel northPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final JLabel qrCodeLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel southSpacer = new JPanel();
    private final JButton browseButton = new JButton("\uD83D\uDCC1 Parcourir");
    private final JButton qrColorButton = new JButton("#000000");
    private final JButton bgColorButton = new JButton("#FFFFFF");
    private final JButton generateButton = new JButton("\uD83D\uDCBE Enregistrer et copier");
    // SERVICES
    private final transient EncodedImage encodedImage = new EncodedImage();
    private final transient DynamicResizeWorker qrCodeResize =
            new DynamicResizeWorker(encodedImage, qrCodeLabel, loader);
    private final transient DynamicPreviewWorker qrCodePreview =
            new DynamicPreviewWorker(encodedImage, qrCodeResize, qrCodeLabel, loader);
    private final transient ColorOperation colorOperation = new ColorOperation();
    private final transient GenerateAndSaveService generateAndSaveService =
            new GenerateAndSaveService(encodedImage);

    /** Constructs the CanScan GUI, setting up layout, panels, inputs, and QR preview. */
    public CanScan() {
        super(ApplicationMetadata.INSTANCE.initializeTitle());
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
        Objects.requireNonNull(scrollPane);
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
    void setLogoFieldTextForTests(String text) {
        logoField.setText(text);
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

    /** Returns the current mode for testing. */
    Mode getCurrentModeForTests() {
        return currentMode;
    }

    /** Sets the current mode for testing. */
    void setCurrentModeForTests(Mode mode) {
        this.currentMode = mode;
    }

    /** Switches the mode for testing. */
    void switchModeForTests(Mode mode) {
        switchMode(mode);
    }

    /**
     * Configures sliders for logo margin and ratio.
     *
     * <p>Sets tick spacing, labels, and listeners to update tooltips dynamically.
     */
    private void initializeSliders() {
        UiComponentsConfiguration.INSTANCE.configureMarginSlider(marginSlider, margin);
        UiComponentsConfiguration.INSTANCE.configureRatioSlider(ratioSlider, imageRatio);
    }

    /**
     * Builds the north panel with mode selection, card panels, common fields, and generate button.
     * Uses GridBagLayout with consistent spacing.
     */
    private JPanel initializeNorthPanel() {
        JPanel northPanel = getNorthJPanel();
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
     * Creates the base north panel with layout, maximum width, and padding applied.
     *
     * @return a preconfigured north {@link JPanel}
     */
    private static JPanel getNorthJPanel() {
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
     * Adds mode selection controls (MECARD/MEET/FREE) to the north panel.
     *
     * <p>Configures radio buttons, update button, and mode switching listeners.
     */
    private void addNorthPanelModeSelection(JPanel northPanel, GridBagConstraints grid) {
        if (Checker.INSTANCE.checkNPE(northPanel, "addNorthPanelModeSelection", NORTH_PANEL)
                || Checker.INSTANCE.checkNPE(grid, "addNorthPanelModeSelection", "grid")) {
            return;
        }
        ButtonGroup group = new ButtonGroup();
        JPanel modePanel =
                UiComponentsConfiguration.INSTANCE.createModePanel(
                        mecardRadio, meetRadio, freeRadio, update, group);
        configureUpdateButton();
        mecardRadio.addActionListener(e -> switchMode(Mode.MECARD));
        meetRadio.addActionListener(e -> switchMode(Mode.MEET));
        freeRadio.addActionListener(e -> switchMode(Mode.FREE));
        UiComponentsConfiguration.INSTANCE.addRow(
                northPanel,
                grid,
                "<html><b>Mode</b></html>",
                "<html>Le format du code QR à générer :<br>"
                        + "Un contact MeCard, un rendez-vous, la saisie libre.</html>",
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
                "<html>Recherche de mise à jour<br>"
                        + StringConstants.LATEST_RELEASES_REPO_URL.getValue()
                        + "</html>");
        update.addActionListener(
                e ->
                        BrowserHelper.INSTANCE.openInBrowser(
                                StringConstants.LATEST_RELEASES_REPO_URL.getValue()));
        SwingWorker<Boolean, Void> worker =
                VersionService.INSTANCE.checkLatestVersion(
                        StringConstants.VERSION.getValue(), update);
        worker.execute();
    }

    /**
     * Adds MECARD MEET FREE card panels to the north panel.
     *
     * <p>Initializes both panels and registers them in the card layout.
     */
    private void addNorthPanelCardPanels(JPanel northPanel, GridBagConstraints grid) {
        if (Checker.INSTANCE.checkNPE(northPanel, "addNorthPanelCardPanels", NORTH_PANEL)
                || Checker.INSTANCE.checkNPE(grid, "addNorthPanelCardPanels", "grid")) {
            return;
        }
        JPanel freePanel = new JPanel(new GridBagLayout());
        freeCard(freePanel, new GridBagConstraints());
        JPanel meetPanel = new JPanel(new GridBagLayout());
        meetCard(meetPanel, new GridBagConstraints());
        JPanel mecardPanel = new JPanel(new GridBagLayout());
        mecard(mecardPanel, new GridBagConstraints());
        grid.gridy += 1;
        grid.gridx = 0;
        grid.weightx = 1.0;
        grid.gridwidth = GridBagConstraints.HORIZONTAL;
        cardPanel.add(mecardPanel, Mode.MECARD.text());
        cardPanel.add(meetPanel, Mode.MEET.text());
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
        if (Checker.INSTANCE.checkNPE(northPanel, "addNorthPanelCommonFields", NORTH_PANEL)
                || Checker.INSTANCE.checkNPE(grid, "addNorthPanelCommonFields", "grid")) {
            return;
        }
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
        colorOperation.initializeColorButton(qrColorButton, Color.BLACK, true);
        colorOperation.initializeColorButton(bgColorButton, Color.WHITE, false);
        qrColorButton.addActionListener(
                e -> {
                    Color newColor = colorOperation.chooseColor(qrColorButton, qrColor, true);
                    if (newColor != null) {
                        qrColor = newColor;
                        qrCodePreview.updateQrCodePreview(getQrInput());
                    }
                });
        bgColorButton.addActionListener(
                e -> {
                    Color newColor = colorOperation.chooseColor(bgColorButton, bgColor, false);
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
     * <p>Configures size, action listener, tooltip, and initial disabled state.
     */
    private void addNorthPanelGenerateButton(JPanel northPanel, GridBagConstraints grid) {
        if (Checker.INSTANCE.checkNPE(northPanel, "addNorthPanelGenerateButton", NORTH_PANEL)
                || Checker.INSTANCE.checkNPE(grid, "addNorthPanelGenerateButton", "grid")) {
            return;
        }
        generateButton.setToolTipText(
                "<html>Enregistre le code QR, et copie les données dans le presse‑papiers.</html>");
        grid.gridy += 1;
        UiComponentsConfiguration.INSTANCE.configureGenerateButton(
                generateButton, this::generateQrCode);
        northPanel.add(generateButton, grid);
    }

    /**
     * Builds the main panel with header, QR preview overlay, and bottom spacer. Registers resize
     * listeners and enables automatic QR updates.
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
        if (Checker.INSTANCE.checkNPE(mainPanel, "initializeScrollPane", "mainPanel")) {
            return null;
        }
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(VERTICAL_SCROLL_UNIT_INCREMENT);
        return scrollPane;
    }

    /**
     * Finalizes window layout: packs components, adjusts size for QR display and loader, and
     * centers the window on screen.
     */
    private void initializeWindow() {
        pack();
        setSize(getWidth(), getHeight() + QR_CODE_LABEL_DEFAULT_SIZE);
        setLoaderSize();
        setLocationRelativeTo(null);
    }

    /** Assigns stable component names for testing and native-image configuration. */
    private void initializeComponentNames() {
        UiComponentsConfiguration.INSTANCE.assignComponentNames(
                new NativeImageUiComponents(
                        nameField,
                        browseButton,
                        ratioSlider,
                        qrColorButton,
                        freeRadio,
                        meetRadio,
                        freeField,
                        meetBeginTimePicker));
    }

    /** Releases QR-code resources and closes the window. */
    @Override
    public void dispose() {
        qrCodeResize.disposeAllResourcesOnExit();
        qrCodePreview.disposeAllResourcesOnExit();
        super.dispose();
    }

    /**
     * Switches between MECARD, MEET, and FREE modes, updating the panel and QR preview.
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
     * Populates the MECARD panel with contact fields.
     *
     * @param mecardPanel target panel
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
     * Populates the MEET panel with meet fields.
     *
     * @param meetPanel target panel
     * @param grid layout constraints
     */
    private void meetCard(JPanel meetPanel, GridBagConstraints grid) {
        if (Checker.INSTANCE.checkNPE(meetPanel, "meet", "meetPanel")
                || Checker.INSTANCE.checkNPE(grid, "meet", "grid")) {
            return;
        }
        meetUIdField.setEditable(false);
        UiComponentsConfiguration.INSTANCE.populateMeetPanel(
                meetPanel,
                grid,
                new MeetJFields(
                        meetTitleField,
                        meetUIdField,
                        meetNameField,
                        meetBeginDatePicker,
                        meetBeginTimePicker,
                        meetEndDatePicker,
                        meetEndTimePicker,
                        meetLatField,
                        meetLongField));
    }

    /**
     * Populates the FREE panel with a multiline text area.
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

    /** Attaches automatic QR preview updates and input validation to all fields and controls. */
    private void automaticQRCodeRenderingForFieldsAndControls() {
        DocumentListener docListener =
                UiComponentsConfiguration.createDocumentListener(
                        () -> qrCodePreview.updateQrCodePreview(getQrInput()));
        JTextField[] textFields = {
            nameField,
            phoneField,
            emailField,
            orgField,
            adrField,
            urlField,
            meetTitleField,
            meetNameField,
            logoField,
            sizeField
        };
        for (JTextField field : textFields) {
            field.getDocument().addDocumentListener(docListener);
        }
        UiComponentsConfiguration.attachLimitedDocumentListener(
                meetLatField,
                MAX_COORDINATE_LENGTH,
                () -> qrCodePreview.updateQrCodePreview(getQrInput()));
        UiComponentsConfiguration.attachLimitedDocumentListener(
                meetLongField,
                MAX_COORDINATE_LENGTH,
                () -> qrCodePreview.updateQrCodePreview(getQrInput()));
        freeField.getDocument().addDocumentListener(docListener);
        marginSlider.addChangeListener(e -> qrCodePreview.updateQrCodePreview(getQrInput()));
        ratioSlider.addChangeListener(e -> qrCodePreview.updateQrCodePreview(getQrInput()));
        roundedModulesCheckBox.addActionListener(
                e -> qrCodePreview.updateQrCodePreview(getQrInput()));
        meetBeginDatePicker
                .getComponentDateTextField()
                .getDocument()
                .addDocumentListener(docListener);
        meetBeginTimePicker
                .getComponentTimeTextField()
                .getDocument()
                .addDocumentListener(docListener);
        meetEndDatePicker
                .getComponentDateTextField()
                .getDocument()
                .addDocumentListener(docListener);
        meetEndTimePicker
                .getComponentTimeTextField()
                .getDocument()
                .addDocumentListener(docListener);
        DocumentListener generateButtonValidationListener =
                UiComponentsConfiguration.createDocumentListener(this::updateGenerateButtonState);
        nameField.getDocument().addDocumentListener(generateButtonValidationListener);
        freeField.getDocument().addDocumentListener(generateButtonValidationListener);
        meetTitleField.getDocument().addDocumentListener(generateButtonValidationListener);
        meetUIdField.getDocument().addDocumentListener(generateButtonValidationListener);
        meetBeginDatePicker
                .getComponentDateTextField()
                .getDocument()
                .addDocumentListener(generateButtonValidationListener);
        meetBeginTimePicker
                .getComponentTimeTextField()
                .getDocument()
                .addDocumentListener(generateButtonValidationListener);
        meetEndDatePicker
                .getComponentDateTextField()
                .getDocument()
                .addDocumentListener(generateButtonValidationListener);
        meetEndTimePicker
                .getComponentTimeTextField()
                .getDocument()
                .addDocumentListener(generateButtonValidationListener);
    }

    /** Enables or disables the "Generate" button based on required fields for the current mode. */
    private void updateGenerateButtonState() {
        generateButton.setEnabled(!shouldDisableGenerateButton());
    }

    /**
     * Determines whether the "Generate" button should be disabled for the current mode.
     *
     * @return {@code true} if the button should be disabled, {@code false} otherwise
     */
    private boolean shouldDisableGenerateButton() {
        return switch (currentMode) {
            case MECARD -> nameField.getText().trim().isEmpty();
            case FREE -> freeField.getText().trim().isEmpty();
            case MEET ->
                    meetTitleField.getText().trim().isEmpty()
                            || meetUIdField.getText().trim().isEmpty()
                            || meetBeginDatePicker
                                    .getComponentDateTextField()
                                    .getText()
                                    .trim()
                                    .isEmpty()
                            || meetBeginTimePicker
                                    .getComponentTimeTextField()
                                    .getText()
                                    .trim()
                                    .isEmpty()
                            || meetEndDatePicker
                                    .getComponentDateTextField()
                                    .getText()
                                    .trim()
                                    .isEmpty()
                            || meetEndTimePicker
                                    .getComponentTimeTextField()
                                    .getText()
                                    .trim()
                                    .isEmpty();
        };
    }

    /**
     * Opens a file chooser to select a logo image and updates the logo text field.
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
            EncodedData qrData = DataBuilderService.INSTANCE.buildData(currentMode, getQrInput());
            if (Checker.INSTANCE.checkNPE(
                    qrData,
                    StringConstants.GENERATE_QR_CODE.getValue(),
                    StringConstants.QR_DATA.getValue())) {
                return;
            }
            CommonFields config =
                    new CommonFields(
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
     * Collects current input and visual settings into a {@link WholeFields} for QR code generation.
     *
     * @return a populated {@link WholeFields} instance
     */
    private WholeFields getQrInput() {
        return new WholeFields(
                calculateAvailableQrCodeLabelHeight(),
                currentMode,
                freeField.getText(),
                nameField.getText(),
                orgField.getText(),
                phoneField.getText(),
                emailField.getText(),
                adrField.getText(),
                urlField.getText(),
                meetTitleField.getText(),
                validateAndGetMeetUID(),
                meetNameField.getText(),
                DateHelper.INSTANCE.validateAndGetDateAndTime(
                        meetBeginDatePicker.getDate(), meetBeginTimePicker.getTime()),
                DateHelper.INSTANCE.validateAndGetDateAndTime(
                        meetEndDatePicker.getDate(), meetEndTimePicker.getTime()),
                CoordinateHelper.INSTANCE.getValidatedCoordinate(meetLatField, true),
                CoordinateHelper.INSTANCE.getValidatedCoordinate(meetLongField, false),
                logoField.getText(),
                validateAndGetSize(),
                validateAndGetMargin(),
                validateAndGetRatio(),
                qrColor,
                bgColor,
                roundedModulesCheckBox.isSelected());
    }

    /** Validates and returns the current meet UID from the meet title field. */
    String validateAndGetMeetUID() {
        meetUIdField.setText(
                ValidationFieldHelper.INSTANCE.validateAndGetMeetUID(meetTitleField.getText()));
        return meetUIdField.getText();
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
