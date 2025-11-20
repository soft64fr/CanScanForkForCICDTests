/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme;
import com.github.lgooddatepicker.components.TimePicker;

import fr.softsf.canscan.constant.StringConstants;
import fr.softsf.canscan.util.UseLucioleFont;

/**
 * Simulates Native Image configuration behavior for UI testing and preview without generating the
 * actual image.
 */
@SuppressWarnings("CallToPrintStackTrace")
public class NativeImageConfigSimulator {

    /**
     * Launches the Native Image configuration UI and runs the end-to-end simulation in a background
     * thread.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        System.out.println(
                "\n[e2e INFO] Demarrage de la generation de configuration Native Image...");
        try {
            FlatCobalt2IJTheme.setup();
            UseLucioleFont.INSTANCE.initialize();
        } catch (Exception e) {
            System.err.println("[e2e ERROR] dans le setup du theme: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        SwingUtilities.invokeLater(
                () -> {
                    CanScan frame = new CanScan();
                    frame.setVisible(true);
                    SwingUtilities.invokeLater(
                            () ->
                                    new Thread(
                                                    () -> {
                                                        try {
                                                            runE2ESimulation(frame);
                                                            System.out.println(
                                                                    "[e2e INFO] Configuration"
                                                                        + " Native Image generee"
                                                                        + " avec succes\n");

                                                            System.exit(0);
                                                        } catch (Exception e) {
                                                            System.err.println(
                                                                    "[e2e ERROR] dans l'appel de la"
                                                                            + " simulation: "
                                                                            + e.getMessage());
                                                            e.printStackTrace();
                                                            System.exit(1);
                                                        }
                                                    })
                                            .start());
                });
    }

    /**
     * Runs the full end-to-end UI simulation for Native Image configuration using a {@link Robot}.
     * Locates required components, simulates user interactions, and logs progress or errors.
     *
     * @param rootContainer the container holding all named UI components
     */
    private static void runE2ESimulation(Container rootContainer) {
        try {
            Robot robot = new Robot();
            robot.setAutoDelay(100);
            robot.setAutoWaitForIdle(true);
            // Trouver les composants par leur nom
            JSlider ratioSlider = findComponent(rootContainer, "ratioSlider", JSlider.class);
            JTextField nameField = findComponent(rootContainer, "nameField", JTextField.class);
            JButton browseButton = findComponent(rootContainer, "browseButton", JButton.class);
            JButton qrColorButton = findComponent(rootContainer, "qrColorButton", JButton.class);
            JRadioButton freeRadio = findComponent(rootContainer, "freeRadio", JRadioButton.class);
            JRadioButton meetRadio = findComponent(rootContainer, "meetRadio", JRadioButton.class);
            JTextArea freeField = findComponent(rootContainer, "freeField", JTextArea.class);
            TimePicker meetBeginTimePicker =
                    findComponent(rootContainer, "meetBeginTimePicker", TimePicker.class);
            System.out.println("Tous les composants ont ete trouves");
            // Tests
            ratioSliderTooltipSimulation(ratioSlider, robot);
            nameTypingSimulation(nameField, robot);
            browseFoldersSimulation(browseButton, robot);
            chooseModuleColor(qrColorButton, robot);
            freeDataTooBig(freeRadio, freeField, robot);
            selectABeginTime(meetRadio, meetBeginTimePicker, robot);
            System.out.println("\n=== SIMULATION E2E TERMINEE ===\n");
        } catch (Exception e) {
            System.err.println("[e2e ERROR] Dans la simulation E2E: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Simulates activating the meeting mode and selecting a start time through the TimePicker.
     * Opens the time menu, chooses the first value using keyboard navigation, and verifies the
     * resulting selected time.
     *
     * @param meetRadio the radio button used to enable meeting mode
     * @param meetBeginTimePicker the TimePicker used to select the start time
     * @param robot the Robot used for UI interaction
     * @throws Exception if UI interaction fails or validation does not match
     */
    private static void selectABeginTime(
            JRadioButton meetRadio, TimePicker meetBeginTimePicker, Robot robot) throws Exception {
        String expected = "00:00";
        Point meetRadioLocation = meetRadio.getLocationOnScreen();
        robot.mouseMove(meetRadioLocation.x + 10, meetRadioLocation.y + 10);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(500);
        Point beginTimePickerLocation =
                meetBeginTimePicker.getComponentToggleTimeMenuButton().getLocationOnScreen();
        robot.mouseMove(beginTimePickerLocation.x + 10, beginTimePickerLocation.y + 10);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        pressDown(robot);
        pressEnter(robot);
        String actual = meetBeginTimePicker.getComponentTimeTextField().getText();
        assertEquals("\n=== Test 6 : Verification du selecteur horaire ===\n", expected, actual);
    }

    /**
     * Simulates selecting the free data mode and entering an oversized payload into the input
     * field. Confirms the resulting error dialog and verifies its title.
     *
     * @param freeRadio the radio button to activate free data mode
     * @param freeField the text area for free-form input
     * @param robot the Robot used for UI interaction
     * @throws Exception if the dialog is not intercepted or validation fails
     */
    private static void freeDataTooBig(JRadioButton freeRadio, JTextArea freeField, Robot robot)
            throws Exception {
        Point freeRadioLocation = freeRadio.getLocationOnScreen();
        robot.mouseMove(freeRadioLocation.x + 10, freeRadioLocation.y + 10);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(500);
        freeField.setText("W".repeat(2000));
        robot.delay(500);
        String actual = interceptAndValideDialog(robot);
        assertEquals(
                "\n=== Test 5 : Verification de free Data too big ===\n",
                StringConstants.ERREUR.getValue(),
                actual);
    }

    /**
     * Simulates selecting a color via {@link JColorChooser}, confirms the dialog with Enter, and
     * verifies the selected value.
     *
     * @param qrColorButton the button that opens the color chooser
     * @param robot the Robot used for UI interaction
     * @throws Exception if the dialog is not found or validation fails
     */
    private static void chooseModuleColor(JButton qrColorButton, Robot robot) throws Exception {
        Point qrColorButtonLocation = qrColorButton.getLocationOnScreen();
        robot.mouseMove(qrColorButtonLocation.x + 10, qrColorButtonLocation.y + 10);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(500);
        JDialog colorDialog = null;
        for (int i = 0; i < 10; i++) {
            for (Window window : Window.getWindows()) {
                if (window.isShowing()
                        && window instanceof JDialog dialog
                        && dialog.getTitle().equals("Choisir la couleur des modules")) {
                    colorDialog = dialog;
                    break;
                }
            }
            if (colorDialog != null) break;
            robot.delay(200);
        }
        if (colorDialog == null) {
            throw new RuntimeException("La boîte de dialogue JColorChooser n'a pas ete trouvee");
        }
        pressEnter(robot);
        String expected = "#000000";
        String actual = qrColorButton.getText();
        assertEquals(
                "\n=== Test 4 : Verification de la couleur selectionnee ===\n", expected, actual);
    }

    /**
     * Simulates clicking a browse button to open a {@link JFileChooser}, performs a right-click
     * inside it, verifies that a popup menu appears, and closes the dialog.
     *
     * @param browseButton the button that triggers the file chooser
     * @param robot the Robot used for mouse interaction
     * @throws Exception if the chooser or popup menu is not detected
     */
    private static void browseFoldersSimulation(JButton browseButton, Robot robot)
            throws Exception {
        String expected = "ok";
        String actual = "ok";
        Point browseButtonLocation = browseButton.getLocationOnScreen();
        robot.mouseMove(browseButtonLocation.x + 10, browseButtonLocation.y + 10);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(500);
        JFileChooser fileChooser = null;
        JDialog dialog = null;
        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window instanceof JDialog jd) {
                for (Component comp : jd.getContentPane().getComponents()) {
                    if (comp instanceof JFileChooser chooser) {
                        fileChooser = chooser;
                        dialog = jd;
                        break;
                    }
                }
            }
        }
        if (fileChooser == null) {
            actual = "nok";
        } else {
            Point chooserLocation = fileChooser.getLocationOnScreen();
            robot.mouseMove(
                    chooserLocation.x + fileChooser.getWidth() / 2,
                    chooserLocation.y + fileChooser.getHeight() / 2);
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            robot.delay(500);
            boolean popupVisible = false;
            for (Window window : Window.getWindows()) {
                if (window.isVisible()
                        && window instanceof JWindow
                        && window.getClass().getName().contains("Popup$HeavyWeightWindow")) {
                    Container content = ((JWindow) window).getContentPane();
                    for (Component comp : content.getComponents()) {
                        if (comp instanceof JPopupMenu popup && popup.getComponentCount() > 0) {
                            Component firstItem = popup.getComponent(0);
                            if (firstItem instanceof JMenuItem menuItem) {
                                String firstText = menuItem.getText();
                                if (firstText != null && !firstText.isBlank()) {
                                    popupVisible = true;
                                }
                            }
                            break;
                        }
                    }
                }
            }
            if (!popupVisible) {
                actual = "nok";
            }
        }
        Objects.requireNonNull(dialog, "dialog ne doit pas etre null");
        dialog.dispose();
        assertEquals("\n=== Test 3 : Verification du selecteur de fichier ===\n", expected, actual);
    }

    /**
     * Simulates typing "Test" into a {@link JTextField} and verifies the input using a {@link
     * Robot}.
     *
     * @param nameField the text field to interact with
     * @param robot the Robot used for mouse and keyboard actions
     * @throws Exception if input or verification fails
     */
    private static void nameTypingSimulation(JTextField nameField, Robot robot) throws Exception {
        Point nameFieldLocation = nameField.getLocationOnScreen();
        robot.mouseMove(nameFieldLocation.x + 10, nameFieldLocation.y + 10);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(200);
        String expected = "test";
        typeString(robot, expected);
        robot.delay(500);
        assertEquals(
                "\n=== Test 2: Verification de la saisie du nom ===\n",
                expected.toLowerCase(),
                nameField.getText().toLowerCase());
    }

    /**
     * Simulates hovering over a {@link JSlider} to verify its tooltip matches the current slider
     * value.
     *
     * @param ratioSlider the slider to test
     * @param robot the Robot used for mouse interaction
     * @throws Exception if the tooltip does not match the expected value
     */
    private static void ratioSliderTooltipSimulation(JSlider ratioSlider, Robot robot)
            throws Exception {
        Point ratioSliderLocation = ratioSlider.getLocationOnScreen();
        robot.mouseMove(
                ratioSliderLocation.x + ratioSlider.getWidth() / 2,
                ratioSliderLocation.y + ratioSlider.getHeight() / 2);
        robot.delay(1500);
        String expectedTooltip = ratioSlider.getValue() + "%";
        String actualTooltip = ratioSlider.getToolTipText();
        assertEquals(
                "\n=== Test 1 : Verification du tooltip ratioSlider ===\n",
                expectedTooltip,
                actualTooltip);
    }

    // ========== Util ==========

    /**
     * Simulates typing a full string using the Robot class. Each character is sent individually via
     * typeChar.
     *
     * @param robot the Robot instance used to simulate keystrokes
     * @param text the string to be typed
     */
    private static void typeString(Robot robot, String text) {
        for (char c : text.toCharArray()) {
            typeChar(robot, c);
        }
    }

    /**
     * Simulates typing a single character using the Robot class. SHIFT is pressed if the character
     * is uppercase or numeric.
     *
     * @param robot the Robot instance used to simulate keystrokes
     * @param c the character to be typed
     */
    private static void typeChar(Robot robot, char c) {
        boolean shift = Character.isUpperCase(c) || Character.isDigit(c);
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
        if (shift) {
            robot.keyPress(KeyEvent.VK_SHIFT);
        }
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
        if (shift) {
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }
        robot.delay(50);
    }

    /**
     * Compares two strings and prints the test result with a label. Throws an exception if the
     * values do not match.
     *
     * @param label Description of the test being performed (e.g \n=== Test 1: Verification du
     *     tooltip ratioSlider ===\n).
     * @param expected Expected string value (e.g. 27%).
     * @param actual Actual string value to compare (e.g. 27%).
     * @throws Exception if expected and actual values differ.
     */
    private static void assertEquals(String label, String expected, String actual)
            throws Exception {
        System.out.println(label + (expected.equals(actual) ? "PASS" : "FAIL"));
        if (!expected.equals(actual)) {
            throw new Exception(
                    label
                            + " , le test a echoue : valeur attendue '"
                            + expected
                            + "', valeur actuelle '"
                            + actual
                            + "'");
        }
    }

    /**
     * Searches for a component of the specified type and name within the given container hierarchy.
     * Starts the search from the root container.
     *
     * @param container the root container to search within
     * @param name the name of the component to find (must match getName())
     * @param type the expected class type of the component
     * @param <T> the type of the component to return
     * @return the matching component if found
     * @throws IllegalArgumentException if no matching component is found
     */
    public static <T extends Component> T findComponent(
            Container container, String name, Class<T> type) {
        return findComponent(container, container, name, type);
    }

    /**
     * Recursively searches for a component of the specified type and name within the container
     * hierarchy.
     *
     * @param root the original root container (used for error reporting)
     * @param current the current container being searched
     * @param name the name of the component to find
     * @param type the expected class type of the component
     * @param <T> the type of the component to return
     * @return the matching component if found, or null if not found in this branch
     * @throws IllegalArgumentException if the component is not found in the entire hierarchy
     */
    @SuppressWarnings("unchecked")
    private static <T extends Component> T findComponent(
            Container root, Container current, String name, Class<T> type) {
        for (Component comp : current.getComponents()) {
            if (type.isInstance(comp) && name.equals(comp.getName())) return (T) comp;
            if (comp instanceof Container cont) {
                T found = findComponent(root, cont, name, type);
                if (found != null) return found;
            }
        }
        if (root == current) {
            throw new IllegalArgumentException(
                    String.format(
                            "Component not found: type=%s, name=\"%s\", root container=%s",
                            type.getSimpleName(),
                            name,
                            root.getClass().getSimpleName()
                                    + (root.getName() != null
                                            ? " [name=" + root.getName() + "]"
                                            : "")));
        }
        return null;
    }

    /**
     * Confirms and closes a visible {@link JOptionPane} by pressing Enter, then returns the dialog
     * title.
     *
     * @param robot the keyboard automation tool
     * @return the title of the validated dialog
     * @throws RuntimeException if no dialog is found within the timeout
     */
    private static String interceptAndValideDialog(Robot robot) {
        for (int i = 0; i < 20; i++) {
            for (Window window : Window.getWindows()) {
                if (window.isShowing() && window instanceof JDialog dialog) {
                    JOptionPane pane = findOptionPane(dialog);
                    if (pane != null) {
                        pressEnter(robot);
                        return dialog.getTitle();
                    }
                }
            }
            robot.delay(300);
        }
        throw new RuntimeException("JOptionPane non intercepte ou validation impossible");
    }

    /**
     * Recursively searches the given container and its descendants for a {@link JOptionPane}
     * instance.
     *
     * @param container the root container to search within
     * @return the first {@code JOptionPane} found, or {@code null} if none is present
     */
    private static JOptionPane findOptionPane(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JOptionPane pane) {
                return pane;
            }
            if (c instanceof Container child) {
                JOptionPane found = findOptionPane(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Simulates pressing the Enter key using the provided {@link Robot}, followed by a short delay.
     *
     * @param robot the Robot instance used to perform the key press
     */
    private static void pressEnter(Robot robot) {
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.delay(300);
    }

    /**
     * Simulates pressing the Down arrow key using the provided {@link Robot}, followed by a short
     * delay.
     *
     * @param robot the Robot instance used to perform the key press
     */
    private static void pressDown(Robot robot) {
        robot.keyPress(KeyEvent.VK_DOWN);
        robot.keyRelease(KeyEvent.VK_DOWN);
        robot.delay(300);
    }
}
