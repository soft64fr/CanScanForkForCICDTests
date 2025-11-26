/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.apache.commons.lang3.StringUtils;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;

import fr.softsf.canscan.constant.FloatConstants;
import fr.softsf.canscan.constant.IntConstants;
import fr.softsf.canscan.model.MecardJFields;
import fr.softsf.canscan.model.MeetJFields;
import fr.softsf.canscan.model.NativeImageUiComponents;
import fr.softsf.canscan.util.Checker;
import fr.softsf.canscan.util.FontManager;

/** Creating and configuring UI components. */
public enum UiComponentsConfiguration {
    INSTANCE;

    private static final int MULTILINE_TEXT_FIELDS_ROWS = 10;
    private static final int RADIO_BUTTON_GAP = 10;
    private static final int COLOR_BUTTONS_GAP = 10;
    private static final int MAJOR_TICK_SPACING = 25;
    private static final double GBC_HALF_WEIGHT_X = 0.5;
    private static final int GENERATE_BUTTON_EXTRA_HEIGHT = 35;
    private static final String CREATE_MODE_PANEL = "createModePanel";
    private static final String ADD_ROW = "addRow";
    private static final int COORDINATES_FIELDS_GAP = 5;
    private static final String POPULATE_MECARD_PANEL = "populateMecardPanel";

    /**
     * Configures a JSlider with standard margin settings.
     *
     * <p>Sets the slider to the given initial value, enables major tick spacing of 1, and ensures
     * that tick marks and labels are painted.
     *
     * @param slider the {@link JSlider} to configure; must not be null
     * @param initialValue the initial value to set for the slider
     */
    public void configureMarginSlider(JSlider slider, int initialValue) {
        if (Checker.INSTANCE.checkNPE(slider, "configureMarginSlider", "slider")) {
            return;
        }
        slider.setValue(initialValue);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
    }

    /**
     * Configures a JSlider for ratio selection with standard settings and tooltip updates.
     *
     * <p>Sets the slider's value based on {@code initialRatio} (scaled to percentage), sets the
     * maximum value, major and minor tick spacing, enables tick marks and labels, and updates the
     * tooltip to show the current percentage dynamically.
     *
     * @param slider the {@link JSlider} to configure; must not be null
     * @param initialRatio the initial ratio (0.0 to 1.0) to set for the slider
     */
    public void configureRatioSlider(JSlider slider, double initialRatio) {
        if (Checker.INSTANCE.checkNPE(slider, "configureRatioSlider", "slider")) {
            return;
        }
        slider.setValue((int) (initialRatio * IntConstants.MAX_PERCENTAGE.getValue()));
        slider.setMaximum(IntConstants.MAX_PERCENTAGE.getValue());
        slider.setMajorTickSpacing(MAJOR_TICK_SPACING);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> slider.setToolTipText(slider.getValue() + "%"));
        slider.setToolTipText(slider.getValue() + "%");
    }

    /**
     * Creates a mode selection panel containing radio buttons for different modes and an update
     * button.
     *
     * <p>The panel arranges the radio buttons on the left and the update button on the right. The
     * provided {@link ButtonGroup} is used to group the radio buttons, and the "mecard" radio
     * button is selected by default.
     *
     * @param mecardRadio the {@link JRadioButton} for "mecard" mode; must not be null
     * @param meetRadio the {@link JRadioButton} for "meet" mode; must not be null
     * @param freeRadio the {@link JRadioButton} for "free" mode; must not be null
     * @param updateButton the {@link JButton} used to trigger updates; must not be null
     * @param buttonGroup the {@link ButtonGroup} to group the radio buttons; must not be null
     * @return a {@link JPanel} containing the configured mode selection UI, or {@code null} if any
     *     required component is {@code null}
     */
    public JPanel createModePanel(
            JRadioButton mecardRadio,
            JRadioButton meetRadio,
            JRadioButton freeRadio,
            JButton updateButton,
            ButtonGroup buttonGroup) {
        if (Checker.INSTANCE.checkNPE(mecardRadio, CREATE_MODE_PANEL, "mecardRadio")
                || Checker.INSTANCE.checkNPE(freeRadio, CREATE_MODE_PANEL, "freeRadio")
                || Checker.INSTANCE.checkNPE(meetRadio, CREATE_MODE_PANEL, "meetRadio")
                || Checker.INSTANCE.checkNPE(updateButton, CREATE_MODE_PANEL, "updateButton")
                || Checker.INSTANCE.checkNPE(buttonGroup, CREATE_MODE_PANEL, "buttonGroup")) {
            return null;
        }
        JPanel modePanel = new JPanel(new BorderLayout());
        mecardRadio.setSelected(true);
        buttonGroup.add(mecardRadio);
        buttonGroup.add(meetRadio);
        buttonGroup.add(freeRadio);
        JPanel radioButtonsPanel = createRadioButtonsPanel(mecardRadio, meetRadio, freeRadio);
        modePanel.add(radioButtonsPanel, BorderLayout.WEST);
        modePanel.add(updateButton, BorderLayout.EAST);
        return modePanel;
    }

    /**
     * Creates a panel containing the given radio buttons arranged horizontally with spacing.
     *
     * <p>Uses {@link GridBagLayout} to align the radio buttons and applies horizontal gaps between
     * them according to {@code RADIO_BUTTON_GAP}.
     *
     * @param mecardRadio the {@link JRadioButton} for "mecard" mode; must not be null
     * @param meetRadio the {@link JRadioButton} for "meet" mode; must not be null
     * @param freeRadio the {@link JRadioButton} for "free" mode; must not be null
     * @return a {@link JPanel} containing the aligned radio buttons
     */
    private JPanel createRadioButtonsPanel(
            JRadioButton mecardRadio, JRadioButton meetRadio, JRadioButton freeRadio) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, RADIO_BUTTON_GAP);
        panel.add(mecardRadio, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, RADIO_BUTTON_GAP);
        panel.add(meetRadio, gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(freeRadio, gbc);
        return panel;
    }

    /**
     * Creates a panel containing buttons for selecting background and QR code colors.
     *
     * <p>The panel arranges the background color button on the left and the QR color button on the
     * right, using {@link GridBagLayout} with horizontal spacing defined by {@code
     * COLOR_BUTTONS_GAP}.
     *
     * @param qrColorButton the {@link JButton} for selecting the QR code color; must not be null
     * @param bgColorButton the {@link JButton} for selecting the background color; must not be null
     * @return a {@link JPanel} containing the color selection buttons, or {@code null} if any
     *     button is null
     */
    public JPanel createColorPanel(JButton qrColorButton, JButton bgColorButton) {
        if (Checker.INSTANCE.checkNPE(qrColorButton, "createColorPanel", "qrColorButton")
                || Checker.INSTANCE.checkNPE(bgColorButton, "createColorPanel", "bgColorButton")) {
            return null;
        }
        JPanel colorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = GBC_HALF_WEIGHT_X;
        gbc.insets = new Insets(0, 0, 0, COLOR_BUTTONS_GAP);
        colorPanel.add(bgColorButton, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        colorPanel.add(qrColorButton, gbc);
        return colorPanel;
    }

    /**
     * Configures a JTextArea for free text input with word wrapping and a fixed size.
     *
     * <p>Enables word wrap and line wrap for the text area, then calculates the preferred size of
     * the enclosing {@link JScrollPane} based on character dimensions and predefined column and row
     * counts.
     *
     * @param textArea the {@link JTextArea} to configure; must not be null
     * @param scrollPane the {@link JScrollPane} containing the text area; must not be null
     */
    public void configureFreeTextArea(JTextArea textArea, JScrollPane scrollPane) {
        if (Checker.INSTANCE.checkNPE(textArea, "configureFreeTextArea", "textArea")
                || Checker.INSTANCE.checkNPE(scrollPane, "configureFreeTextArea", "scrollPane")) {
            return;
        }
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        Dimension size =
                new Dimension(
                        FontManager.INSTANCE.getCharWidth()
                                * IntConstants.TEXT_FIELDS_COLUMNS.getValue(),
                        FontManager.INSTANCE.getLineHeight() * MULTILINE_TEXT_FIELDS_ROWS);
        scrollPane.setPreferredSize(size);
        scrollPane.setMinimumSize(size);
        scrollPane.setMaximumSize(size);
    }

    /**
     * Configures a generate button with a standard size, attaches an action listener, and sets its
     * initial state to disabled.
     *
     * <p>The button's height is increased by {@code GENERATE_BUTTON_EXTRA_HEIGHT}, and its minimum,
     * preferred, and maximum sizes are all set to this dimension. The provided {@link
     * ActionListener} is added to handle button actions.
     *
     * @param button the {@link JButton} to configure; must not be null
     * @param listener the {@link ActionListener} to attach; must not be null
     */
    public void configureGenerateButton(JButton button, ActionListener listener) {
        if (Checker.INSTANCE.checkNPE(button, "configureGenerateButton", "button")
                || Checker.INSTANCE.checkNPE(listener, "configureGenerateButton", "listener")) {
            return;
        }
        Dimension d =
                new Dimension(button.getWidth(), button.getHeight() + GENERATE_BUTTON_EXTRA_HEIGHT);
        button.setMinimumSize(d);
        button.setPreferredSize(d);
        button.setMaximumSize(d);
        button.addActionListener(listener);
        button.setEnabled(false);
    }

    /**
     * Creates an overlay panel containing a loader and a QR code label.
     *
     * <p>The loader is configured with a flat style, no border, transparent background, and set to
     * indeterminate mode. Both the loader and the QR code label are added to a {@link JPanel} using
     * {@link OverlayLayout}, allowing the loader to appear over the label.
     *
     * @param loader the {@link JProgressBar} used as a loader; must not be null
     * @param qrCodeLabel the {@link JLabel} displaying the QR code; must not be null
     * @return a {@link JPanel} containing the overlay, or {@code null} if any component is null
     */
    public JPanel createQrCodeOverlayPanel(JProgressBar loader, JLabel qrCodeLabel) {
        if (Checker.INSTANCE.checkNPE(loader, "createQrCodeOverlayPanel", "loader")
                || Checker.INSTANCE.checkNPE(
                        qrCodeLabel, "createQrCodeOverlayPanel", "qrCodeLabel")) {
            return null;
        }
        loader.putClientProperty("FlatLaf.style", "arc:0");
        loader.setBorder(BorderFactory.createEmptyBorder());
        loader.setIndeterminate(true);
        loader.setOpaque(false);
        loader.setAlignmentX(FloatConstants.OVERLAY_PANEL_ALIGNMENT.getValue());
        loader.setAlignmentY(FloatConstants.OVERLAY_PANEL_ALIGNMENT.getValue());
        JPanel overlayPanel = new JPanel();
        overlayPanel.setLayout(new OverlayLayout(overlayPanel));
        overlayPanel.setOpaque(false);
        overlayPanel.add(loader);
        overlayPanel.add(qrCodeLabel);
        return overlayPanel;
    }

    /**
     * Creates and populates a MECARD input panel with labeled input fields.
     *
     * <p>Configures the {@link JPanel} using {@link GridBagLayout} and adds rows for name,
     * organization, phone, email, address, and URL fields. Tooltips provide guidance for each input
     * field.
     *
     * @param mecardPanel the {@link JPanel} to populate; must not be null
     * @param grid the {@link GridBagConstraints} used for layout positioning; must not be null
     * @param mecardJFields the {@link MecardJFields} containing the input fields; must not be null
     */
    public void populateMecardPanel(
            JPanel mecardPanel, GridBagConstraints grid, MecardJFields mecardJFields) {
        if (Checker.INSTANCE.checkNPE(mecardPanel, POPULATE_MECARD_PANEL, "mecardPanel")
                || Checker.INSTANCE.checkNPE(grid, POPULATE_MECARD_PANEL, "grid")
                || Checker.INSTANCE.checkNPE(
                        mecardJFields, POPULATE_MECARD_PANEL, "mecardJFields")) {
            return;
        }
        GridBagLayout layout = (GridBagLayout) mecardPanel.getLayout();
        layout.columnWidths = new int[] {IntConstants.DEFAULT_LABEL_WIDTH.getValue(), 0};
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.weightx = 1;
        grid.gridy = -1;
        addRow(
                mecardPanel,
                grid,
                "<html><b>Nom, prénom</b></html>",
                "Mettre une virgule seule pour une organisation.",
                mecardJFields.nameField());
        addRow(
                mecardPanel,
                grid,
                "Organisation",
                "Le nom de l'entreprise.",
                mecardJFields.orgField());
        addRow(
                mecardPanel,
                grid,
                "Téléphone",
                "<html>Préférer le format international <b>+33…</b></html>.",
                mecardJFields.phoneField());
        addRow(mecardPanel, grid, "Courriel", "", mecardJFields.emailField());
        addRow(mecardPanel, grid, "Adresse", "L'adresse postale.", mecardJFields.adrField());
        addRow(
                mecardPanel,
                grid,
                "Lien",
                "<html>L'URL complète du site web.<br>Par exemple :"
                        + " <b>https://soft64.fr</b></html>",
                mecardJFields.urlField());
    }

    /**
     * Creates and populates a MEET input panel with labeled input fields for event details.
     *
     * <p>Configures the {@link JPanel} using {@link GridBagLayout} and adds rows for the event
     * title, unique identifier, start and end date/time, organizer name, and location coordinates.
     * Tooltips provide guidance for each input field.
     *
     * @param meetPanel the {@link JPanel} to populate; must not be null
     * @param grid the {@link GridBagConstraints} used for layout positioning; must not be null
     * @param meetJFields the {@link MeetJFields} containing the input fields; must not be null
     */
    public void populateMeetPanel(
            JPanel meetPanel, GridBagConstraints grid, MeetJFields meetJFields) {
        if (Checker.INSTANCE.checkNPE(meetPanel, "populateMeetPanel", "meetPanel")
                || Checker.INSTANCE.checkNPE(grid, "populateMeetPanel", "grid")
                || Checker.INSTANCE.checkNPE(meetJFields, POPULATE_MECARD_PANEL, "meetJFields")) {
            return;
        }
        GridBagLayout layout = (GridBagLayout) meetPanel.getLayout();
        layout.columnWidths = new int[] {IntConstants.DEFAULT_LABEL_WIDTH.getValue(), 0};
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.weightx = 1;
        grid.gridy = -1;
        addRow(
                meetPanel,
                grid,
                "<html><b>Titre</b></html>",
                "<html>Il doit être unique, car il sert à générer l'identifiant"
                        + " unique.</html>",
                meetJFields.meetTitleField());
        addRow(
                meetPanel,
                grid,
                "<html><b>Identifiant</b></html>",
                "<html>Identifiant permanent de l'événement.<br>Il doit rester identique pour"
                        + " toutes les versions de l'événement.</html>",
                meetJFields.meetUIdField());
        JPanel beginDateTimePanel =
                createDateTimePanel(
                        meetJFields.meetBeginDatePicker(), meetJFields.meetBeginTimePicker());
        addRow(
                meetPanel,
                grid,
                "<html><b>Début</b></html>",
                "Date et heure du début de l'événement.",
                beginDateTimePanel);
        JPanel endDateTimePanel =
                createDateTimePanel(
                        meetJFields.meetEndDatePicker(), meetJFields.meetEndTimePicker());
        addRow(
                meetPanel,
                grid,
                "<html><b>Fin</b></html>",
                "Date et heure de la fin de l'événement.",
                endDateTimePanel);
        addRow(
                meetPanel,
                grid,
                "<html>Nom</html>",
                "Nom de l'organisateur.",
                meetJFields.meetNameField());
        JPanel coordinatesPanel =
                createCoordinatesPanel(meetJFields.meetLatField(), meetJFields.meetLongField());
        addRow(
                meetPanel,
                grid,
                "Coordonnées",
                "Latitude et longitude du lieu de l'événement.",
                coordinatesPanel);
    }

    /**
     * Creates a panel with date/time fields side by side, distributed evenly with a gap.
     *
     * @param meetDatePicker the date field
     * @param meetTimePicker the time field
     * @return a JPanel containing the two fields
     */
    public JPanel createDateTimePanel(DatePicker meetDatePicker, TimePicker meetTimePicker) {
        if (Checker.INSTANCE.checkNPE(meetDatePicker, "createDateTimePanel", "meetDatePicker")
                || Checker.INSTANCE.checkNPE(
                        meetTimePicker, "createDateTimePanel", "meetTimePicker")) {
            return null;
        }
        JPanel dateTimePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = GBC_HALF_WEIGHT_X;
        gbc.insets = new Insets(0, 0, 0, COORDINATES_FIELDS_GAP);
        dateTimePanel.add(meetDatePicker, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        dateTimePanel.add(meetTimePicker, gbc);
        return dateTimePanel;
    }

    /**
     * Creates a panel with latitude and longitude text fields side by side, distributed evenly with
     * a gap.
     *
     * @param latField the latitude text field
     * @param longField the longitude text field
     * @return a JPanel containing the two fields
     */
    public JPanel createCoordinatesPanel(JTextField latField, JTextField longField) {
        if (Checker.INSTANCE.checkNPE(latField, "createCoordinatesPanel", "latField")
                || Checker.INSTANCE.checkNPE(longField, "createCoordinatesPanel", "longField")) {
            return null;
        }
        JPanel coordinatesPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = GBC_HALF_WEIGHT_X;
        gbc.insets = new Insets(0, 0, 0, COORDINATES_FIELDS_GAP);
        latField.setToolTipText("Latitude");
        coordinatesPanel.add(latField, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        longField.setToolTipText("Longitude");
        coordinatesPanel.add(longField, gbc);
        return coordinatesPanel;
    }

    /**
     * Creates and populates a FREE text input panel.
     *
     * <p>Configures the text area with wrapping and size using {@link
     * #configureFreeTextArea(JTextArea, JScrollPane)}, then adds it to the panel with a label and
     * tooltip. Uses {@link GridBagLayout} for layout.
     *
     * @param freePanel the {@link JPanel} to populate; must not be null
     * @param grid the {@link GridBagConstraints} used for layout positioning; must not be null
     * @param freeField the {@link JTextArea} for free text input; must not be null
     * @param freeScrollPane the {@link JScrollPane} containing the text area; must not be null
     */
    public void populateFreePanel(
            JPanel freePanel,
            GridBagConstraints grid,
            JTextArea freeField,
            JScrollPane freeScrollPane) {
        if (Checker.INSTANCE.checkNPE(freePanel, "populateFreePanel", "freePanel")
                || Checker.INSTANCE.checkNPE(grid, "populateFreePanel", "grid")) {
            return;
        }
        GridBagLayout layout = (GridBagLayout) freePanel.getLayout();
        layout.columnWidths = new int[] {IntConstants.DEFAULT_LABEL_WIDTH.getValue(), 0};
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.weightx = 1;
        grid.gridy = -1;
        configureFreeTextArea(freeField, freeScrollPane);
        addRow(
                freePanel,
                grid,
                "<html><b>Texte</b></html>",
                "Les données brutes à encoder.",
                freeScrollPane);
    }

    /**
     * Assigns unique names to all GUI components encapsulated in the given {@link
     * NativeImageUiComponents} instance.
     *
     * <p>This allows automated test frameworks, such as native image configuration simulators, to
     * reliably identify each component by name.
     *
     * @param nativeImageUiComponents the {@link NativeImageUiComponents} containing the GUI
     *     elements to name; must not be null
     */
    public void assignComponentNames(NativeImageUiComponents nativeImageUiComponents) {
        nativeImageUiComponents.assignNames();
    }

    /**
     * Creates a simple {@link DocumentListener} that executes the given action on any change to the
     * document.
     *
     * <p>The action is invoked asynchronously on the Swing event dispatch thread using {@link
     * SwingUtilities#invokeLater(Runnable)} to ensure thread safety.
     *
     * @param action the {@link Runnable} to execute when the document changes; must not be null
     * @return a {@link DocumentListener} that triggers the action on insert, remove, or change
     *     events, or {@code null} if the action is null
     */
    public static DocumentListener createDocumentListener(Runnable action) {
        if (Checker.INSTANCE.checkNPE(action, "createDocumentListener", "action")) {
            return null;
        }
        return new DocumentListener() {
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
     * Attaches a {@link DocumentListener} to a {@link JTextField} that:
     *
     * <ul>
     *   <li>Executes the given {@code action} on any change to the document (insert, remove, or
     *       change), asynchronously on the Swing EDT.
     *   <li>Limits the text length to {@code maxLength} characters using a {@link DocumentFilter}.
     *   <li>Removes line breaks (\n, \r) from pasted or inserted text.
     * </ul>
     *
     * <p>This allows dynamic updates (e.g., QR code preview) while preventing input overflow and
     * unwanted line breaks.
     *
     * @param field the {@link JTextField} to attach the listener to; must not be null
     * @param maxLength the maximum number of characters allowed in the field
     * @param action the {@link Runnable} to execute on document changes; must not be null
     */
    public static void attachLimitedDocumentListener(
            JTextField field, int maxLength, Runnable action) {
        if (Checker.INSTANCE.checkNPE(field, "attachLimitedDocumentListener", "field")
                || Checker.INSTANCE.checkNPE(action, "attachLimitedDocumentListener", "action")) {
            return;
        }
        DocumentFilter lengthFilter =
                new DocumentFilter() {
                    @Override
                    public void insertString(
                            FilterBypass fb, int offset, String string, AttributeSet attr)
                            throws BadLocationException {
                        if (string == null) {
                            return;
                        }
                        String cleaned = string.replace("\n", "").replace("\r", "");
                        if (fb.getDocument().getLength() + cleaned.length() <= maxLength) {
                            super.insertString(fb, offset, cleaned, attr);
                        }
                    }

                    @Override
                    public void replace(
                            FilterBypass fb,
                            int offset,
                            int length,
                            String text,
                            AttributeSet attrs)
                            throws BadLocationException {
                        if (text == null) {
                            return;
                        }
                        String cleaned = text.replace("\n", "").replace("\r", "");
                        int currentLength = fb.getDocument().getLength();
                        if (currentLength - length + cleaned.length() <= maxLength) {
                            super.replace(fb, offset, length, cleaned, attrs);
                        }
                    }
                };
        ((AbstractDocument) field.getDocument()).setDocumentFilter(lengthFilter);
        DocumentListener listener = createDocumentListener(action);
        field.getDocument().addDocumentListener(listener);
    }

    /**
     * Adds a labeled row to a panel using {@link GridBagLayout}.
     *
     * <p>The row consists of a {@link JLabel} on the left and the specified {@link JComponent} on
     * the right. Optionally, a tooltip can be set for the label.
     *
     * @param panel the {@link JPanel} to which the row is added; must not be null
     * @param gbc the {@link GridBagConstraints} used for layout positioning; must not be null
     * @param labelText the text for the {@link JLabel}; must not be null
     * @param tooltipText the tooltip text for the label; may be null or blank
     * @param component the {@link JComponent} to add next to the label; must not be null
     */
    public void addRow(
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
     * Creates default {@link GridBagConstraints} for consistent layout.
     *
     * <p>The default constraints set uniform insets of 3 pixels on all sides, fill horizontally,
     * start at {@code gridx = 0}, {@code gridy = -1}, and assign {@code weightx = 1} for proper
     * resizing behavior.
     *
     * @return a new {@link GridBagConstraints} instance with default settings
     */
    public GridBagConstraints createDefaultConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = -1;
        gbc.weightx = 1;
        return gbc;
    }

    /**
     * Creates a JButton styled with a Material Icon and a text label.
     *
     * <p>Uses an HTML table structure for consistent vertical alignment of the icon and text.
     *
     * @param iconCode The Unicode value of the Material Icon (e.g., {@code "\uE2C7"}).
     * @param text The text label to display next to the icon.
     * @return A new, styled {@code JButton} configured with the icon and text.
     */
    public JButton createIconButton(String iconCode, String text) {
        String html =
                String.format(
                        "<html>"
                                + "<table cellpadding=0 cellspacing=0>"
                                + "<tr>"
                                + "<td style='vertical-align: middle;'>"
                                + "<font face=\"Material Icons\" size=\"+1\">%s</font>"
                                + "</td>"
                                + "<td style='vertical-align: middle;'>&nbsp;%s</td>"
                                + "</tr>"
                                + "</table>"
                                + "</html>",
                        iconCode, text);
        return new JButton(html);
    }

    /**
     * Creates a JButton styled to display a **bold** Material Icon only.
     *
     * @param iconCode The Unicode value of the Material Icon (e.g., "\uE863").
     * @return A new JButton containing only the icon.
     */
    public static JButton createIconOnlyButton(String iconCode) {
        String html =
                String.format(
                        "<html><b><font face=\"Material Icons\" size=\"+1\">%s</font></b></html>",
                        iconCode);
        return new JButton(html);
    }

    /**
     * Generates the HTML string for a JLabel with text followed by an icon.
     *
     * @param text The text label to display first.
     * @param iconCode The Unicode value of the Material Icon (e.g., {@code "\uE161"}).
     * @return The formatted HTML string.
     */
    public static String getIconAfterTextHtml(String text, String iconCode) {
        return String.format(
                "<html>"
                        + "<table cellpadding=0 cellspacing=0>"
                        + "<tr>"
                        + "<td style='vertical-align: middle;'>%s&nbsp;</td>"
                        + "<td style='vertical-align: middle;'>"
                        + "<font face=\"Material Icons\" size=\"+0\">%s</font>"
                        + "</td>"
                        + "</tr>"
                        + "</table>"
                        + "</html>",
                text, iconCode);
    }
}
