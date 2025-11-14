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

    private Mode currentMode = Mode.MECARD;
    // UI Components
    private final JRadioButton mecardRadio =
            new JRadioButton("<html><b>" + Mode.MECARD.text() + "</b></html>");
    private final JRadioButton freeRadio =
            new JRadioButton("<html><b>" + Mode.FREE.text() + "</b></html>");
    private final JButton update = new JButton("\uD83D\uDD04");
    private final JTextField nameField = new JTextField(TEXT_FIELDS_COLUMNS);
    private final JTextField phoneField = new JTextField(TEXT_FIELDS_COLUMNS);
    private final JTextField emailField = new JTextField(TEXT_FIELDS_COLUMNS);
    private final JTextField orgField = new JTextField(TEXT_FIELDS_COLUMNS);
    private final JTextField adrField = new JTextField(TEXT_FIELDS_COLUMNS);
    private final JTextField urlField = new JTextField(TEXT_FIELDS_COLUMNS);
    private final JTextArea freeField = new JTextArea("");
    private final JScrollPane freeScrollPane = new JScrollPane(freeField);
    private final JTextField logoField = new JTextField(TEXT_FIELDS_COLUMNS);
    private final JTextField sizeField = new JTextField(SIZE_FIELD_DEFAULT, TEXT_FIELDS_COLUMNS);
    private final JSlider marginSlider = new JSlider(0, MINIMUM_QR_CODE_SIZE, margin);
    private final JSlider ratioSlider =
            new JSlider(0, MAX_PERCENTAGE, (int) (imageRatio * MAX_PERCENTAGE));
    private final JCheckBox roundedModulesCheckBox = new JCheckBox();
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
    private final transient Loader loader = new Loader(qrCodeLabel);
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
     * <p>Constructs the main window and orchestrates its setup by delegating to dedicated
     * initialization methods. Configures layout, panels, input fields, QR code preview, and window
     * behavior to provide a complete user interface.
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
     * Constructs the application window title using metadata from the version properties.
     *
     * <p>Combines the application name, version, and organization into a formatted string for
     * display in the window title.
     *
     * @return a formatted title string, e.g., "📱 CanScan v1.0.0.0 • Soft64.fr"
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
        marginSlider.setMajorTickSpacing(1);
        marginSlider.setPaintTicks(true);
        marginSlider.setPaintLabels(true);
        ratioSlider.setMajorTickSpacing(MAJOR_TICK_SPACING);
        ratioSlider.setMinorTickSpacing(1);
        ratioSlider.setPaintTicks(true);
        ratioSlider.setPaintLabels(true);
        ratioSlider.addChangeListener(e -> setRatioSliderTooltipValue());
        setRatioSliderTooltipValue();
    }

    /**
     * Builds the north panel containing mode selection, card panels, common fields, and the
     * generate button.
     *
     * <p>Uses a GridBagLayout with consistent spacing and borders.
     */
    private JPanel initializeNorthPanel() {
        JPanel northPanel = new JPanel(new GridBagLayout());
        northPanel.setMaximumSize(new Dimension(DEFAULT_LABEL_WIDTH * 3, northPanel.getHeight()));
        northPanel.setBorder(
                new EmptyBorder(
                        IntConstants.DEFAULT_GAP.getValue(),
                        IntConstants.DEFAULT_GAP.getValue(),
                        IntConstants.DEFAULT_GAP.getValue(),
                        IntConstants.DEFAULT_GAP.getValue()));
        GridBagConstraints grid = northPanelGridBagConstraints();
        GridBagLayout layout = (GridBagLayout) northPanel.getLayout();
        layout.columnWidths = new int[] {DEFAULT_LABEL_WIDTH, 0};
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
        GridBagConstraints grid = new GridBagConstraints();
        grid.insets = new Insets(3, 3, 3, 3);
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.gridy = -1;
        grid.weightx = 1;
        return grid;
    }

    /**
     * Adds mode selection controls (MECARD/Free) to the north panel.
     *
     * <p>Configures radio buttons, update button, and mode switching listeners.
     */
    private void addNorthPanelModeSelection(JPanel northPanel, GridBagConstraints grid) {
        JPanel modePanel = new JPanel(new BorderLayout());
        mecardRadio.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(mecardRadio);
        group.add(freeRadio);
        JPanel radioButtonsPanel = radioButtonsPanel();
        modePanel.add(radioButtonsPanel, BorderLayout.WEST);
        modePanel.add(update, BorderLayout.EAST);
        configureUpdateButton();
        mecardRadio.addActionListener(e -> switchMode(Mode.MECARD));
        freeRadio.addActionListener(e -> switchMode(Mode.FREE));
        addRow(
                northPanel,
                grid,
                "<html><b>Mode</b></html>",
                "<html>Le format du code QR à générer :<br>"
                        + "Un contact MeCard ou la saisie libre.</html>",
                modePanel);
    }

    /**
     * Creates the radio button panel for mode selection.
     *
     * <p>Aligns MECARD and Free options side by side.
     */
    private JPanel radioButtonsPanel() {
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
        return radioButtonsPanel;
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
        addRow(
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
        addRow(
                northPanel,
                grid,
                "Taille du logo ⚠",
                "<html>Le pourcentage du logo dans le code QR.<br>⚠ Peut gêner la"
                        + " détection.</html>",
                ratioSlider);
        addRow(
                northPanel,
                grid,
                "Marge ⚠",
                "<html>La marge extérieure entre 0 et 10.<br>⚠ Peut gêner la détection.</html>",
                marginSlider);
        JPanel colorPanel = colorPanel();
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
                "<html>Le côté du code QR en pixels.<br>⚠ Peut dégrader les performances de"
                        + " l'application.</html>",
                sizeField);
        addRow(
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
        return colorPanel;
    }

    /**
     * Adds the generate button to the north panel.
     *
     * <p>Configures size, action listener, and initial disabled state.
     */
    private void addNorthPanelGenerateButton(JPanel northPanel, GridBagConstraints grid) {
        grid.gridy += 1;
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
    }

    /**
     * Creates the main panel containing all GUI sections.
     *
     * <p>Centers the QR code label, registers resize listeners for dynamic updates, and adds the
     * north panel, preview area, and spacer in a BorderLayout.
     */
    private JPanel initializeMainPanel() {
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
        southSpacer.setPreferredSize(new Dimension(0, IntConstants.DEFAULT_GAP.getValue()));
        automaticQRCodeRenderingForFieldsAndControls();
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(northPanelWrapper, BorderLayout.NORTH);
        mainPanel.add(qrCodeLabel, BorderLayout.CENTER);
        mainPanel.add(southSpacer, BorderLayout.SOUTH);
        return mainPanel;
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
     * <p>Packs components, adjusts size for QR code display, and centers the window on screen.
     */
    private void initializeWindow() {
        pack();
        setSize(getWidth(), getHeight() + QR_CODE_LABEL_DEFAULT_SIZE);
        setLocationRelativeTo(null);
    }

    /**
     * Assigns component names for identification.
     *
     * <p>Used in testing and configuration generation to reference GUI elements reliably.
     */
    private void initializeComponentNames() {
        nameField.setName("nameField");
        browseButton.setName("browseButton");
        ratioSlider.setName("ratioSlider");
        qrColorButton.setName("qrColorButton");
        freeRadio.setName("freeRadio");
        freeField.setName("freeField");
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
    private void mecard(JPanel mecardPanel, GridBagConstraints grid) {
        if (Checker.INSTANCE.checkNPE(mecardPanel, "mecard", "mecardPanel")
                || Checker.INSTANCE.checkNPE(grid, "mecard", "grid")) {
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
                "Mettre une virgule seule pour une organisation.",
                nameField);
        addRow(mecardPanel, grid, "Organisation", "Le nom de l'entreprise.", orgField);
        addRow(
                mecardPanel,
                grid,
                "Téléphone",
                "<html>Préférer le format international <b>+33…</b></html>.",
                phoneField);
        addRow(mecardPanel, grid, "Courriel", "", emailField);
        addRow(mecardPanel, grid, "Adresse", "L'adresse postale.", adrField);
        addRow(
                mecardPanel,
                grid,
                "Lien",
                "<html>L'URL complète du site web.<br>Par exemple :"
                        + " <b>https://soft64.fr</b></html>",
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
    private void freeCard(JPanel freePanel, GridBagConstraints grid) {
        if (Checker.INSTANCE.checkNPE(freePanel, "freeCard", "freePanel")
                || Checker.INSTANCE.checkNPE(grid, "freeCard", "grid")) {
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
        FontMetrics fm = freeField.getFontMetrics(freeField.getFont());
        int charHeight = fm.getHeight();
        int charWidth = fm.charWidth('W');
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
                "Les données brutes à encoder.",
                freeScrollPane);
    }

    /**
     * Sets up automatic QR code preview updates for all input fields and controls.
     *
     * <p>Any change in text fields, sliders, or the "rounded modules" checkbox triggers an
     * immediate update of the QR code preview without saving to a file. Also attaches a listener to
     * validate input and enable or disable the generate button accordingly.
     */
    private void automaticQRCodeRenderingForFieldsAndControls() {
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
