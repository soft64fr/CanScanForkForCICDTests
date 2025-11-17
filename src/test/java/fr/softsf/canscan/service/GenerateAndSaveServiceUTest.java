/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.service;

import java.io.File;
import javax.swing.JFileChooser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import fr.softsf.canscan.ui.EncodedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("*** Generate and save service tests ***")
class GenerateAndSaveServiceUTest {

    private GenerateAndSaveService qrService;

    @TempDir File tempDir;

    @BeforeEach
    void setUp() {
        EncodedImage encodedImage = mock(EncodedImage.class);
        qrService = new GenerateAndSaveService(encodedImage);
    }

    @Test
    void givenFileWithPngExtension_whenGetSelectedPngFile_thenReturnSameFile() {
        JFileChooser chooser = mock(JFileChooser.class);
        File testFile = new File(tempDir, "test.png");
        when(chooser.getSelectedFile()).thenReturn(testFile);
        File result = qrService.getSelectedFileForTests(chooser);
        assertTrue(result.getName().endsWith(".png"));
    }

    @Test
    void givenFileWithoutPngExtension_whenGetSelectedPngFile_thenReturnFileWithPngExtension() {
        JFileChooser chooser = mock(JFileChooser.class);
        File testFile = new File(tempDir, "test");
        when(chooser.getSelectedFile()).thenReturn(testFile);
        File result = qrService.getSelectedFileForTests(chooser);
        assertTrue(result.getName().endsWith(".png"));
    }

    @Test
    void givenNonExistingFile_whenResolveFileNameConflict_thenReturnSameFile() {
        File testFile = new File(tempDir, "nonexistent.png");
        File result = qrService.resolveFileNameConflictForTests(testFile);
        assertEquals(testFile, result);
    }
}
