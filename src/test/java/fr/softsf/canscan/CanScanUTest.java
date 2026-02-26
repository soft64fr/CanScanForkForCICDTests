/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import fr.softsf.canscan.model.CommonFields;
import fr.softsf.canscan.model.EncodedData;
import fr.softsf.canscan.model.Mode;
import fr.softsf.canscan.model.WholeFields;
import fr.softsf.canscan.service.DataBuilderService;
import fr.softsf.canscan.ui.ColorOperation;
import fr.softsf.canscan.ui.EncodedImage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/** Suite de tests unitaires pour la logique principale de CanScan. */
@DisplayName("*** CanScan tests ***")
class CanScanUTest {

    private CanScan generator;
    private ColorOperation colorOperation;

    @TempDir File tempDir;

    /** Initialisation de l'instance de test et configuration des champs par défaut. */
    @BeforeEach
    void setUp() throws InterruptedException, InvocationTargetException {
        javax.swing.SwingUtilities.invokeAndWait(() -> generator = new CanScan());
        colorOperation = new ColorOperation();
        generator.setNameFieldTextForTests();
        generator.setPhoneFieldTextForTests();
        generator.setEmailFieldTextForTests();
        generator.setOrgFieldTextForTests();
        generator.setAdrFieldTextForTests();
        generator.setUrlFieldTextForTests();
        generator.setLogoFieldTextForTests("");
        generator.setSizeFieldTextForTests("400");
        generator.setMarginSliderValueForTests(3);
        generator.setRatioSliderValueForTests((int) (0.27 * 100));
    }

    @Test
    @DisplayName("MECARD : Construction d'une chaîne complète avec tous les champs de contact")
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
    @DisplayName("MECARD : Construction avec le nom uniquement")
    void givenOnlyNameField_whenBuildMecard_thenReturnMecardWithNameOnly() {
        String mecard = DataBuilderService.INSTANCE.buildMecard("Bob", "", "", "", "", "");
        assertTrue(mecard.startsWith("MECARD:"));
        assertTrue(mecard.contains("N:Bob;"));
        assertFalse(mecard.contains("TEL:"));
        assertFalse(mecard.contains("EMAIL:"));
    }

    @Test
    @DisplayName("MECARD : Retourne une chaîne vide si aucun champ n'est saisi")
    void givenAllBlankFields_whenBuildMecard_thenReturnEmptyString() {
        String mecard = DataBuilderService.INSTANCE.buildMecard("", "", "", "", "", "");
        assertEquals("", mecard);
    }

    @Test
    @DisplayName("QR : Génération d'image 400x400 sans logo")
    void givenConfigWithoutLogo_whenGenerateQrCodeImage_thenReturn400x400Image() throws Exception {
        CommonFields config = new CommonFields(null, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        EncodedImage encodedImage = new EncodedImage();
        BufferedImage qr =
                encodedImage.generateImage(
                        DataBuilderService.INSTANCE.buildMecard(
                                "John", "0123456789", "", "", "", ""),
                        config);
        assertNotNull(qr);
        assertEquals(400, qr.getWidth());
        assertEquals(400, qr.getHeight());
    }

    @Test
    @DisplayName("QR : Génération avec modules arrondis")
    void givenConfigWithRoundedModules_whenGenerateQrCodeImage_thenReturn400x400Image()
            throws Exception {
        CommonFields config = new CommonFields(null, 400, 0.27, Color.BLACK, Color.WHITE, true, 3);
        EncodedImage encodedImage = new EncodedImage();
        BufferedImage qr = encodedImage.generateImage("Test data", config);
        assertNotNull(qr);
        assertEquals(400, qr.getWidth());
        assertEquals(400, qr.getHeight());
    }

    @Test
    @DisplayName("QR : Génération avec couleurs personnalisées et taille 300")
    void givenConfigWithCustomColors_whenGenerateQrCodeImage_thenReturn300x300Image()
            throws Exception {
        CommonFields config = new CommonFields(null, 300, 0.0, Color.RED, Color.YELLOW, false, 2);
        EncodedImage encodedImage = new EncodedImage();
        BufferedImage qr = encodedImage.generateImage("Test", config);
        assertNotNull(qr);
        assertEquals(300, qr.getWidth());
    }

    @Test
    @DisplayName("QR : Génération avec marge nulle")
    void givenConfigWithZeroMargin_whenGenerateQrCodeImage_thenReturnValidImage() throws Exception {
        CommonFields config = new CommonFields(null, 400, 0.1, Color.BLACK, Color.WHITE, false, 0);
        EncodedImage encodedImage = new EncodedImage();
        BufferedImage qr = encodedImage.generateImage("Test", config);
        assertNotNull(qr);
    }

    @Test
    @DisplayName("QR : Génération avec un ratio de logo élevé (0.5)")
    void givenConfigWithLargeLogoRatio_whenGenerateQrCodeImage_thenReturnValidImage()
            throws Exception {
        CommonFields config = new CommonFields(null, 500, 0.5, Color.BLACK, Color.WHITE, true, 4);
        EncodedImage encodedImage = new EncodedImage();
        BufferedImage qr = encodedImage.generateImage("Large logo test", config);
        assertNotNull(qr);
    }

    @Test
    @DisplayName("UI : Mise à jour du bouton après sélection de la couleur du QR (Rouge)")
    void givenRedColorSelected_whenChooseQrColor_thenButtonTextAndColorUpdated() {
        JButton button = new JButton();
        try (MockedStatic<JColorChooser> chooserMock = Mockito.mockStatic(JColorChooser.class)) {
            chooserMock
                    .when(() -> JColorChooser.showDialog(any(), anyString(), any()))
                    .thenReturn(Color.RED);
            Color chosen = colorOperation.chooseColor(button, Color.BLACK, true);
            assertEquals(Color.RED, chosen);
            assertEquals("#FF0000", button.getText());
        }
    }

    @Test
    @DisplayName("UI : Mise à jour du bouton après sélection de la couleur de fond (Bleu)")
    void givenBlueColorSelected_whenChooseBgColor_thenButtonTextAndColorUpdated() {
        JButton button = new JButton();
        try (MockedStatic<JColorChooser> chooserMock = Mockito.mockStatic(JColorChooser.class)) {
            chooserMock
                    .when(() -> JColorChooser.showDialog(any(), anyString(), any()))
                    .thenReturn(Color.BLUE);
            Color chosen = colorOperation.chooseColor(button, Color.WHITE, false);
            assertEquals(Color.BLUE, chosen);
            assertEquals("#0000FF", button.getText());
        }
    }

    @Test
    @DisplayName("UI : Annulation du sélecteur de couleur")
    void givenColorDialogCancelled_whenChooseColor_thenButtonTextUnchanged() {
        JButton button = new JButton("Noir");
        try (MockedStatic<JColorChooser> chooserMock = Mockito.mockStatic(JColorChooser.class)) {
            chooserMock
                    .when(() -> JColorChooser.showDialog(any(), anyString(), any()))
                    .thenReturn(null);
            Color chosen = colorOperation.chooseColor(button, Color.BLACK, true);
            assertNull(chosen);
            assertEquals("Noir", button.getText());
        }
    }

    @Test
    @DisplayName("UI : Mise à jour du champ logo après sélection d'un fichier")
    void givenFileSelected_whenBrowseLogo_thenLogoFieldUpdated() {
        CanScan spyGenerator = spy(generator);
        File fakeFile = new File("fake-logo.png");
        doReturn(fakeFile).when(spyGenerator).chooseLogoFile();
        ActionEvent e = mock(ActionEvent.class);
        spyGenerator.browseLogo(e);
        assertTrue(spyGenerator.getLogoFieldTextForTests().endsWith("fake-logo.png"));
    }

    @Test
    @DisplayName("UI : Champ logo inchangé si la sélection de fichier est annulée")
    void givenFileSelectionCancelled_whenBrowseLogo_thenLogoFieldUnchanged() {
        CanScan spyGenerator = spy(generator);
        doReturn(null).when(spyGenerator).chooseLogoFile();
        spyGenerator.setLogoFieldTextForTests("logoField");
        ActionEvent e = mock(ActionEvent.class);
        spyGenerator.browseLogo(e);
        assertEquals("logoField", spyGenerator.getLogoFieldTextForTests());
    }

    @ParameterizedTest(name = "Saisie : {0}, Attendu : {1}")
    @CsvSource({
        "500, 500", // valid
        "abc, 400", // invalid text
        "-50, 10", // negative
        "0, 10", // zero
        "5, 10" // below minimum
    })
    @DisplayName("VALIDATION : Vérification de la saisie du champ taille")
    void givenVariousSizeInputs_whenValidateAndGetSize_thenReturnExpectedResult(
            String input, int expected) {
        generator.setSizeFieldTextForTests(input);
        int result = generator.validateAndGetSize();
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("VALIDATION : Marge valide fixée à 4")
    void givenValidMargin4_whenMarginFieldCheck_thenGetMarginUpdatedTo4() {
        generator.setMarginSliderValueForTests(4);
        generator.validateAndGetMarginForTests();
        assertEquals(4, generator.getMarginFieldIntForTests());
    }

    @Test
    @DisplayName("VALIDATION : Marge négative corrigée à 0")
    void givenNegativeMargin_whenMarginFieldCheck_thenGetMarginSetTo0() {
        generator.setMarginSliderValueForTests(-2);
        generator.validateAndGetMarginForTests();
        assertEquals(0, generator.getMarginFieldIntForTests());
    }

    @Test
    @DisplayName("VALIDATION : Marge excessive corrigée à 10")
    void givenMarginAboveMaximum_whenMarginFieldCheck_thenGetMarginSetTo10() {
        generator.setMarginSliderValueForTests(15);
        generator.validateAndGetMarginForTests();
        assertEquals(10, generator.getMarginFieldIntForTests());
    }

    @ParameterizedTest(name = "Saisie slider : {0}%, Ratio attendu : {1}")
    @CsvSource({"0,0.0", "50,0.5", "100,1.0"})
    @DisplayName("VALIDATION : Calcul du ratio du logo")
    void givenRatioPercent_whenRatioFieldCheck_thenGetRatioSetCorrectly(
            int sliderValue, double expectedRatio) {
        generator.setRatioSliderValueForTests(sliderValue);
        generator.validateAndGetRatioForTests();
        double actualRatio = generator.validateAndGetRatioForTests();
        assertEquals(expectedRatio, actualRatio, 0.01);
    }

    @Test
    @DisplayName("MODE : Changement du mode MECARD vers FREE")
    void givenMecardMode_whenSwitchToFreeMode_thenCurrentModeIsFree() {
        generator.setCurrentModeForTests(Mode.MECARD);
        generator.switchModeForTests(Mode.FREE);
        assertEquals(Mode.FREE, generator.getCurrentModeForTests());
    }

    @Test
    @DisplayName("MODE : Changement du mode FREE vers MECARD")
    void givenFreeMode_whenSwitchToMecardMode_thenCurrentModeIsMecard() {
        generator.setCurrentModeForTests(Mode.FREE);
        generator.switchModeForTests(Mode.MECARD);
        assertEquals(Mode.MECARD, generator.getCurrentModeForTests());
    }

    @Test
    @DisplayName("DRAW : Dessin du motif de recherche carré (Finder Pattern)")
    void givenBlackWhiteImage_whenDrawSquareFinderPattern_thenPatternDrawnAtOrigin() {
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        EncodedImage encodedImage = new EncodedImage();
        encodedImage.drawSquareFinderPatternAtPixel(g, 0, 0, 21, Color.BLACK, Color.WHITE);
        int rgb = img.getRGB(0, 0);
        assertNotEquals(0, rgb);
    }

    @Test
    @DisplayName("DRAW : Vérification du dessin des trois motifs de recherche sur le QR")
    void givenQrImage_whenDrawFinderPatterns_thenAllThreePatternsDrawn() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        CommonFields configRounded =
                new CommonFields(null, 100, 0.27, Color.BLACK, Color.WHITE, true, 2);
        EncodedImage encodedImage = new EncodedImage();
        encodedImage.drawFinderPatterns(g, 21, configRounded);
        CommonFields configSquare =
                new CommonFields(null, 100, 0.27, Color.BLACK, Color.WHITE, false, 2);
        EncodedImage encodedImageNew = new EncodedImage();
        encodedImageNew.drawFinderPatterns(g, 21, configSquare);
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
    @DisplayName("UTIL : Conversion de la couleur Noire en Hexadécimal")
    void givenBlackColor_whenConvertToHex_thenReturn000000() {
        String hex = colorOperation.colorToHex(Color.BLACK);
        assertEquals("#000000", hex);
    }

    @Test
    @DisplayName("MODE : Libellé du mode MECARD")
    void givenMecardMode_whenGetModeText_thenReturnContact() {
        assertEquals("Contact", Mode.MECARD.text());
    }

    @Test
    @DisplayName("MODE : Libellé du mode FREE")
    void givenFreeMode_whenGetModeText_thenReturnSaisieLibre() {
        assertEquals("Saisie libre", Mode.FREE.text());
    }

    @Test
    @DisplayName("UTIL : Conversion de la couleur Blanche en Hexadécimal")
    void givenWhiteColor_whenConvertToHex_thenReturnFFFFFF() {
        String hex = colorOperation.colorToHex(Color.WHITE);
        assertEquals("#FFFFFF", hex);
    }

    @Test
    @DisplayName("UTIL : Conversion d'une couleur personnalisée en Hexadécimal")
    void givenCustomColor_whenConvertToHex_thenReturnCorrectHex() {
        Color custom = new Color(128, 64, 192);
        String hex = colorOperation.colorToHex(custom);
        assertEquals("#8040C0", hex);
    }

    @Test
    @DisplayName("DATA : Génération des données MECARD à partir de l'objet WholeFields")
    void givenMecardModeWithContactData_whenBuildQrData_thenReturnMecardString() {
        WholeFields input =
                new WholeFields(
                        () -> 300,
                        Mode.MECARD,
                        "",
                        "Alice",
                        "",
                        "123456",
                        "",
                        "",
                        "",
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
        EncodedData result = DataBuilderService.INSTANCE.buildData(Mode.MECARD, input);
        assertNotNull(result);
        String data = result.data();
        assertTrue(data.startsWith("MECARD:"));
        assertTrue(data.contains("N:Alice"));
        assertTrue(data.contains("TEL:123456"));
    }

    @Test
    @DisplayName("DATA : Génération des données en mode Saisie Libre")
    void givenFreeModeWithText_whenBuildQrData_thenReturnFreeText() {
        WholeFields input =
                new WholeFields(
                        () -> 300,
                        Mode.FREE,
                        "Test data",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
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
        EncodedData result = DataBuilderService.INSTANCE.buildData(Mode.FREE, input);
        assertNotNull(result);
        assertEquals("Test data", result.data());
    }

    @Test
    @DisplayName("DATA : Retourne une chaîne vide si les champs MECARD sont vides")
    void givenEmptyMecardFields_whenBuildQrData_thenReturnEmptyString() {
        WholeFields input =
                new WholeFields(
                        () -> 300,
                        Mode.FREE,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
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
        EncodedData result = DataBuilderService.INSTANCE.buildData(Mode.MECARD, input);
        assertNotNull(result);
        assertEquals("", result.data());
    }

    @Test
    @DisplayName("LOGO : Dessin effectif du logo sur l'image QR")
    void givenValidLogo_whenDrawLogoIfPresent_thenLogoDrawnOnImage() throws Exception {
        File logoFile = createTestLogoFile("test-logo.png");
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        CommonFields config =
                new CommonFields(logoFile, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        EncodedImage encodedImage = new EncodedImage();
        encodedImage.drawLogoIfPresent(g, config);
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
    @DisplayName("LOGO : Absence d'exception si Graphics2D est null")
    void givenNullGraphics_whenDrawLogoIfPresent_thenReturnWithoutException() throws Exception {
        File logoFile = createTestLogoFile("test-logo.png");
        CommonFields config =
                new CommonFields(logoFile, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        executeWithoutDialog(
                () ->
                        assertDoesNotThrow(
                                () -> {
                                    EncodedImage encodedImage = new EncodedImage();
                                    encodedImage.drawLogoIfPresent(null, config);
                                }));
    }

    @Test
    @DisplayName("LOGO : Absence d'exception si CommonFields est null")
    void givenNullConfig_whenDrawLogoIfPresent_thenReturnWithoutException() {
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        executeWithoutDialog(
                () ->
                        assertDoesNotThrow(
                                () -> {
                                    try {
                                        EncodedImage encodedImage = new EncodedImage();
                                        encodedImage.drawLogoIfPresent(g, null);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }));
        g.dispose();
    }

    @Test
    @DisplayName("LOGO : Absence d'exception si le fichier logo est null")
    void givenNullLogoFile_whenDrawLogoIfPresent_thenReturnWithoutException() {
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        CommonFields config = new CommonFields(null, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    EncodedImage encodedImage = new EncodedImage();
                    encodedImage.drawLogoIfPresent(g, config);
                });
        g.dispose();
    }

    @Test
    @DisplayName("LOGO : Absence d'exception si le fichier logo n'existe pas")
    void givenNonExistentLogoFile_whenDrawLogoIfPresent_thenReturnWithoutException() {
        File nonExistentFile = new File(tempDir, "non-existent-logo.png");
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        CommonFields config =
                new CommonFields(nonExistentFile, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    EncodedImage encodedImage = new EncodedImage();
                    encodedImage.drawLogoIfPresent(g, config);
                });
        g.dispose();
    }

    @Test
    @DisplayName("LOGO : Absence d'exception si le ratio du logo est à zéro")
    void givenZeroImageRatio_whenDrawLogoIfPresent_thenReturnWithoutException() throws Exception {
        File logoFile = createTestLogoFile("test-logo.png");
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        CommonFields config =
                new CommonFields(logoFile, 400, 0.0, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    EncodedImage encodedImage = new EncodedImage();
                    encodedImage.drawLogoIfPresent(g, config);
                });
        g.dispose();
    }

    @ParameterizedTest(name = "Ratio : {0}")
    @CsvSource({"0.1", "0.2", "0.27", "0.3", "0.4", "0.5"})
    @DisplayName("LOGO : Validation du redimensionnement selon différents ratios")
    void givenDifferentImageRatios_whenDrawLogoIfPresent_thenLogoScaledCorrectly(double ratio)
            throws Exception {
        File logoFile = createTestLogoFile("test-logo-ratio.png");
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        CommonFields config =
                new CommonFields(logoFile, 400, ratio, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    EncodedImage encodedImage = new EncodedImage();
                    encodedImage.drawLogoIfPresent(g, config);
                },
                "drawLogoIfPresent devrait fonctionner avec imageRatio = " + ratio);
        g.dispose();
    }

    @Test
    @DisplayName("LOGO : Support du format JPEG")
    void givenJPEGLogo_whenDrawLogoIfPresent_thenLogoDrawnSuccessfully() throws Exception {
        File logoFile = createTestLogoFile("test-logo.jpg");
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        CommonFields config =
                new CommonFields(logoFile, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    EncodedImage encodedImage = new EncodedImage();
                    encodedImage.drawLogoIfPresent(g, config);
                },
                "drawLogoIfPresent devrait fonctionner avec un fichier JPEG");
        g.dispose();
    }

    @Test
    @DisplayName("LOGO : Mise à l'échelle sur un QR Code de grande taille (1000px)")
    void givenLargeQRSize_whenDrawLogoIfPresent_thenLogoScaledProperly() throws Exception {
        File logoFile = createTestLogoFile("test-logo-large.png");
        int largeSize = 1000;
        BufferedImage qrImage = new BufferedImage(largeSize, largeSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        CommonFields config =
                new CommonFields(logoFile, largeSize, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    EncodedImage encodedImage = new EncodedImage();
                    encodedImage.drawLogoIfPresent(g, config);
                },
                "drawLogoIfPresent devrait gérer les grandes tailles de QR code");
        g.dispose();
    }

    @Test
    @DisplayName("LOGO : Mise à l'échelle sur un QR Code de petite taille (100px)")
    void givenSmallQRSize_whenDrawLogoIfPresent_thenLogoScaledProperly() throws Exception {
        File logoFile = createTestLogoFile("test-logo-small.png");
        int smallSize = 100;
        BufferedImage qrImage = new BufferedImage(smallSize, smallSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        CommonFields config =
                new CommonFields(logoFile, smallSize, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    EncodedImage encodedImage = new EncodedImage();
                    encodedImage.drawLogoIfPresent(g, config);
                },
                "drawLogoIfPresent devrait gérer les petites tailles de QR code");
        g.dispose();
    }

    @Test
    @DisplayName("LOGO : Support de la transparence (Alpha channel)")
    void givenLogoWithTransparency_whenDrawLogoIfPresent_thenLogoDrawnWithAlpha() throws Exception {
        File logoFile = createTestLogoFileWithAlpha();
        BufferedImage qrImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        CommonFields config =
                new CommonFields(logoFile, 400, 0.27, Color.BLACK, Color.WHITE, false, 3);
        assertDoesNotThrow(
                () -> {
                    EncodedImage encodedImage = new EncodedImage();
                    encodedImage.drawLogoIfPresent(g, config);
                },
                "drawLogoIfPresent devrait gérer les logos avec transparence");
        g.dispose();
    }

    @Test
    @DisplayName("LOGO : Centrage précis du logo sur le QR Code")
    void givenValidLogo_whenDrawLogoIfPresent_thenLogoCenteredCorrectly() throws Exception {
        File logoFile = createTestLogoFile("test-logo-centered.png");
        int size = 400;
        double imageRatio = 0.27;
        BufferedImage qrImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        CommonFields config =
                new CommonFields(logoFile, size, imageRatio, Color.BLACK, Color.WHITE, false, 3);
        EncodedImage encodedImage = new EncodedImage();
        encodedImage.drawLogoIfPresent(g, config);
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

    /** Exécute un test en simulant l'absence d'interaction utilisateur via JOptionPane. */
    private void executeWithoutDialog(Runnable test) {
        try (MockedStatic<JOptionPane> optionPaneMock = Mockito.mockStatic(JOptionPane.class)) {
            optionPaneMock
                    .when(
                            () ->
                                    JOptionPane.showMessageDialog(
                                            null, null, null, JOptionPane.INFORMATION_MESSAGE))
                    .thenAnswer(_ -> null);
            optionPaneMock
                    .when(
                            () ->
                                    JOptionPane.showMessageDialog(
                                            null, null, null, JOptionPane.WARNING_MESSAGE))
                    .thenAnswer(_ -> null);
            optionPaneMock
                    .when(
                            () ->
                                    JOptionPane.showMessageDialog(
                                            null, null, null, JOptionPane.ERROR_MESSAGE))
                    .thenAnswer(_ -> null);
            optionPaneMock
                    .when(
                            () ->
                                    JOptionPane.showMessageDialog(
                                            null, null, null, JOptionPane.QUESTION_MESSAGE))
                    .thenAnswer(_ -> null);
            optionPaneMock
                    .when(
                            () ->
                                    JOptionPane.showMessageDialog(
                                            null, null, null, JOptionPane.PLAIN_MESSAGE))
                    .thenAnswer(_ -> null);
            test.run();
        }
    }

    /** Crée un fichier image temporaire pour les besoins des tests. */
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

    /** Crée un fichier image avec transparence pour tester le canal Alpha. */
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

    /** Nettoyage après chaque test : fermeture de la fenêtre et libération des ressources. */
    @AfterEach
    @SuppressWarnings("MagicConstant")
    void tearDown() throws InterruptedException, InvocationTargetException {
        if (generator != null) {
            try (MockedStatic<JOptionPane> paneMock = Mockito.mockStatic(JOptionPane.class)) {
                paneMock.when(() -> JOptionPane.showMessageDialog(any(), any(), any(), anyInt()))
                        .thenAnswer(_ -> null);
                paneMock.when(() -> JOptionPane.showMessageDialog(any(), any()))
                        .thenAnswer(_ -> null);
                javax.swing.SwingUtilities.invokeAndWait(
                        () -> {
                            generator.setVisible(false);
                            generator.dispose();
                        });
            }
            generator = null;
        }
    }
}
