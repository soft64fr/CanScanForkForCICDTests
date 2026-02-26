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

/** Suite de tests unitaires pour la gestion des fenêtres contextuelles via MyPopup. */
@DisplayName("*** MyPopup tests ***")
class MyPopupUTest {

    /** Tests relatifs à l'affichage des erreurs d'arguments. */
    @Nested
    @DisplayName("showArgumentErrorDialog tests")
    class ShowArgumentErrorDialogTests {

        /** Vérifie que l'appel avec des paramètres valides ne lève aucune exception. */
        @Test
        @SuppressWarnings("unused")
        @DisplayName("UI : Affichage erreur argument avec paramètres valides")
        void givenValidParameters_whenShowArgumentErrorDialog_thenDoesNotThrowException() {
            try (MockedStatic<JOptionPane> mocked = mockStatic(JOptionPane.class)) {
                assertDoesNotThrow(
                        () ->
                                MyPopup.INSTANCE.showArgumentErrorDialog(
                                        null, "testMethod", "paramName", "null"));
            }
        }

        /** Vérifie la robustesse de la méthode face à des paramètres nuls. */
        @Test
        @SuppressWarnings("unused")
        @DisplayName("UI : Affichage erreur argument avec paramètres nuls")
        void givenNullParameters_whenShowArgumentErrorDialog_thenDoesNotThrowException() {
            try (MockedStatic<JOptionPane> mocked = mockStatic(JOptionPane.class)) {
                assertDoesNotThrow(
                        () -> MyPopup.INSTANCE.showArgumentErrorDialog(null, null, null, null));
            }
        }
    }

    /** Tests relatifs à l'affichage des boîtes de dialogue standards. */
    @Nested
    @DisplayName("showDialog tests")
    class ShowDialogTests {

        /** Vérifie l'affichage d'un dialogue informatif classique. */
        @Test
        @SuppressWarnings("unused")
        @DisplayName("UI : Affichage dialogue avec arguments valides")
        void givenValidArguments_whenShowDialog_thenDoesNotThrowException() {
            try (MockedStatic<JOptionPane> mocked = mockStatic(JOptionPane.class)) {
                assertDoesNotThrow(
                        () ->
                                MyPopup.INSTANCE.showDialog(
                                        "⚠️ Test:\n", "Message de test", "Titre"));
            }
        }

        /** Vérifie la gestion des valeurs vides ou nulles dans le dialogue. */
        @Test
        @SuppressWarnings("unused")
        @DisplayName("UI : Affichage dialogue avec valeurs nulles ou vides")
        void givenNullAndBlankValues_whenShowDialog_thenDoesNotThrowException() {
            try (MockedStatic<JOptionPane> mocked = mockStatic(JOptionPane.class)) {
                assertDoesNotThrow(() -> MyPopup.INSTANCE.showDialog(null, " ", null));
            }
        }

        /** Vérifie le support des caractères Unicode complexes (Emoji, Idéogrammes). */
        @Test
        @SuppressWarnings("unused")
        @DisplayName("UI : Affichage dialogue avec caractères Unicode")
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

    /** Tests relatifs aux dialogues de confirmation Oui/Non. */
    @Nested
    @DisplayName("showYesNoConfirmDialog tests")
    class ShowYesNoConfirmDialogTests {

        /**
         * Vérifie que la réponse du dialogue correspond aux constantes attendues de JOptionPane.
         */
        @Test
        @SuppressWarnings("unused")
        @DisplayName("UI : Retour d'option valide pour dialogue Oui/Non")
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

    /** Tests vérifiant l'implémentation du pattern Singleton. */
    @Nested
    @DisplayName("Singleton behavior tests")
    class SingletonTests {

        /** S'assure que l'instance enum est unique et accessible. */
        @Test
        @DisplayName("PATTERN : Unicité de l'instance Singleton MyPopup")
        void givenPopupEnum_whenAccessInstance_thenInstanceIsNotNullAndUnique() {
            MyPopup instance1 = MyPopup.INSTANCE;
            MyPopup instance2 = MyPopup.valueOf("INSTANCE");
            assertSame(instance1, instance2, "Les instances doivent être identiques");
        }
    }
}
