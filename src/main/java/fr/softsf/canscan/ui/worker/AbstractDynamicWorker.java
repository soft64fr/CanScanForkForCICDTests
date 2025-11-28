/*
 * CanScan - Copyright © 2025-present SOFT64.FR Lob2018
 * Licensed under the GNU General Public License v3.0 (GPLv3.0).
 * See the full license at: https://github.com/Lob2018/CanScan?tab=License-1-ov-file#readme
 */
package fr.softsf.canscan.ui.worker;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import fr.softsf.canscan.model.WholeFields;
import fr.softsf.canscan.ui.MyPopup;
import fr.softsf.canscan.util.Checker;

/**
 * Base class managing asynchronous QR code tasks with a unified workflow.
 *
 * <p>Handles debounce timers, worker cancellation, resource cleanup, and error reporting. Provides
 * lifecycle management to safely release resources during shutdown or reset.
 *
 * @param <T> type of result produced by the SwingWorker (e.g., ImageIcon, BufferedImage)
 */
public abstract class AbstractDynamicWorker<T> {

    protected Timer debounceTimer;
    protected SwingWorker<T, Void> worker;
    protected WholeFields wholeFields;
    protected final JProgressBar loader;

    /**
     * Initializes the QR code worker with a required loader.
     *
     * @param loader {@link JProgressBar} for progress indication; must not be {@code null}
     */
    protected AbstractDynamicWorker(JProgressBar loader) {
        Checker.INSTANCE.checkNPE(loader, "AbstractDynamicWorker", "loader");
        this.loader = loader;
    }

    /** Stops and clears the debounce timer. */
    protected void stopDebounceTimer() {
        if (debounceTimer != null) {
            debounceTimer.stop();
            debounceTimer = null;
        }
    }

    /** Cancels the current SwingWorker and clears its reference safely. */
    protected void cancelWorker() {
        if (worker == null || worker.isDone()) {
            return;
        }
        worker.cancel(true);
        try {
            worker.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | CancellationException ignored) {
            // Expected on cancellation or execution failure
        } finally {
            worker = null;
        }
    }

    /** Clears any allocated data, icons, or buffers before starting a new task. */
    protected abstract void clearResources();

    /** Creates and returns a new SwingWorker for the current operation. */
    protected abstract SwingWorker<T, Void> createWorker();

    /** Called when the worker completes successfully with its result. */
    protected abstract void onWorkerSuccess(T result);

    /**
     * Handles the completion of the current {@link SwingWorker} task.
     *
     * <p>Stops the loader indicator, verifies cancellation status, and invokes the appropriate
     * success or failure callback. Intended to be called from the worker's {@code done()} method to
     * centralize post-execution logic and avoid duplication in subclasses.
     */
    protected void handleWorkerDone() {
        stopLoader();
        try {
            if (worker == null || worker.isCancelled()) {
                return;
            }
            onWorkerSuccess(worker.get());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            onWorkerFailure(ie);
        } catch (ExecutionException | CancellationException ex) {
            onWorkerFailure(ex);
        }
    }

    /** Called when the worker fails or is cancelled. */
    protected void onWorkerFailure(Exception ex) {
        resetWorker();
        MyPopup.INSTANCE.showDialog("Erreur d’exécution", ex.getMessage(), "Erreur");
    }

    /**
     * Executes the unified workflow: cancel previous worker, stop debounce, clear resources, and
     * start a new worker after the specified delay.
     *
     * @param delayMs debounce delay in milliseconds before starting the worker
     */
    protected void resetAndStartWorker(int delayMs) {
        resetWorker();
        debounceTimer =
                new Timer(
                        delayMs,
                        e -> {
                            loader.setVisible(true);
                            worker = createWorker();
                            worker.execute();
                        });
        debounceTimer.setRepeats(false);
        debounceTimer.start();
    }

    /**
     * Resets the current worker and associated resources.
     *
     * <p>Stops the debounce timer, cancels any running {@link SwingWorker}, clears allocated
     * resources, and stops the {@link JProgressBar} indicator. Prepares the worker for a new task
     * or handles failure cleanup.
     */
    private void resetWorker() {
        stopLoader();
        stopDebounceTimer();
        cancelWorker();
        clearResources();
    }

    /**
     * Releases all resources used by this worker.
     *
     * <p>Stops timers, cancels workers, and clears data to ensure a clean shutdown or module reset.
     */
    public void disposeAllResourcesOnExit() {
        resetWorker();
        wholeFields = null;
    }

    /** Stops the loader's wait indicator. */
    protected void stopLoader() {
        loader.setVisible(false);
    }
}
