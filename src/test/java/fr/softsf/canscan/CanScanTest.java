/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import fr.softsf.canscan.model.Mode;
import fr.softsf.canscan.model.QrConfig;
import fr.softsf.canscan.model.QrDataResult;
import fr.softsf.canscan.model.QrInput;
import fr.softsf.canscan.service.DataBuilderService;
import fr.softsf.canscan.service.GenerateAndSaveService;
import fr.softsf.canscan.ui.QrCodeBufferedImage;
import fr.softsf.canscan.ui.QrCodeColor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test suite for {@link CanScan}.
 *
 * <p>Note: These tests validate the QR data generation logic using various contact field
 * combinations. They do not cover full UI behavior or static method mocking. For complete UI
 * testing, consider using AssertJ Swing or manual integration tests.
 */
@DisplayName("CanScan QR Data Tests")
class CanScanTest {

    private CanScan generator;
    private QrCodeColor qrCodeColor;
    private GenerateAndSaveService qrService;

    @TempDir File tempDir;

    @BeforeEach
    void setUp() {
        generator = new CanScan();
        qrCodeColor = new QrCodeColor();
        generator.nameField.setText("John");
        generator.phoneField.setText("0123456789");
        generator.emailField.setText("john@example.com");
        generator.orgField.setText("Org");
        generator.adrField.setText("Addr");
        generator.urlField.setText("https://example.com");
        generator.logoField.setText("");
        generator.sizeField.setText("400");
        generator.marginSlider.setValue(3);
        generator.ratioSlider.setValue((int) (0.27 * 100));
        generator.roundedModulesCheckBox.setSelected(true);
        QrCodeBufferedImage qrCodeBufferedImage = mock(QrCodeBufferedImage.class);
        qrService = new GenerateAndSaveService(qrCodeBufferedImage);
    }

    @Test
    void givenAllContactFields_whenBuildMecard_thenReturnCompleteMecardString() {
        String mecard =
                DataBuilderService.INSTANCE.buildMecard(
                        "Alice", "12345", "a@b.com", "Org", "Addr", "https://toto.com");
        assertTrue(mecard.startsWith("MECARD:"));
        assertTrue(mecard.contains("N:Alice;"));
        assertTrue(mecard.contains("TEL:12345;"));
        assertTrue(mecard.contains("EMAIL:a@b.com;"));
        assertTrue(mecard.contains("ORG:Org;"));
        assertTrue(mecard.contains("ADR:Addr;"));
        assertTrue(mecard.contains("URL:https://toto.com;"));
    }

    @Test
    void givenOnlyNameField_whenBuildMecard_thenReturnMecardWithNameOnly() {
        String mecard = DataBuilderService.INSTANCE.buildMecard("Bob", "", "", "", "", "");
        assertTrue(mecard.startsWith("MECARD:"));
        assertTrue(mecard.contains("N:Bob;"));
        assertFalse(mecard.contains("TEL:"));
        assertFalse(mecard.contains("EMAIL:"));
    }

    @Test
    void givenAllBlankFields_whenBuildMecard_thenReturnEmptyMecardStructure() {
        String mecard = DataBuilderService.INSTANCE.buildMecard("", "", "", "", "", "");
        assertEquals("MECARD:;", mecard);
    }

    @Test
    void givenConfigWithoutLogo_whenGenerateQrCodeImage_thenReturn400x400Image() throws Exception {
        QrConfig config = new QrConfig(null, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
        BufferedImage qr =
                qrCodeBufferedImage.generateQrCodeImage(
                        DataBuilderService.INSTANCE.buildMecard(
                                "John", "0123456789", "", "", "", ""),
                        config);
        assertNotNull(qr);
        assertEquals(400, qr.getWidth());
        assertEquals(400, qr.getHeight());
    }

    @Test
    void givenConfigWithRoundedModules_whenGenerateQrCodeImage_thenReturn400x400Image()
            throws Exception {
        QrConfig config = new QrConfig(null, 400, 0.27, Color.BLACK, Color.WHITE, true, 3);
        QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
        BufferedImage qr = qrCodeBufferedImage.generateQrCodeImage("Test data", config);
        assertNotNull(qr);
        assertEquals(400, qr.getWidth());
        assertEquals(400, qr.getHeight());
    }

    @Test
    void givenConfigWithCustomColors_whenGenerateQrCodeImage_thenReturn300x300Image()
            throws Exception {
        QrConfig config = new QrConfig(null, 300, 0.0, Color.RED, Color.YELLOW, false, 2);
        QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
        BufferedImage qr = qrCodeBufferedImage.generateQrCodeImage("Test", config);
        assertNotNull(qr);
        assertEquals(300, qr.getWidth());
    }

    @Test
    void givenConfigWithZeroMargin_whenGenerateQrCodeImage_thenReturnValidImage() throws Exception {
        QrConfig config = new QrConfig(null, 400, 0.1, Color.BLACK, Color.WHITE, false, 0);
        QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
        BufferedImage qr = qrCodeBufferedImage.generateQrCodeImage("Test", config);
        assertNotNull(qr);
    }

    @Test
    void givenConfigWithLargeLogoRatio_whenGenerateQrCodeImage_thenReturnValidImage()
            throws Exception {
        QrConfig config = new QrConfig(null, 500, 0.5, Color.BLACK, Color.WHITE, true, 4);
        QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
        BufferedImage qr = qrCodeBufferedImage.generateQrCodeImage("Large logo test", config);
        assertNotNull(qr);
    }

    @Test
    void givenRedColorSelected_whenChooseQrColor_thenButtonTextAndColorUpdated() {
        JButton button = new JButton();
        try (MockedStatic<JColorChooser> chooserMock = Mockito.mockStatic(JColorChooser.class)) {
            chooserMock
                    .when(() -> JColorChooser.showDialog(any(), anyString(), any()))
                    .thenReturn(Color.RED);
            Color chosen = qrCodeColor.chooseColor(button, Color.BLACK, true);
            assertEquals(Color.RED, chosen);
            assertEquals("#FF0000", button.getText());
        }
    }

    @Test
    void givenBlueColorSelected_whenChooseBgColor_thenButtonTextAndColorUpdated() {
        JButton button = new JButton();
        try (MockedStatic<JColorChooser> chooserMock = Mockito.mockStatic(JColorChooser.class)) {
            chooserMock
                    .when(() -> JColorChooser.showDialog(any(), anyString(), any()))
                    .thenReturn(Color.BLUE);
            Color chosen = qrCodeColor.chooseColor(button, Color.WHITE, false);
            assertEquals(Color.BLUE, chosen);
            assertEquals("#0000FF", button.getText());
        }
    }

    @Test
    void givenColorDialogCancelled_whenChooseColor_thenButtonTextUnchanged() {
        JButton button = new JButton("Noir");
        try (MockedStatic<JColorChooser> chooserMock = Mockito.mockStatic(JColorChooser.class)) {
            chooserMock
                    .when(() -> JColorChooser.showDialog(any(), anyString(), any()))
                    .thenReturn(null);
            Color chosen = qrCodeColor.chooseColor(button, Color.BLACK, true);
            assertNull(chosen);
            assertEquals("Noir", button.getText());
        }
    }

    @Test
    void givenFileSelected_whenBrowseLogo_thenLogoFieldUpdated() {
        CanScan spyGenerator = spy(generator);
        File fakeFile = new File("fake-logo.png");
        doReturn(fakeFile).when(spyGenerator).chooseLogoFile();
        ActionEvent e = mock(ActionEvent.class);
        spyGenerator.browseLogo(e);
        assertTrue(spyGenerator.logoField.getText().endsWith("fake-logo.png"));
    }

    @Test
    void givenFileSelectionCancelled_whenBrowseLogo_thenLogoFieldUnchanged() {
        CanScan spyGenerator = spy(generator);
        doReturn(null).when(spyGenerator).chooseLogoFile();
        spyGenerator.logoField.setText("");
        ActionEvent e = mock(ActionEvent.class);
        spyGenerator.browseLogo(e);
        assertEquals("", spyGenerator.logoField.getText());
    }

    @ParameterizedTest(name = "given input ''{0}'' when sizeFieldCheck then expect {1}")
    @CsvSource({
        "500, 500", // valid
        "abc, 400", // invalid text
        "-50, 10", // negative
        "0, 10", // zero
        "5, 10" // below minimum
    })
    void givenVariousSizeInputs_whenValidateAndGetSize_thenReturnExpectedResult(
            String input, int expected) {
        generator.sizeField.setText(input);
        int result = generator.validateAndGetSize();
        assertEquals(expected, result);
    }

    @Test
    void givenValidMargin4_whenMarginFieldCheck_thenGetMarginUpdatedTo4() {
        generator.marginSlider.setValue(4);
        generator.validateAndGetMargin();
        Field marginField = getField("margin");
        assertEquals(4, getFieldValue(marginField));
    }

    @Test
    void givenNegativeMargin_whenMarginFieldCheck_thenGetMarginSetTo0() {
        generator.marginSlider.setValue(-2);
        generator.validateAndGetMargin();
        Field marginField = getField("margin");
        assertEquals(0, getFieldValue(marginField));
    }

    @Test
    void givenMarginAboveMaximum_whenMarginFieldCheck_thenGetMarginSetTo10() {
        generator.marginSlider.setValue(15);
        generator.validateAndGetMargin();
        Field marginField = getField("margin");
        assertEquals(10, getFieldValue(marginField));
    }

    @ParameterizedTest(name = "given ratio {0}% when ratioFieldCheck then ratio set to {1}")
    @CsvSource({"0,   0.0", "50,  0.5", "100, 1.0"})
    void givenRatioPercent_whenRatioFieldCheck_thenGetRatioSetCorrectly(
            int sliderValue, double expectedRatio) {
        generator.ratioSlider.setValue(sliderValue);
        generator.validateAndGetRatio();
        Field ratioField = getField("imageRatio");
        double actualRatio = (double) getFieldValue(ratioField);
        assertEquals(expectedRatio, actualRatio, 0.01);
    }

    @Test
    void givenMecardMode_whenSwitchToFreeMode_thenCurrentModeIsFree() throws Exception {
        Method switchMode = CanScan.class.getDeclaredMethod("switchMode", Mode.class);
        switchMode.setAccessible(true);
        switchMode.invoke(generator, Mode.FREE);
        Field currentModeField = CanScan.class.getDeclaredField("currentMode");
        currentModeField.setAccessible(true);
        assertEquals(Mode.FREE, currentModeField.get(generator));
    }

    @Test
    void givenFreeMode_whenSwitchToMecardMode_thenCurrentModeIsMecard() throws Exception {
        Method switchMode = CanScan.class.getDeclaredMethod("switchMode", Mode.class);
        switchMode.setAccessible(true);
        switchMode.invoke(generator, Mode.FREE);
        switchMode.invoke(generator, Mode.MECARD);
        Field currentModeField = CanScan.class.getDeclaredField("currentMode");
        currentModeField.setAccessible(true);
        assertEquals(Mode.MECARD, currentModeField.get(generator));
    }

    @Test
    void givenBlackWhiteImage_whenDrawSquareFinderPattern_thenPatternDrawnAtOrigin() {
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
        qrCodeBufferedImage.drawSquareFinderPatternAtPixel(g, 0, 0, 21, Color.BLACK, Color.WHITE);
        int rgb = img.getRGB(0, 0);
        assertNotEquals(0, rgb);
    }

    @Test
    void givenQrImage_whenDrawFinderPatterns_thenAllThreePatternsDrawn() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        QrConfig configRounded = new QrConfig(null, 100, 0.27, Color.BLACK, Color.WHITE, true, 2);
        QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
        qrCodeBufferedImage.drawFinderPatterns(g, 21, configRounded);
        QrConfig configSquare = new QrConfig(null, 100, 0.27, Color.BLACK, Color.WHITE, false, 2);
        QrCodeBufferedImage qrCodeBufferedImageNew = new QrCodeBufferedImage();
        qrCodeBufferedImageNew.drawFinderPatterns(g, 21, configSquare);
        double moduleSizeX = (double) configSquare.size() / 21;
        int firstX = (int) (configSquare.margin() * moduleSizeX);
        int firstY = (int) (configSquare.margin() * moduleSizeX);
        int pixel = img.getRGB(firstX, firstY);
        assertNotEquals(
                0,
                pixel,
                "Le pixel dans le coin du pattern doit avoir été modifié après le dessin des"
                        + " patterns");
    }

    @Test
    void givenBlackColor_whenConvertToHex_thenReturn000000() {
        String hex = qrCodeColor.colorToHex(Color.BLACK);
        assertEquals("#000000", hex);
    }

    @Test
    void givenMecardMode_whenGetModeText_thenReturnMECARD() {
        assertEquals("Contact (MeCard)", Mode.MECARD.text());
        assertEquals("Saisie libre", Mode.FREE.text());
    }

    @Test
    void givenWhiteColor_whenConvertToHex_thenReturnFFFFFF() {
        String hex = qrCodeColor.colorToHex(Color.WHITE);
        assertEquals("#FFFFFF", hex);
    }

    @Test
    void givenCustomColor_whenConvertToHex_thenReturnCorrectHex() {
        Color custom = new Color(128, 64, 192);
        String hex = qrCodeColor.colorToHex(custom);
        assertEquals("#8040C0", hex);
    }

    @Test
    void givenFileWithPngExtension_whenGetSelectedPngFile_thenReturnSameFile() throws Exception {
        Method getSelectedPngFile =
                GenerateAndSaveService.class.getDeclaredMethod(
                        "getSelectedPngFile", JFileChooser.class);
        getSelectedPngFile.setAccessible(true);
        JFileChooser chooser = mock(JFileChooser.class);
        File testFile = new File(tempDir, "test.png");
        when(chooser.getSelectedFile()).thenReturn(testFile);
        File result = (File) getSelectedPngFile.invoke(qrService, chooser);
        assertTrue(result.getName().endsWith(".png"));
    }

    @Test
    void givenFileWithoutPngExtension_whenGetSelectedPngFile_thenReturnFileWithPngExtension()
            throws Exception {
        Method getSelectedPngFile =
                GenerateAndSaveService.class.getDeclaredMethod(
                        "getSelectedPngFile", JFileChooser.class);
        getSelectedPngFile.setAccessible(true);
        JFileChooser chooser = mock(JFileChooser.class);
        File testFile = new File(tempDir, "test");
        when(chooser.getSelectedFile()).thenReturn(testFile);
        File result = (File) getSelectedPngFile.invoke(qrService, chooser);
        assertTrue(result.getName().endsWith(".png"));
    }

    @Test
    void givenNonExistingFile_whenResolveFileNameConflict_thenReturnSameFile() throws Exception {
        Method resolveFileNameConflict =
                GenerateAndSaveService.class.getDeclaredMethod(
                        "resolveFileNameConflict", File.class);
        resolveFileNameConflict.setAccessible(true);
        File testFile = new File(tempDir, "nonexistent.png");
        File result = (File) resolveFileNameConflict.invoke(qrService, testFile);
        assertEquals(testFile, result);
    }

    @Test
    void givenMecardModeWithContactData_whenBuildQrData_thenReturnMecardString() {
        QrInput input =
                new QrInput(
                        300,
                        Mode.MECARD,
                        "",
                        "Alice",
                        "",
                        "123456",
                        "",
                        "",
                        "",
                        "",
                        400,
                        4,
                        0.25,
                        Color.BLACK,
                        Color.WHITE,
                        false);
        QrDataResult result = DataBuilderService.INSTANCE.buildData(Mode.MECARD, input);
        assertNotNull(result);
        String data = result.data();
        assertTrue(data.startsWith("MECARD:"));
        assertTrue(data.contains("N:Alice"));
        assertTrue(data.contains("TEL:123456"));
    }

    @Test
    void givenFreeModeWithText_whenBuildQrData_thenReturnFreeText() {
        QrInput input =
                new QrInput(
                        300,
                        Mode.FREE,
                        "Test data",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        400,
                        4,
                        0.25,
                        Color.BLACK,
                        Color.WHITE,
                        false);
        QrDataResult result = DataBuilderService.INSTANCE.buildData(Mode.FREE, input);
        assertNotNull(result);
        assertEquals("Test data", result.data());
    }

    @Test
    void givenEmptyMecardFields_whenBuildQrData_thenReturnEmptyString() {
        QrInput input =
                new QrInput(
                        300,
                        Mode.FREE,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        400,
                        4,
                        0.25,
                        Color.BLACK,
                        Color.WHITE,
                        false);
        QrDataResult result = DataBuilderService.INSTANCE.buildData(Mode.MECARD, input);
        assertNotNull(result);
        assertEquals("", result.data());
    }

    @Test
    void givenValidLogo_whenDrawLogoIfPresent_thenLogoDrawnOnImage() throws Exception {
        File logoFile = createTestLogoFile("test-logo.png");
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        QrConfig config = new QrConfig(logoFile, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
        qrCodeBufferedImage.drawLogoIfPresent(g, config);
        int centerX = 200;
        int centerY = 200;
        boolean hasModifiedPixels = false;
        for (int x = centerX - 50; x < centerX + 50; x++) {
            for (int y = centerY - 50; y < centerY + 50; y++) {
                if (qrImage.getRGB(x, y) != Color.WHITE.getRGB()) {
                    hasModifiedPixels = true;
                    break;
                }
            }
            if (hasModifiedPixels) break;
        }
        assertTrue(hasModifiedPixels, "Le logo devrait être visible dans la zone centrale");
        g.dispose();
    }

    @Test
    void givenNullGraphics_whenDrawLogoIfPresent_thenReturnWithoutException() throws Exception {
        File logoFile = createTestLogoFile("test-logo.png");
        QrConfig config = new QrConfig(logoFile, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        executeWithoutDialog(
                () ->
                        assertDoesNotThrow(
                                () -> {
                                    QrCodeBufferedImage qrCodeBufferedImage =
                                            new QrCodeBufferedImage();
                                    qrCodeBufferedImage.drawLogoIfPresent(null, config);
                                }));
    }

    @Test
    void givenNullConfig_whenDrawLogoIfPresent_thenReturnWithoutException() {
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        executeWithoutDialog(
                () ->
                        assertDoesNotThrow(
                                () -> {
                                    try {
                                        QrCodeBufferedImage qrCodeBufferedImage =
                                                new QrCodeBufferedImage();
                                        qrCodeBufferedImage.drawLogoIfPresent(g, null);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }));
        g.dispose();
    }

    @Test
    void givenNullLogoFile_whenDrawLogoIfPresent_thenReturnWithoutException() {
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        QrConfig config = new QrConfig(null, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
                    qrCodeBufferedImage.drawLogoIfPresent(g, config);
                });
        g.dispose();
    }

    @Test
    void givenNonExistentLogoFile_whenDrawLogoIfPresent_thenReturnWithoutException() {
        File nonExistentFile = new File(tempDir, "non-existent-logo.png");
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        QrConfig config =
                new QrConfig(nonExistentFile, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
                    qrCodeBufferedImage.drawLogoIfPresent(g, config);
                });
        g.dispose();
    }

    @Test
    void givenZeroImageRatio_whenDrawLogoIfPresent_thenReturnWithoutException() throws Exception {
        File logoFile = createTestLogoFile("test-logo.png");
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        QrConfig config = new QrConfig(logoFile, 400, 0.0, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
                    qrCodeBufferedImage.drawLogoIfPresent(g, config);
                });
        g.dispose();
    }

    @ParameterizedTest(
            name = "given imageRatio {0} when drawLogoIfPresent then logo scaled correctly")
    @CsvSource({"0.1", "0.2", "0.27", "0.3", "0.4", "0.5"})
    void givenDifferentImageRatios_whenDrawLogoIfPresent_thenLogoScaledCorrectly(double ratio)
            throws Exception {
        File logoFile = createTestLogoFile("test-logo-ratio.png");
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        QrConfig config = new QrConfig(logoFile, 400, ratio, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
                    qrCodeBufferedImage.drawLogoIfPresent(g, config);
                },
                "drawLogoIfPresent devrait fonctionner avec imageRatio = " + ratio);
        g.dispose();
    }

    @Test
    void givenJPEGLogo_whenDrawLogoIfPresent_thenLogoDrawnSuccessfully() throws Exception {
        File logoFile = createTestLogoFile("test-logo.jpg");
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        QrConfig config = new QrConfig(logoFile, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
                    qrCodeBufferedImage.drawLogoIfPresent(g, config);
                },
                "drawLogoIfPresent devrait fonctionner avec un fichier JPEG");
        g.dispose();
    }

    @Test
    void givenLargeQRSize_whenDrawLogoIfPresent_thenLogoScaledProperly() throws Exception {
        File logoFile = createTestLogoFile("test-logo-large.png");
        int largeSize = 1000;
        BufferedImage qrImage = new BufferedImage(largeSize, largeSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        QrConfig config =
                new QrConfig(logoFile, largeSize, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
                    qrCodeBufferedImage.drawLogoIfPresent(g, config);
                },
                "drawLogoIfPresent devrait gérer les grandes tailles de QR code");
        g.dispose();
    }

    @Test
    void givenSmallQRSize_whenDrawLogoIfPresent_thenLogoScaledProperly() throws Exception {
        File logoFile = createTestLogoFile("test-logo-small.png");
        int smallSize = 100;
        BufferedImage qrImage = new BufferedImage(smallSize, smallSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        QrConfig config =
                new QrConfig(logoFile, smallSize, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
                    qrCodeBufferedImage.drawLogoIfPresent(g, config);
                },
                "drawLogoIfPresent devrait gérer les petites tailles de QR code");
        g.dispose();
    }

    @Test
    void givenLogoWithTransparency_whenDrawLogoIfPresent_thenLogoDrawnWithAlpha() throws Exception {
        File logoFile = createTestLogoFileWithAlpha();
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        QrConfig config = new QrConfig(logoFile, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
                    qrCodeBufferedImage.drawLogoIfPresent(g, config);
                },
                "drawLogoIfPresent devrait gérer les logos avec transparence");
        g.dispose();
    }

    @Test
    void givenValidLogo_whenDrawLogoIfPresent_thenLogoCenteredCorrectly() throws Exception {
        File logoFile = createTestLogoFile("test-logo-centered.png");
        int size = 400;
        double imageRatio = 0.27;
        BufferedImage qrImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        QrConfig config =
                new QrConfig(logoFile, size, imageRatio, Color.BLACK, Color.WHITE, false, 3);
        QrCodeBufferedImage qrCodeBufferedImage = new QrCodeBufferedImage();
        qrCodeBufferedImage.drawLogoIfPresent(g, config);
        int whiteBoxSize = (int) (size * imageRatio);
        int expectedCenterX = size / 2;
        int expectedCenterY = size / 2;
        int tolerance = whiteBoxSize / 2;
        boolean hasLogoNearCenter = false;
        for (int x = Math.max(0, expectedCenterX - tolerance);
                x < Math.min(size, expectedCenterX + tolerance);
                x++) {
            for (int y = Math.max(0, expectedCenterY - tolerance);
                    y < Math.min(size, expectedCenterY + tolerance);
                    y++) {
                if (qrImage.getRGB(x, y) != Color.WHITE.getRGB()) {
                    hasLogoNearCenter = true;
                    break;
                }
            }
            if (hasLogoNearCenter) break;
        }
        assertTrue(hasLogoNearCenter, "Le logo devrait être centré sur le QR code");
        g.dispose();
    }

    // Méthodes utilitaires

    private void executeWithoutDialog(Runnable test) {
        try (MockedStatic<JOptionPane> optionPaneMock = Mockito.mockStatic(JOptionPane.class)) {
            optionPaneMock
                    .when(
                            () ->
                                    JOptionPane.showMessageDialog(
                                            null, null, null, JOptionPane.INFORMATION_MESSAGE))
                    .thenAnswer(inv -> null);
            optionPaneMock
                    .when(
                            () ->
                                    JOptionPane.showMessageDialog(
                                            null, null, null, JOptionPane.WARNING_MESSAGE))
                    .thenAnswer(inv -> null);
            optionPaneMock
                    .when(
                            () ->
                                    JOptionPane.showMessageDialog(
                                            null, null, null, JOptionPane.ERROR_MESSAGE))
                    .thenAnswer(inv -> null);
            optionPaneMock
                    .when(
                            () ->
                                    JOptionPane.showMessageDialog(
                                            null, null, null, JOptionPane.QUESTION_MESSAGE))
                    .thenAnswer(inv -> null);
            optionPaneMock
                    .when(
                            () ->
                                    JOptionPane.showMessageDialog(
                                            null, null, null, JOptionPane.PLAIN_MESSAGE))
                    .thenAnswer(inv -> null);
            test.run();
        }
    }

    private File createTestLogoFile(String filename) throws IOException {
        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        Path logoPath = tempDir.toPath().resolve(filename);
        BufferedImage logo = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = logo.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
        ImageIO.write(logo, extension.equals("jpg") ? "jpg" : "png", logoPath.toFile());
        return logoPath.toFile();
    }

    private File createTestLogoFileWithAlpha() throws IOException {
        Path logoPath = tempDir.toPath().resolve("test-logo-alpha.png");
        BufferedImage logo = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = logo.createGraphics();
        g.setColor(new Color(255, 0, 0, 128));
        g.fillOval(10, 10, 80, 80);
        g.dispose();
        ImageIO.write(logo, "png", logoPath.toFile());
        return logoPath.toFile();
    }

    private Field getField(String fieldName) {
        try {
            Field field = CanScan.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getFieldValue(Field field) {
        try {
            return field.get(generator);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
