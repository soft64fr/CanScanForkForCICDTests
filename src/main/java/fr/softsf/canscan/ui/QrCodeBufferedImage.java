/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the MIT License (MIT).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import fr.softsf.canscan.model.ModuleContext;
import fr.softsf.canscan.model.QrConfig;
import fr.softsf.canscan.util.Checker;

/**
 * Thread-safe singleton to hold a shared QR code {@link BufferedImage}. All access is synchronized
 * for thread safety.
 */
public class QrCodeBufferedImage {

    private static final double DEFAULT_GAP_BETWEEN_LOGO_AND_MODULES = 0.9;
    private static final String CONFIG = "config";
    private static final String DRAW_SQUARE_FINDER_PATTERN_AT_PIXEL =
            "drawSquareFinderPatternAtPixel";
    private static final String BG_COLOR = "bgColor";
    private static final String DRAW_ROUNDED_FINDER_PATTERN_AT_PIXEL =
            "drawRoundedFinderPatternAtPixel";
    private static final String SHOULD_SKIP_MODULE = "shouldSkipModule";
    private static final String DRAW_MODULES = "drawModules";
    private static final String MATRIX = "matrix";
    private static final String GENERATE_QR_CODE_IMAGE = "generateQrCodeImage";
    private static final int AVAILABLE_MEMORY_TO_GENERATE_IMAGE = 50;
    private static final int BYTES_PER_KILOBYTE = 1024;
    private BufferedImage qrOriginal;
    private final Object imageLock = new Object();

    /**
     * @return the current QR code image, or null if not set.
     */
    public BufferedImage getQrOriginal() {
        synchronized (imageLock) {
            return qrOriginal;
        }
    }

    /**
     * Updates the QR code image. Should only be called by internal components.
     *
     * @param newImage the new image, can be null.
     */
    public void updateQrOriginal(BufferedImage newImage) {
        synchronized (imageLock) {
            this.qrOriginal = newImage;
        }
    }

    /**
     * Releases the current QR code image and its resources. Must be called on the EDT if Swing
     * components are involved.
     */
    public synchronized void freeQrOriginal() {
        synchronized (imageLock) {
            if (qrOriginal != null) {
                qrOriginal.flush();
                qrOriginal = null;
            }
        }
    }

    /**
     * Generates a QR code image with optional logo and custom styling.
     *
     * <p>The QR code is generated using the ZXing library with high error correction, allowing a
     * logo to be embedded without breaking scanability. Modules, colors, margin, and rounded or
     * square style are applied according to the provided configuration.
     *
     * @param data The formatted string to encode in the QR code.
     * @param config QR code configuration containing size, colors, margin, module style, and
     *     optional logo.
     * @return A BufferedImage containing the generated QR code.
     * @throws WriterException If encoding the MECARD text into a QR code fails.
     * @throws IOException If reading the logo file fails.
     * @throws OutOfMemoryError If the requested size exceeds available memory.
     */
    public BufferedImage generateQrCodeImage(String data, QrConfig config)
            throws WriterException, IOException {
        if (Checker.INSTANCE.checkNPE(config, GENERATE_QR_CODE_IMAGE, CONFIG)
                || Checker.INSTANCE.checkNPE(data, GENERATE_QR_CODE_IMAGE, "data")) {
            return null;
        }
        final int size = config.size();
        validateMemoryForImageSize(size);
        BitMatrix matrix = createMatrix(data, config.margin());
        if (Checker.INSTANCE.checkNPE(matrix, GENERATE_QR_CODE_IMAGE, MATRIX)) {
            return null;
        }
        BufferedImage qrImage = null;
        Graphics2D g = null;
        try {
            qrImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            g = qrImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            fillBackground(g, size, config.bgColor());
            drawModules(g, matrix, config);
            Objects.requireNonNull(matrix, "Dans generateQrCodeImage matrix ne doit pas être null");
            drawFinderPatterns(g, matrix.getWidth(), config);
            drawLogoIfPresent(g, config);
        } catch (OutOfMemoryError oom) {
            if (g != null) {
                g.dispose();
            }
            if (qrImage != null) {
                qrImage.flush();
            }
            throw new OutOfMemoryError(
                    String.format(
                            "Mémoire insuffisante pour générer une image de %dx%d pixels.%nTaille"
                                    + " estimée %d Mo.%nMémoire disponible %d Mo.",
                            size, size, estimateImageMemoryMB(size), getAvailableMemoryMB()));
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
        return qrImage;
    }

    /**
     * Validates that sufficient memory is available to generate a square image of the given size.
     * Applies a hard limit of 200M px and ensures a minimum memory margin before allocation.
     *
     * @param size the width and height of the square image in pixels
     * @throws OutOfMemoryError if the image exceeds pixel limits or available memory is
     *     insufficient
     */
    private void validateMemoryForImageSize(int size) {
        final long MAX_PIXELS = 200_000_000L;
        long totalPixels = (long) size * size;
        if (totalPixels > MAX_PIXELS) {
            throw new OutOfMemoryError(
                    String.format(
                            """

                            Dimension trop grande:
                            %dx%d pixels (%,d pixels).
                            Maximum autorisé: %,d pixels.
                            """,
                            size, size, totalPixels, MAX_PIXELS));
        }
        long estimatedMB = estimateImageMemoryMB(size);
        long availableMB = getAvailableMemoryMB();
        if (estimatedMB > availableMB) {
            throw new OutOfMemoryError(
                    String.format(
                            "Mémoire insuffisante pour générer une image de %dx%d pixels.%nMémoire"
                                    + " nécessaire %d Mo.%nMémoire disponible %d Mo.%nRéduire la"
                                    + " dimension souhaitée.",
                            size, size, estimatedMB, availableMB));
        }
    }

    /**
     * Estimates the memory required to create a BufferedImage.
     *
     * @param size The width and height of the square image.
     * @return Estimated memory in megabytes.
     */
    private long estimateImageMemoryMB(int size) {
        long totalPixels = (long) size * size;
        long bytesRequired = totalPixels * 4;
        return bytesRequired / (BYTES_PER_KILOBYTE * BYTES_PER_KILOBYTE);
    }

    /**
     * Gets the available memory in the JVM with safety margin for image generation.
     *
     * @return Available memory in megabytes, minus the reserved safety margin. Returns 0 if not
     *     enough memory is available.
     */
    private long getAvailableMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = allocatedMemory - freeMemory;
        long availableMemory = maxMemory - usedMemory;
        long safeAvailableMemory =
                availableMemory
                        - (AVAILABLE_MEMORY_TO_GENERATE_IMAGE
                                * BYTES_PER_KILOBYTE
                                * BYTES_PER_KILOBYTE);
        return Math.max(0, safeAvailableMemory) / (BYTES_PER_KILOBYTE * BYTES_PER_KILOBYTE);
    }

    /**
     * Creates a QR code matrix for the given text.
     *
     * @param text The string to encode in the QR code.
     * @param margin The outer margin of the QR code in modules.
     * @return A BitMatrix representing the encoded QR code.
     * @throws WriterException If encoding fails.
     */
    private BitMatrix createMatrix(String text, int margin) throws WriterException {
        if (Checker.INSTANCE.checkNPE(text, "createMatrix", "text")) {
            return null;
        }
        Map<EncodeHintType, Object> hints =
                Map.of(
                        EncodeHintType.CHARACTER_SET,
                        "UTF-8",
                        EncodeHintType.ERROR_CORRECTION,
                        ErrorCorrectionLevel.H,
                        EncodeHintType.MARGIN,
                        margin);
        return new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints);
    }

    /**
     * Fills the entire QR code area with the specified background color.
     *
     * @param g The graphics context used for drawing.
     * @param size The width and height of the QR code area in pixels.
     * @param bgColor The background color to fill.
     */
    private void fillBackground(Graphics2D g, int size, Color bgColor) {
        if (Checker.INSTANCE.checkNPE(g, "fillBackground", "g")
                || Checker.INSTANCE.checkNPE(bgColor, "fillBackground", BG_COLOR)) {
            return;
        }
        g.setColor(bgColor);
        g.fillRect(0, 0, size, size);
    }

    /**
     * Renders all QR code modules onto the provided graphics context.
     *
     * <p>Modules that are part of finder patterns or the central logo area are skipped. Supports
     * rounded or square modules according to configuration.
     *
     * @param g the graphics context used for drawing
     * @param matrix the QR code bit matrix representing module positions
     * @param config the QR code configuration including size, colors, module shape, margin, and
     *     logo ratio
     */
    private void drawModules(Graphics2D g, BitMatrix matrix, QrConfig config) {
        if (Checker.INSTANCE.checkNPE(g, DRAW_MODULES, "g")
                || Checker.INSTANCE.checkNPE(matrix, DRAW_MODULES, MATRIX)
                || Checker.INSTANCE.checkNPE(config, DRAW_MODULES, CONFIG)) {
            return;
        }
        int matrixWidth = matrix.getWidth();
        int matrixHeight = matrix.getHeight();
        double moduleSizeX = (double) config.size() / matrixWidth;
        double moduleSizeY = (double) config.size() / matrixHeight;
        int whiteBoxSize = (int) (config.size() * config.imageRatio());
        int whiteBoxX = (config.size() - whiteBoxSize) / 2;
        int whiteBoxY = (config.size() - whiteBoxSize) / 2;
        g.setColor(config.qrColor());
        ModuleContext ctx =
                new ModuleContext(
                        config, matrixWidth, matrixHeight, whiteBoxX, whiteBoxY, whiteBoxSize);
        for (int y = 0; y < matrixHeight; y++) {
            for (int x = 0; x < matrixWidth; x++) {
                if (shouldSkipModule(x, y, matrix, ctx)) {
                    continue;
                }
                drawModule(g, x, y, moduleSizeX, moduleSizeY, config);
            }
        }
    }

    /**
     * Determines whether a QR code module should be skipped during rendering.
     *
     * <p>A module is skipped if it is unset, part of a finder pattern, or overlaps the central logo
     * area.
     *
     * @param x the module's x-coordinate in the QR matrix
     * @param y the module's y-coordinate in the QR matrix
     * @param matrix the QR code bit matrix
     * @param ctx the module context containing configuration, matrix dimensions, and logo box
     * @return true if the module should be skipped, false otherwise
     */
    private boolean shouldSkipModule(int x, int y, BitMatrix matrix, ModuleContext ctx) {
        if (Checker.INSTANCE.checkNPE(matrix, SHOULD_SKIP_MODULE, MATRIX)
                || Checker.INSTANCE.checkNPE(ctx, SHOULD_SKIP_MODULE, "ctx")
                || Checker.INSTANCE.checkNPE(ctx.config(), SHOULD_SKIP_MODULE, "ctx.config")) {
            return true;
        }
        if (matrix.get(x, y)) {
            double scaleX = (double) ctx.config().size() / ctx.matrixWidth();
            double scaleY = (double) ctx.config().size() / ctx.matrixHeight();
            double cx = x * scaleX;
            double cy = y * scaleY;
            if (isInPositionPattern(
                    x, y, ctx.matrixWidth(), ctx.matrixHeight(), ctx.config().margin())) {
                return true;
            }
            return cx + scaleX > ctx.whiteBoxX()
                    && cx < ctx.whiteBoxX() + ctx.whiteBoxSize()
                    && cy + scaleY > ctx.whiteBoxY()
                    && cy < ctx.whiteBoxY() + ctx.whiteBoxSize();
        }
        return true;
    }

    /**
     * Draws a single QR code module at the specified coordinates with configured size and shape.
     *
     * <p>Supports square or rounded modules depending on configuration.
     *
     * @param g the graphics context used for rendering
     * @param x the module's x-coordinate in the matrix
     * @param y the module's y-coordinate in the matrix
     * @param moduleSizeX width of the module in pixels
     * @param moduleSizeY height of the module in pixels
     * @param config the QR code configuration
     */
    private void drawModule(
            Graphics2D g, int x, int y, double moduleSizeX, double moduleSizeY, QrConfig config) {
        if (Checker.INSTANCE.checkNPE(g, "drawModule", "g")
                || Checker.INSTANCE.checkNPE(config, "drawModule", CONFIG)) {
            return;
        }
        double cx = x * moduleSizeX;
        double cy = y * moduleSizeY;
        if (config.roundedModules()) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.fill(new Ellipse2D.Double(cx, cy, moduleSizeX, moduleSizeY));
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.fillRect(
                    (int) cx, (int) cy, (int) Math.ceil(moduleSizeX), (int) Math.ceil(moduleSizeY));
        }
    }

    /**
     * Determines whether the given coordinates fall within any of the three QR code position
     * patterns.
     *
     * @param x X-coordinate in the QR matrix.
     * @param y Y-coordinate in the QR matrix.
     * @param matrixWidth Width of the QR matrix.
     * @param matrixHeight Height of the QR matrix.
     * @param margin QR code margin in modules.
     * @return {@code true} if the coordinate is inside a position pattern; {@code false} otherwise.
     */
    private boolean isInPositionPattern(
            int x, int y, int matrixWidth, int matrixHeight, int margin) {
        return isInTopLeftPattern(x, y, margin)
                || isInTopRightPattern(x, y, matrixWidth, margin)
                || isInBottomLeftPattern(x, y, matrixHeight, margin);
    }

    /**
     * Checks if coordinates are within the top-left position pattern.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param margin QR code margin
     * @return {@code true} if inside top-left pattern; {@code false} otherwise
     */
    private boolean isInTopLeftPattern(int x, int y, int margin) {
        return x >= margin && x < margin + 7 && y >= margin && y < margin + 7;
    }

    /**
     * Checks if coordinates are within the top-right position pattern.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param matrixWidth Width of the QR matrix
     * @param margin QR code margin
     * @return {@code true} if inside top-right pattern; {@code false} otherwise
     */
    private boolean isInTopRightPattern(int x, int y, int matrixWidth, int margin) {
        return x >= matrixWidth - margin - 7
                && x < matrixWidth - margin
                && y >= margin
                && y < margin + 7;
    }

    /**
     * Checks if coordinates are within the bottom-left position pattern.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param matrixHeight Height of the QR matrix
     * @param margin QR code margin
     * @return {@code true} if inside bottom-left pattern; {@code false} otherwise
     */
    private boolean isInBottomLeftPattern(int x, int y, int matrixHeight, int margin) {
        return x >= margin
                && x < margin + 7
                && y >= matrixHeight - margin - 7
                && y < matrixHeight - margin;
    }

    /**
     * Renders the three QR code finder patterns at the corners using the specified style.
     *
     * @param g The graphics context for rendering.
     * @param matrixWidth Width of the QR code matrix.
     * @param config Configuration containing size, colors, module shape, and margin.
     */
    public void drawFinderPatterns(Graphics2D g, int matrixWidth, QrConfig config) {
        if (Checker.INSTANCE.checkNPE(g, "drawFinderPatterns", "g")
                || Checker.INSTANCE.checkNPE(config, "drawFinderPatterns", CONFIG)) {
            return;
        }
        double moduleSizeX = (double) config.size() / matrixWidth;
        double marginPixels = config.margin() * moduleSizeX;
        double diameter = 7 * moduleSizeX;
        if (config.roundedModules()) {
            drawRoundedFinderPatternAtPixel(
                    g, marginPixels, marginPixels, diameter, config.qrColor(), config.bgColor());
            drawRoundedFinderPatternAtPixel(
                    g,
                    marginPixels,
                    config.size() - marginPixels - diameter,
                    diameter,
                    config.qrColor(),
                    config.bgColor());
            drawRoundedFinderPatternAtPixel(
                    g,
                    config.size() - marginPixels - diameter,
                    marginPixels,
                    diameter,
                    config.qrColor(),
                    config.bgColor());
        } else {
            drawSquareFinderPatternAtPixel(
                    g, marginPixels, marginPixels, diameter, config.qrColor(), config.bgColor());
            drawSquareFinderPatternAtPixel(
                    g,
                    marginPixels,
                    config.size() - marginPixels - diameter,
                    diameter,
                    config.qrColor(),
                    config.bgColor());
            drawSquareFinderPatternAtPixel(
                    g,
                    config.size() - marginPixels - diameter,
                    marginPixels,
                    diameter,
                    config.qrColor(),
                    config.bgColor());
        }
    }

    /**
     * Draws the logo at the center of the QR code if a valid logo file is provided.
     *
     * <p>The logo is scaled to fit within 90% of the designated white box area, which is determined
     * by the QR code size and configured image ratio.
     *
     * @param g The Graphics2D context used for rendering the QR code.
     * @param config QR code configuration containing size, logo file, and image ratio.
     * @throws IOException If reading the logo file fails or the file is not a valid image.
     */
    public void drawLogoIfPresent(Graphics2D g, QrConfig config) throws IOException {
        if (Checker.INSTANCE.checkNPE(g, "drawLogoIfPresent", "g")
                || Checker.INSTANCE.checkNPE(config, "drawLogoIfPresent", CONFIG)
                || config.logoFile() == null
                || !config.logoFile().exists()
                || config.imageRatio() == 0) {
            return;
        }
        final int size = config.size();
        final int whiteBoxSize = (int) (size * config.imageRatio());
        final int whiteBoxX = (size - whiteBoxSize) / 2;
        final int whiteBoxY = (size - whiteBoxSize) / 2;
        final int logoMaxSize = (int) (whiteBoxSize * DEFAULT_GAP_BETWEEN_LOGO_AND_MODULES);
        final int logoX = whiteBoxX + (whiteBoxSize - logoMaxSize) / 2;
        final int logoY = whiteBoxY + (whiteBoxSize - logoMaxSize) / 2;
        BufferedImage logo = null;
        BufferedImage scaledLogo = null;
        Graphics2D gLogo = null;
        try (InputStream in = new FileInputStream(config.logoFile())) {
            logo = ImageIO.read(in);
            if (logo == null) {
                Popup.INSTANCE.showDialog(
                        "",
                        "Ce format de logo n'est pas pris en charge (seulement PNG, JPG, ou JPEG).",
                        "Information");
                return;
            }
            scaledLogo = new BufferedImage(logoMaxSize, logoMaxSize, BufferedImage.TYPE_INT_ARGB);
            gLogo = scaledLogo.createGraphics();
            gLogo.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gLogo.drawImage(logo, 0, 0, logoMaxSize, logoMaxSize, null);
        } finally {
            if (gLogo != null) {
                gLogo.dispose();
            }
            if (logo != null) {
                logo.flush();
            }
            if (scaledLogo != null) {
                g.drawImage(scaledLogo, logoX, logoY, null);
                scaledLogo.flush();
            }
        }
    }

    /**
     * Draws a QR code finder pattern with rounded corners at the specified pixel coordinates.
     *
     * @param g Graphics2D context to draw on.
     * @param x X-coordinate of the top-left corner.
     * @param y Y-coordinate of the top-left corner.
     * @param diameter Diameter of the finder pattern.
     * @param qrColor Color of the QR modules.
     * @param bgColor Background color inside the pattern.
     */
    private void drawRoundedFinderPatternAtPixel(
            Graphics2D g, double x, double y, double diameter, Color qrColor, Color bgColor) {
        if (Checker.INSTANCE.checkNPE(g, DRAW_ROUNDED_FINDER_PATTERN_AT_PIXEL, "g")
                || Checker.INSTANCE.checkNPE(
                        qrColor, DRAW_ROUNDED_FINDER_PATTERN_AT_PIXEL, "qrColor")
                || Checker.INSTANCE.checkNPE(
                        bgColor, DRAW_ROUNDED_FINDER_PATTERN_AT_PIXEL, BG_COLOR)) {
            return;
        }
        double arc = diameter / 4.0;
        g.setColor(qrColor);
        g.fill(new RoundRectangle2D.Double(x, y, diameter, diameter, arc, arc));
        double innerMargin = diameter / 7.0;
        g.setColor(bgColor);
        g.fill(
                new RoundRectangle2D.Double(
                        x + innerMargin,
                        y + innerMargin,
                        diameter - 2 * innerMargin,
                        diameter - 2 * innerMargin,
                        arc,
                        arc));
        double centerMargin = diameter / 7.0 * 2;
        g.setColor(qrColor);
        g.fill(
                new RoundRectangle2D.Double(
                        x + centerMargin,
                        y + centerMargin,
                        diameter - 2 * centerMargin,
                        diameter - 2 * centerMargin,
                        arc,
                        arc));
    }

    /**
     * Draws a standard square QR code finder pattern at the specified pixel coordinates.
     *
     * @param g Graphics2D context to draw on.
     * @param x X-coordinate of the top-left corner.
     * @param y Y-coordinate of the top-left corner.
     * @param diameter Diameter of the finder pattern.
     * @param qrColor Color of the QR modules.
     * @param bgColor Background color inside the pattern.
     */
    public void drawSquareFinderPatternAtPixel(
            Graphics2D g, double x, double y, double diameter, Color qrColor, Color bgColor) {
        if (Checker.INSTANCE.checkNPE(g, DRAW_SQUARE_FINDER_PATTERN_AT_PIXEL, "g")
                || Checker.INSTANCE.checkNPE(
                        qrColor, DRAW_SQUARE_FINDER_PATTERN_AT_PIXEL, "qrColor")
                || Checker.INSTANCE.checkNPE(
                        bgColor, DRAW_SQUARE_FINDER_PATTERN_AT_PIXEL, BG_COLOR)) {
            return;
        }
        g.setColor(qrColor);
        g.fillRect((int) x, (int) y, (int) diameter, (int) diameter);
        double innerMargin = diameter / 7.0;
        g.setColor(bgColor);
        g.fillRect(
                (int) (x + innerMargin),
                (int) (y + innerMargin),
                (int) (diameter - 2 * innerMargin),
                (int) (diameter - 2 * innerMargin));
        double centerMargin = diameter / 7.0 * 2;
        g.setColor(qrColor);
        g.fillRect(
                (int) (x + centerMargin),
                (int) (y + centerMargin),
                (int) (diameter - 2 * centerMargin),
                (int) (diameter - 2 * centerMargin));
    }
}
