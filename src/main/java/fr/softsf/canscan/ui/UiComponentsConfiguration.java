/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

import org.apache.commons.lang3.StringUtils;

import fr.softsf.canscan.model.MecardJFields;
import fr.softsf.canscan.util.Checker;
import fr.softsf.canscan.util.FloatConstants;
import fr.softsf.canscan.util.IntConstants;

/** Creating and configuring UI components. */
public enum UiComponentsConfiguration {
    INSTANCE;

    private static final int MULTILINE_TEXT_FIELDS_ROWS = 10;
    private static final int RADIO_BUTTON_GAP = 20;
    private static final int COLOR_BUTTONS_GAP = 10;
    private static final int MAJOR_TICK_SPACING = 25;
    private static final double GBC_COLOR_BUTTONS_WEIGHT_X = 0.5;
    private static final int GENERATE_BUTTON_EXTRA_HEIGHT = 35;
    private static final String CREATE_MODE_PANEL = "createModePanel";
    private static final String ADD_ROW = "addRow";

    /** Configures margin slider with standard settings. */
    public void configureMarginSlider(JSlider slider, int initialValue) {
        if (Checker.INSTANCE.checkNPE(slider, "configureMarginSlider", "slider")) {
            return;
        }
        slider.setValue(initialValue);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
    }

    /** Configures ratio slider with standard settings and tooltip updates. */
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

    /** Creates mode selection panel with radio buttons and update button. */
    public JPanel createModePanel(
            JRadioButton mecardRadio,
            JRadioButton freeRadio,
            JButton updateButton,
            ButtonGroup buttonGroup) {
        if (Checker.INSTANCE.checkNPE(mecardRadio, CREATE_MODE_PANEL, "mecardRadio")
                || Checker.INSTANCE.checkNPE(freeRadio, CREATE_MODE_PANEL, "freeRadio")
                || Checker.INSTANCE.checkNPE(updateButton, CREATE_MODE_PANEL, "updateButton")
                || Checker.INSTANCE.checkNPE(buttonGroup, CREATE_MODE_PANEL, "buttonGroup")) {
            return null;
        }
        JPanel modePanel = new JPanel(new BorderLayout());
        mecardRadio.setSelected(true);
        buttonGroup.add(mecardRadio);
        buttonGroup.add(freeRadio);
        JPanel radioButtonsPanel = createRadioButtonsPanel(mecardRadio, freeRadio);
        modePanel.add(radioButtonsPanel, BorderLayout.WEST);
        modePanel.add(updateButton, BorderLayout.EAST);
        return modePanel;
    }

    /** Creates panel with aligned radio buttons. */
    private JPanel createRadioButtonsPanel(JRadioButton mecardRadio, JRadioButton freeRadio) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, RADIO_BUTTON_GAP);
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(mecardRadio, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(freeRadio, gbc);
        return panel;
    }

    /** Creates color selection panel with QR and background color buttons. */
    public JPanel createColorPanel(JButton qrColorButton, JButton bgColorButton) {
        if (Checker.INSTANCE.checkNPE(qrColorButton, "createColorPanel", "qrColorButton")
                || Checker.INSTANCE.checkNPE(bgColorButton, "createColorPanel", "bgColorButton")) {
            return null;
        }
        JPanel colorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = GBC_COLOR_BUTTONS_WEIGHT_X;
        gbc.insets = new Insets(0, 0, 0, COLOR_BUTTONS_GAP);
        colorPanel.add(bgColorButton, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        colorPanel.add(qrColorButton, gbc);
        return colorPanel;
    }

    /** Configures text area for free text input with wrapping and size. */
    public void configureFreeTextArea(JTextArea textArea, JScrollPane scrollPane) {
        if (Checker.INSTANCE.checkNPE(textArea, "configureFreeTextArea", "textArea")
                || Checker.INSTANCE.checkNPE(scrollPane, "configureFreeTextArea", "scrollPane")) {
            return;
        }
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        var fm = textArea.getFontMetrics(textArea.getFont());
        int charHeight = fm.getHeight();
        int charWidth = fm.charWidth('W');
        Dimension size =
                new Dimension(
                        charWidth * IntConstants.TEXT_FIELDS_COLUMNS.getValue(),
                        charHeight * MULTILINE_TEXT_FIELDS_ROWS);
        scrollPane.setPreferredSize(size);
        scrollPane.setMinimumSize(size);
        scrollPane.setMaximumSize(size);
    }

    /** Configures generate button with standard size and disabled state. */
    public void configureGenerateButton(JButton button, java.awt.event.ActionListener listener) {
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

    /** Creates overlay panel containing loader and QR code label. */
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

    /** Creates and populates MECARD input panel. */
    public void populateMecardPanel(
            JPanel mecardPanel, GridBagConstraints grid, MecardJFields mecardJFields) {
        if (Checker.INSTANCE.checkNPE(mecardPanel, "populateMecardPanel", "mecardPanel")
                || Checker.INSTANCE.checkNPE(grid, "populateMecardPanel", "grid")) {
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

    /** Creates and populates FREE text input panel. */
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

    /** Assigns component names for testing and identification. */
    public void assignComponentNames(
            JTextField nameField,
            JButton browseButton,
            JSlider ratioSlider,
            JButton qrColorButton,
            JRadioButton freeRadio,
            JTextArea freeField) {
        if (nameField != null) {
            nameField.setName("nameField");
        }
        if (browseButton != null) {
            browseButton.setName("browseButton");
        }
        if (ratioSlider != null) {
            ratioSlider.setName("ratioSlider");
        }
        if (qrColorButton != null) {
            qrColorButton.setName("qrColorButton");
        }
        if (freeRadio != null) {
            freeRadio.setName("freeRadio");
        }
        if (freeField != null) {
            freeField.setName("freeField");
        }
    }

    /** Creates simple DocumentListener that executes action on any document change. */
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

    /** Adds labeled row to panel using GridBagLayout. */
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

    /** Creates default GridBagConstraints for consistent layout. */
    public GridBagConstraints createDefaultConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = -1;
        gbc.weightx = 1;
        return gbc;
    }
}
