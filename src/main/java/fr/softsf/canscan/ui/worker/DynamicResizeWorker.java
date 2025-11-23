/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui.worker;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import fr.softsf.canscan.constant.IntConstants;
import fr.softsf.canscan.model.WholeFields;
import fr.softsf.canscan.ui.EncodedImage;
import fr.softsf.canscan.ui.LabelIconUtil;
import fr.softsf.canscan.util.Checker;

/**
 * Asynchronously resizes a generated QR code image for display in a Swing UI.
 *
 * <p>Resize operations are executed off the Event Dispatch Thread (EDT) to maintain UI
 * responsiveness. This class uses a debounce mechanism and background {@link SwingWorker} to avoid
 * unnecessary scaling when multiple layout or configuration changes occur rapidly.
 *
 * <p>Each instance manages resizing for a specific {@link JLabel}, working with a {@link
 * EncodedImage} as the source image. The optional {@link JProgressBar} can show a wait/progress
 * indicator while resizing is in progress.
 *
 * <p>Resources are properly managed: previous icons are disposed, background workers are cancelled,
 * and the loader is stopped to prevent memory leaks and ensure smooth UI updates.
 */
public class DynamicResizeWorker extends AbstractDynamicWorker<ImageIcon> {

    private static final int RESIZE_DEBOUNCE_DELAY_MS = 200;
    private static final int DEFAULT_SIZE = 50;

    private final EncodedImage encodedImage;
    private final JLabel qrCodeLabel;

    /**
     * Constructs a new QR code resize manager for a specific label.
     *
     * @param encodedImage the source QR code image; must not be {@code null}
     * @param qrCodeLabel the label where the resized QR code will be displayed; must not be {@code
     *     null}
     * @param loader loader to show a wait/progress indicator
     */
    public DynamicResizeWorker(EncodedImage encodedImage, JLabel qrCodeLabel, JProgressBar loader) {
        super(loader);
        this.encodedImage = encodedImage;
        this.qrCodeLabel = qrCodeLabel;
    }

    /**
     * Updates the current QR input, resize the loader, and schedules a debounced resize operation.
     *
     * <p>Any ongoing resize worker is cancelled and a new one is started after a short delay.
     *
     * @param wholeFields the latest QR code configuration
     */
    public void updateQrCodeResize(WholeFields wholeFields) {
        Checker.INSTANCE.checkNPE(wholeFields, "updateQrCodeResize", "wholeFields");
        this.wholeFields = wholeFields;
        updateLoaderSize();
        resetAndStartWorker(RESIZE_DEBOUNCE_DELAY_MS);
    }

    /**
     * Clears the current icon before starting a new resize task. Invoked automatically by the
     * {@link AbstractDynamicWorker} workflow.
     */
    @Override
    protected void clearResources() {
        LabelIconUtil.INSTANCE.disposeIcon(qrCodeLabel);
        qrCodeLabel.setIcon(null);
    }

    /**
     * Creates a background {@link SwingWorker} that scales the QR code image to the target size.
     *
     * <p>Uses bilinear interpolation for smooth resizing. Cancels promptly if the thread is
     * interrupted.
     *
     * @return a configured {@link SwingWorker} that produces an {@link ImageIcon} for display
     */
    @Override
    protected SwingWorker<ImageIcon, Void> createWorker() {
        int size = Math.max(wholeFields.availableHeightForQrCode(), DEFAULT_SIZE);
        return new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                BufferedImage src = encodedImage.getQrOriginal();
                if (src == null) {
                    return null;
                }
                BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = scaled.createGraphics();
                try {
                    g.setRenderingHint(
                            RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(src, 0, 0, size, size, null);
                } finally {
                    g.dispose();
                }
                return new ImageIcon(scaled);
            }

            @Override
            protected void done() {
                handleWorkerDone();
            }
        };
    }

    /**
     * Updates the QR code label with the generated icon and adjusts the loader size to maintain
     * visual alignment once the worker completes successfully.
     *
     * @param result the resized {@link ImageIcon}, or {@code null} if the task was cancelled
     */
    @Override
    protected void onWorkerSuccess(ImageIcon result) {
        if (result == null) {
            return;
        }
        qrCodeLabel.setIcon(result);
        updateLoaderSize();
    }

    /**
     * Resizes the loader to a square dimension based on the given value and refreshes its parent
     * container to ensure proper layout and rendering.
     */
    private void updateLoaderSize() {
        int size =
                qrCodeLabel.getPreferredSize().height + IntConstants.LOADER_SIZE_OFFSET.getValue();
        if (size == IntConstants.LOADER_SIZE_OFFSET.getValue()) {
            size = Math.max(wholeFields.availableHeightForQrCode(), DEFAULT_SIZE);
        }
        Dimension dim = new Dimension(size, size);
        loader.setPreferredSize(dim);
        loader.setMaximumSize(dim);
        Container parent = loader.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }
}
