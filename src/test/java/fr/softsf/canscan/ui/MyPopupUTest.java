/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

@DisplayName("*** MyPopup tests ***")
class MyPopupUTest {

    @Nested
    @DisplayName("showArgumentErrorDialog tests")
    class ShowArgumentErrorDialogTests {

        @Test
        @SuppressWarnings("unused")
        @DisplayName("givenValidParameters_whenShowArgumentErrorDialog_thenDoesNotThrowException")
        void givenValidParameters_whenShowArgumentErrorDialog_thenDoesNotThrowException() {
            try (MockedStatic<JOptionPane> mocked = mockStatic(JOptionPane.class)) {
                assertDoesNotThrow(
                        () ->
                                MyPopup.INSTANCE.showArgumentErrorDialog(
                                        null, "testMethod", "paramName", "null"));
            }
        }

        @Test
        @SuppressWarnings("unused")
        @DisplayName("givenNullParameters_whenShowArgumentErrorDialog_thenDoesNotThrowException")
        void givenNullParameters_whenShowArgumentErrorDialog_thenDoesNotThrowException() {
            try (MockedStatic<JOptionPane> mocked = mockStatic(JOptionPane.class)) {
                assertDoesNotThrow(
                        () -> MyPopup.INSTANCE.showArgumentErrorDialog(null, null, null, null));
            }
        }
    }

    @Nested
    @DisplayName("showDialog tests")
    class ShowDialogTests {

        @Test
        @SuppressWarnings("unused")
        @DisplayName("givenValidArguments_whenShowDialog_thenDoesNotThrowException")
        void givenValidArguments_whenShowDialog_thenDoesNotThrowException() {
            try (MockedStatic<JOptionPane> mocked = mockStatic(JOptionPane.class)) {
                assertDoesNotThrow(
                        () ->
                                MyPopup.INSTANCE.showDialog(
                                        "⚠️ Test:\n", "Message de test", "Titre"));
            }
        }

        @Test
        @SuppressWarnings("unused")
        @DisplayName("givenNullAndBlankValues_whenShowDialog_thenDoesNotThrowException")
        void givenNullAndBlankValues_whenShowDialog_thenDoesNotThrowException() {
            try (MockedStatic<JOptionPane> mocked = mockStatic(JOptionPane.class)) {
                assertDoesNotThrow(() -> MyPopup.INSTANCE.showDialog(null, " ", null));
            }
        }

        @Test
        @SuppressWarnings("unused")
        @DisplayName("givenUnicodeCharacters_whenShowDialog_thenDoesNotThrowException")
        void givenUnicodeCharacters_whenShowDialog_thenDoesNotThrowException() {
            try (MockedStatic<JOptionPane> mocked = mockStatic(JOptionPane.class)) {
                String unicodeMessage = "Test message — 你好 🌍 🚀";
                assertDoesNotThrow(
                        () ->
                                MyPopup.INSTANCE.showDialog(
                                        "🧪 Prefix:\n", unicodeMessage, "Essai Unicode"));
            }
        }
    }

    @Nested
    @DisplayName("showYesNoConfirmDialog tests")
    class ShowYesNoConfirmDialogTests {

        @Test
        @SuppressWarnings("unused")
        @DisplayName("givenValidMessage_whenShowYesNoConfirmDialog_thenReturnsValidOption")
        void givenValidMessage_whenShowYesNoConfirmDialog_thenReturnsValidOption() {
            try (MockedStatic<JOptionPane> mocked = mockStatic(JOptionPane.class)) {
                mocked.when(
                                () ->
                                        JOptionPane.showConfirmDialog(
                                                null,
                                                "Confirmer ?",
                                                "Soft64.fr",
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.WARNING_MESSAGE))
                        .thenReturn(JOptionPane.YES_OPTION);

                int result = MyPopup.INSTANCE.showYesNoConfirmDialog("Confirmer ?");
                assertTrue(result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION);
            }
        }
    }

    @Nested
    @DisplayName("Singleton behavior tests")
    class SingletonTests {

        @Test
        @DisplayName("givenPopupEnum_whenAccessInstance_thenInstanceIsNotNullAndUnique")
        void givenPopupEnum_whenAccessInstance_thenInstanceIsNotNullAndUnique() {
            MyPopup instance1 = MyPopup.INSTANCE;
            MyPopup instance2 = MyPopup.valueOf("INSTANCE");
            assertSame(instance1, instance2, "Les instances doivent être identiques");
        }
    }
}
