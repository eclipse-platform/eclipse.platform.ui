/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.ui.tests.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.FinishedJobs;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.eclipse.ui.progress.IProgressService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProgressServiceTest extends ProgressTestCase {

	/** Upper bound for waiting until an expected progress dialog is shown. */
	private static final long DIALOG_TIMEOUT_MILLIS = 10_000;

	private IProgressService progressService;

	@Override
	@Before
	public void doSetUp() throws Exception {
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		progressService = PlatformUI.getWorkbench().getProgressService();
		FinishedJobs.getInstance().clearAll();
	}

	@Override
	@After
	public void doTearDown() throws Exception {
		FinishedJobs.getInstance().clearAll();
		WorkbenchPlugin.getDefault().getPreferenceStore().setToDefault(IPreferenceConstants.RUN_IN_BACKGROUND);
		super.doTearDown();
	}

	/**
	 * See
	 * {@link #testRun_noFork_cancelable_runInForeground_callFromUIThread_blockingUiThread_noDialogShown()}
	 * for why a progress dialog is expected to never appear when {@code fork ==
	 * false} and the runnable simply blocks the UI thread without pumping
	 * events, and for the caveat about why this alone does not fully prove the
	 * 800ms delay is honored.
	 */
	@Test
	public void testRun_noFork_notCancelable_runInForeground_callFromUIThread_blockingUiThread_noDialogShown()
			throws Exception {
		assertDialogShown(false, false, false, false, false);
	}

	/**
	 * See
	 * {@link #testRun_noFork_cancelable_runInForeground_callFromUIThread_pumpingUiEventsWhileRunning_dialogShown()}
	 * for why pumping UI events while "working" is required for {@code fork ==
	 * false} to be able to show a dialog.
	 */
	@Test
	public void testRun_noFork_notCancelable_runInForeground_callFromUIThread_pumpingUiEventsWhileRunning_dialogShown()
			throws Exception {
		assertDialogShown(false, false, false, true, true);
	}

	/**
	 * See
	 * {@link #testRun_noFork_cancelable_runInBackground_callFromUIThread_blockingUiThread_noDialogShown()}
	 * for why this assertion passing does not, by itself, prove that the
	 * "Always run in background" preference is honored.
	 */
	@Test
	public void testRun_noFork_notCancelable_runInBackground_callFromUIThread_blockingUiThread_noDialogShown()
			throws Exception {
		assertDialogShown(false, false, true, false, false);
	}

	/**
	 * See
	 * {@link #testRun_noFork_cancelable_runInBackground_callFromUIThread_pumpingUiEventsWhileRunning_noDialogShown()}.
	 */
	@Test
	public void testRun_noFork_notCancelable_runInBackground_callFromUIThread_pumpingUiEventsWhileRunning_noDialogShown()
			throws Exception {
		assertDialogShown(false, false, true, true, false);
	}

	/**
	 * With {@code fork == false}, {@link IProgressService#run} executes the
	 * runnable <b>synchronously on the calling thread</b>, which here (like in the
	 * vast majority of real callers, e.g. drag-and-drop handlers) is the UI thread.
	 * <p>
	 * The progress dialog is only opened by a {@code WorkbenchJob} that is
	 * scheduled to run <b>on the UI thread</b> after a delay
	 * ({@link IProgressService#getLongOperationTime()}) via
	 * {@code Display.asyncExec}; actually running that queued job requires the
	 * UI thread's event loop to be pumped (i.e. {@code Display.readAndDispatch()}
	 * to be called again). If the runnable just blocks (e.g. with
	 * {@code Thread.sleep}) without ever giving the UI thread a chance to pump
	 * events, that queued job never runs and the dialog is expected to
	 * <b>never</b> appear, no matter how long the operation takes or whether it
	 * is cancelable.
	 * <p>
	 * <b>Caveat:</b> this assertion passing does <b>not</b>, by itself, prove
	 * that the 800ms long-operation delay is honored while the UI thread is
	 * blocked - it can also pass for the wrong reason. {@code ProgressManager}
	 * used to have a bug where, for this exact
	 * {@code fork == false} / not-running-in-background combination, it left
	 * the dialog's {@code openOnRun} flag at its default of {@code true} and
	 * therefore opened the dialog <b>synchronously</b>, immediately, as part of
	 * {@code run()} itself - completely ignoring the 800ms delay. Had that bug
	 * still been present, a dialog would have appeared here too (just far too
	 * early), and this test would have failed - which is how the fix is
	 * indirectly exercised. But if some other, hypothetical bug reintroduced an
	 * immediate/synchronous open while <b>also</b> leaving the UI thread
	 * blocked such that the {@code Show} event never gets a chance to be
	 * observed by this test's listener before the runnable finishes, this
	 * assertion could pass without truly proving the delay is honored. The
	 * unambiguous proof that the delay is honored - i.e. that the dialog is
	 * shown only after the delay elapses and only if the UI thread is kept
	 * responsive - is
	 * {@link #testRun_noFork_cancelable_runInForeground_callFromUIThread_pumpingUiEventsWhileRunning_dialogShown()},
	 * where events are pumped and the dialog is expected (and observed) to
	 * appear.
	 */
	@Test
	public void testRun_noFork_cancelable_runInForeground_callFromUIThread_blockingUiThread_noDialogShown()
			throws Exception {
		assertDialogShown(false, true, false, false, false);
	}

	/**
	 * Unlike the {@code blockingUiThread} variants above, here the runnable
	 * keeps calling {@code Display.readAndDispatch()} while it "works" instead
	 * of just sleeping - i.e. it behaves like a real, responsive long-running
	 * operation that cooperates with the UI thread instead of freezing it. This
	 * gives the {@code WorkbenchJob} that opens the progress dialog after the
	 * "long operation" delay a chance to actually run, so the dialog is shown.
	 * This is the only way {@code fork == false} can show a dialog, since (see
	 * {@link #testRun_noFork_cancelable_runInForeground_callFromUIThread_blockingUiThread_noDialogShown()})
	 * {@code fork == false} can only show a dialog when invoked from the UI thread.
	 */
	@Test
	public void testRun_noFork_cancelable_runInForeground_callFromUIThread_pumpingUiEventsWhileRunning_dialogShown()
			throws Exception {
		assertDialogShown(false, true, false, true, true);
	}

	/**
	 * <b>Caveat:</b> with the runnable blocking the UI thread (i.e. not pumping
	 * events), the dialog never gets a <b>chance</b> to appear "the normal way"
	 * (i.e. via the delayed {@code WorkbenchJob}, see
	 * {@link #testRun_noFork_cancelable_runInForeground_callFromUIThread_blockingUiThread_noDialogShown()})
	 * - regardless of the "Always run in background" preference. So this
	 * assertion passing here does <b>not</b>, by itself, prove that the
	 * preference is honored: it would equally pass if the preference were
	 * completely ignored, or even if {@code ProgressManager} still had the bug
	 * where the dialog was opened <b>synchronously</b> (ignoring the
	 * 800ms long-operation delay entirely) as soon as {@code run()} was called
	 * - because that buggy synchronous-open code path was itself skipped
	 * whenever the "Always run in background" preference was set. The real
	 * proof that the preference suppresses the
	 * dialog is
	 * {@link #testRun_noFork_cancelable_runInBackground_callFromUIThread_pumpingUiEventsWhileRunning_noDialogShown()},
	 * where the runnable does pump events (giving the dialog a genuine
	 * opportunity to appear) and it still does not show up. This test is kept
	 * only to document/pin the (also correct, if weaker) blocked-UI-thread
	 * behavior.
	 */
	@Test
	public void testRun_noFork_cancelable_runInBackground_callFromUIThread_blockingUiThread_noDialogShown()
			throws Exception {
		assertDialogShown(false, true, true, false, false);
	}

	/**
	 * Even when the runnable pumps UI events while running (see
	 * {@link #testRun_noFork_cancelable_runInForeground_callFromUIThread_pumpingUiEventsWhileRunning_dialogShown()}),
	 * the "Always run in background" preference must still suppress the modal
	 * progress dialog.
	 */
	@Test
	public void testRun_noFork_cancelable_runInBackground_callFromUIThread_pumpingUiEventsWhileRunning_noDialogShown()
			throws Exception {
		assertDialogShown(false, true, true, true, false);
	}

	@Test
	public void testRun_fork_notCancelable_runInForeground_callFromUIThread_notPumpingUiEvents_dialogShown()
			throws Exception {
		assertDialogShown(true, false, false, false, true);
	}

	@Test
	public void testRun_fork_notCancelable_runInBackground_callFromUIThread_notPumpingUiEvents_noDialogShown()
			throws Exception {
		assertDialogShown(true, false, true, false, false);
	}

	@Test
	public void testRun_fork_cancelable_runInForeground_callFromUIThread_notPumpingUiEvents_dialogShown()
			throws Exception {
		assertDialogShown(true, true, false, false, true);
	}

	@Test
	public void testRun_fork_cancelable_runInBackground_callFromUIThread_notPumpingUiEvents_noDialogShown()
			throws Exception {
		assertDialogShown(true, true, true, false, false);
	}

	/**
	 * Runs {@link IProgressService#run(boolean, boolean, IRunnableWithProgress)}
	 * from the UI thread with the given {@code fork}/{@code cancelable} arguments
	 * and the given "Always run in background" preference, detects whether the
	 * progress dialog popped up while it was running, and asserts the outcome
	 * against {@code expectDialogShown}.
	 *
	 * @param pumpUiEventsWhileRunning whether the runnable itself keeps pumping SWT
	 *                                 events (rather than just blocking, e.g. via
	 *                                 {@code Thread.sleep}) while it "works". Only
	 *                                 matters - and is only safe to set - when
	 *                                 {@code fork == false}: see
	 *                                 {@link #testRun_noFork_cancelable_runInForeground_callFromUIThread_blockingUiThread_noDialogShown()}
	 *                                 and
	 *                                 {@link #testRun_noFork_cancelable_runInForeground_callFromUIThread_pumpingUiEventsWhileRunning_dialogShown()}.
	 */
	private void assertDialogShown(boolean fork, boolean cancelable, boolean runInBackgroundPref,
			boolean pumpUiEventsWhileRunning, boolean expectDialogShown) throws Exception {
		runInBackground(runInBackgroundPref);

		Display display = PlatformUI.getWorkbench().getDisplay();
		boolean dialogShown = runAndDetectProgressDialog(shown -> progressService.run(fork, cancelable,
				monitor -> work(monitor, display, pumpUiEventsWhileRunning, expectDialogShown, shown)));

		assertEquals(expectDialogShown, dialogShown, String.format(
				"Expected progress dialog shown=%s for fork=%s, cancelable=%s, runInBackground=%s, pumpUiEventsWhileRunning=%s",
				expectDialogShown, fork, cancelable, runInBackgroundPref, pumpUiEventsWhileRunning));
	}

	/**
	 * Simulates a long-running operation. If a dialog is expected, it keeps working
	 * until the dialog was shown (bounded by {@link #DIALOG_TIMEOUT_MILLIS}),
	 * otherwise it works long enough for a dialog to have appeared if it was going
	 * to.
	 */
	private void work(IProgressMonitor monitor, Display display, boolean pumpUiEvents, boolean expectDialogShown,
			BooleanSupplier dialogShown) {
		int workUnits = 3;
		SubMonitor sub = SubMonitor.convert(monitor, workUnits);
		// Without an expected dialog: work long enough for the dialog to appear if it
		// was going to
		long workDurationMillis = (long) (progressService.getLongOperationTime() / workUnits * 1.5);
		long dialogDeadline = System.currentTimeMillis() + DIALOG_TIMEOUT_MILLIS;
		for (int i = 0; i < workUnits; i++) {
			long deadline = System.currentTimeMillis() + workDurationMillis;
			while (expectDialogShown ? !dialogShown.getAsBoolean() && System.currentTimeMillis() < dialogDeadline
					: System.currentTimeMillis() < deadline) {
				try {
					if (pumpUiEvents) {
						// Behave like a real, responsive long-running operation: keep
						// dispatching UI events instead of freezing the UI thread, so
						// that the job opening the progress dialog gets a chance to run.
						if (!display.readAndDispatch()) {
							Thread.sleep(5);
						}
					} else {
						Thread.sleep(5);
					}
				} catch (InterruptedException e) {
					// do nothing
				}
			}
			sub.worked(1);
		}
	}

	private static void runInBackground(boolean value) {
		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.RUN_IN_BACKGROUND, value);
	}

	/**
	 * Runs {@code action} while watching for any <b>newly shown</b> {@link Shell}
	 * (i.e. one that did not already exist right before {@code action} started)
	 * becoming visible on the display. Uses an {@link SWT#Show} display filter
	 * rather than polling, because {@code Shell.setVisible(true)} (called by
	 * {@code Window.open()}) fires that event synchronously as a direct consequence
	 * of the call - it does not require the SWT event loop to be pumped. This
	 * matters because with {@code fork == false} the calling (UI) thread never gets
	 * to pump events again once the runnable starts (it just freezes), so a
	 * poll-based approach would never see a dialog that was already opened
	 * synchronously right before the freeze. The action gets a supplier telling
	 * whether a dialog was shown so far.
	 */
	private boolean runAndDetectProgressDialog(DialogAwareRunnable action) throws Exception {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Set<Shell> preexistingShells = new HashSet<>(Arrays.asList(display.getShells()));
		AtomicBoolean dialogShown = new AtomicBoolean(false);
		Listener showListener = event -> {
			if (event.widget instanceof Shell shell && !preexistingShells.contains(shell)
					&& shell.getData() instanceof ProgressMonitorJobsDialog) {
				dialogShown.set(true);
			}
		};

		// SWT fires  Show  synchronously as part of  Shell.setVisible(true)  itself so
		// it will fire even during the frozen, non-pumping case (i.e. fork == false).
		display.addFilter(SWT.Show, showListener);
		try {
			action.run(dialogShown::get);
		} finally {
			display.removeFilter(SWT.Show, showListener);
		}
		return dialogShown.get();
	}

	private interface DialogAwareRunnable {
		void run(BooleanSupplier dialogShown) throws Exception;
	}

}
