package org.eclipse.ui.tests.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.FinishedJobs;
import org.eclipse.ui.progress.IProgressService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class ProgressServiceTest extends ProgressTestCase {

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
		assertDialogShown(false, false, false, true, false, false);
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
		assertDialogShown(false, false, false, true, true, true);
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
		assertDialogShown(false, false, true, true, false, false);
	}

	/**
	 * See
	 * {@link #testRun_noFork_cancelable_runInBackground_callFromUIThread_pumpingUiEventsWhileRunning_noDialogShown()}.
	 */
	@Test
	public void testRun_noFork_notCancelable_runInBackground_callFromUIThread_pumpingUiEventsWhileRunning_noDialogShown()
			throws Exception {
		assertDialogShown(false, false, true, true, true, false);
	}

	/**
	 * This is the direct counterpart of the naive assumption that
	 * "{@code fork == false} called from a non-UI thread is fine, since the
	 * calling (non-UI) thread just blocks while the UI thread stays
	 * responsive". That assumption is <b>wrong</b>: {@link IProgressService#run}
	 * cannot be called off the UI thread <b>at all</b>, independently of
	 * {@code fork} - see
	 * {@link #assertDialogShown(boolean, boolean, boolean, boolean, boolean, boolean)}
	 * for why, and
	 * {@link #testRun_fork_cancelable_runInForeground_callFromNonUIThread_notPumpingUiEvents_throwsInvalidThreadAccess()}
	 * for the {@code fork == true} counterpart of this test, showing the
	 * restriction is unconditional on {@code fork}.
	 * <p>
	 * We deliberately do not repeat this for every {@code cancelable} /
	 * {@code runInBackground} combination: the failure happens before those
	 * parameters are ever read, so varying them cannot change the outcome.
	 */
	@Test
	public void testRun_noFork_cancelable_runInForeground_callFromNonUIThread_notPumpingUiEvents_throwsInvalidThreadAccess()
			throws Exception {
		assertDialogShown(false, true, false, false, false, false);
	}

	/**
	 * With {@code fork == false}, {@link IProgressService#run} executes the
	 * runnable <b>synchronously on the calling thread</b>, which here (like in
	 * the vast majority of real callers, e.g. drag-and-drop handlers) is the UI
	 * thread - {@code fork == false} can only ever be meaningfully invoked from
	 * the UI thread in the first place, since calling it from any other thread
	 * fails immediately (see
	 * {@link #testRun_noFork_cancelable_runInForeground_callFromNonUIThread_notPumpingUiEvents_throwsInvalidThreadAccess()}).
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
	 * blocked - it can also pass for the wrong reason. Before this PR,
	 * {@code ProgressManager} had a bug where, for this exact
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
		assertDialogShown(false, true, false, true, false, false);
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
	 * {@code fork == false} can only ever be invoked from the UI thread in the
	 * first place.
	 */
	@Test
	public void testRun_noFork_cancelable_runInForeground_callFromUIThread_pumpingUiEventsWhileRunning_dialogShown()
			throws Exception {
		assertDialogShown(false, true, false, true, true, true);
	}

	/**
	 * <b>Caveat:</b> with the runnable blocking the UI thread (i.e. not pumping
	 * events), the dialog never gets a <b>chance</b> to appear "the normal way"
	 * (i.e. via the delayed {@code WorkbenchJob}, see
	 * {@link #testRun_noFork_cancelable_runInForeground_callFromUIThread_blockingUiThread_noDialogShown()})
	 * - regardless of the "Always run in background" preference. So this
	 * assertion passing here does <b>not</b>, by itself, prove that the
	 * preference is honored: it would equally pass if the preference were
	 * completely ignored, or even if {@code ProgressManager} had the bug this
	 * PR fixes, where the dialog was opened <b>synchronously</b> (ignoring the
	 * 800ms long-operation delay entirely) as soon as {@code run()} was called
	 * - because that buggy synchronous-open code path was itself skipped
	 * whenever the "Always run in background" preference was set, both before
	 * and after this PR. The real proof that the preference suppresses the
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
		assertDialogShown(false, true, true, true, false, false);
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
		assertDialogShown(false, true, true, true, true, false);
	}

	@Test
	public void testRun_fork_notCancelable_runInForeground_callFromUIThread_notPumpingUiEvents_dialogShown()
			throws Exception {
		assertDialogShown(true, false, false, true, false, true);
	}

	@Test
	public void testRun_fork_notCancelable_runInBackground_callFromUIThread_notPumpingUiEvents_noDialogShown()
			throws Exception {
		assertDialogShown(true, false, true, true, false, false);
	}

	/**
	 * One might assume {@code fork == true} is always safe to call from any
	 * thread, since the actual runnable executes on a separate (forked)
	 * thread anyway. This is <b>not</b> the case: {@code ProgressManager.run}
	 * unconditionally computes the dialog's default parent shell via
	 * {@code Display.getShells()} as its very first statement - before it
	 * even looks at {@code fork} - so the call itself always requires the UI
	 * thread, regardless of {@code fork}. See
	 * {@link #assertDialogShown(boolean, boolean, boolean, boolean, boolean, boolean)}
	 * for details, and
	 * {@link #testRun_noFork_cancelable_runInForeground_callFromNonUIThread_notPumpingUiEvents_throwsInvalidThreadAccess()}
	 * for the {@code fork == false} counterpart of this test.
	 * <p>
	 * We deliberately do not repeat this for every {@code cancelable} /
	 * {@code runInBackground} combination: the failure happens before those
	 * parameters are ever read, so varying them cannot change the outcome.
	 */
	@Test
	public void testRun_fork_cancelable_runInForeground_callFromNonUIThread_notPumpingUiEvents_throwsInvalidThreadAccess()
			throws Exception {
		assertDialogShown(true, true, false, false, false, false);
	}

	@Test
	public void testRun_fork_cancelable_runInForeground_callFromUIThread_notPumpingUiEvents_dialogShown()
			throws Exception {
		assertDialogShown(true, true, false, true, false, true);
	}

	@Test
	public void testRun_fork_cancelable_runInBackground_callFromUIThread_notPumpingUiEvents_noDialogShown()
			throws Exception {
		assertDialogShown(true, true, true, true, false, false);
	}

	/**
	 * Runs {@link IProgressService#run(boolean, boolean, IRunnableWithProgress)}
	 * with the given {@code fork}/{@code cancelable} arguments and the given
	 * "Always run in background" preference, detects whether the progress dialog
	 * popped up while it was running (or that the call failed with the expected
	 * {@link SWTException} when {@code callFromUIThread} is {@code false}), and
	 * asserts the outcome against {@code expectDialogShown}.
	 *
	 * @param callFromUIThread whether {@link IProgressService#run} itself is
	 *                         called from this (UI) thread or from a plain
	 *                         background thread. <b>Regardless of
	 *                         {@code fork}</b>: {@code ProgressManager.run}
	 *                         unconditionally computes the dialog's default
	 *                         parent shell via {@code Display.getShells()} as
	 *                         its very first statement - before it even looks
	 *                         at {@code fork}, {@code cancelable} or the
	 *                         "Always run in background" preference - and
	 *                         {@code Display.getShells()} throws an
	 *                         {@link SWTException} with
	 *                         {@link SWT#ERROR_THREAD_INVALID_ACCESS} when
	 *                         called from any thread other than the display's
	 *                         own thread. So calling
	 *                         {@link IProgressService#run} off the UI thread
	 *                         always fails immediately and the operation never
	 *                         even starts - this is <b>not</b> specific to
	 *                         {@code fork == false}. When {@code false},
	 *                         {@code expectDialogShown} must be {@code false}
	 *                         and {@code pumpUiEventsWhileRunning} is
	 *                         meaningless (the runnable's body is never
	 *                         reached).
	 * @param pumpUiEventsWhileRunning whether the runnable itself keeps pumping
	 *                                 SWT events (rather than just blocking, e.g.
	 *                                 via {@code Thread.sleep}) while it
	 *                                 "works". Only matters - and is only safe
	 *                                 to set - when {@code fork == false} and
	 *                                 {@code callFromUIThread == true}: see
	 *                                 {@link #testRun_noFork_cancelable_runInForeground_callFromUIThread_blockingUiThread_noDialogShown()}
	 *                                 and
	 *                                 {@link #testRun_noFork_cancelable_runInForeground_callFromUIThread_pumpingUiEventsWhileRunning_dialogShown()}.
	 */
	private void assertDialogShown(boolean fork, boolean cancelable, boolean runInBackgroundPref,
			boolean callFromUIThread, boolean pumpUiEventsWhileRunning, boolean expectDialogShown) throws Exception {
		runInBackground(runInBackgroundPref);

		Display display = PlatformUI.getWorkbench().getDisplay();
		IRunnableWithProgress longRunningRunnable = monitor -> {
			int workUnits = 3;
			// Give it enough time so that the progress dialog can appear
			long workDurationMillis = (long) (progressService.getLongOperationTime() / workUnits * 1.5);
			SubMonitor sub = SubMonitor.convert(monitor, workUnits);
			for (int i = 0; i < workUnits; i++) {
				try {
					if (pumpUiEventsWhileRunning) {
						// Behave like a real, responsive long-running operation: keep
						// dispatching UI events instead of freezing the UI thread, so
						// that the job opening the progress dialog gets a chance to run.
						long deadline = System.currentTimeMillis() + workDurationMillis;
						while (System.currentTimeMillis() < deadline) {
							if (!display.readAndDispatch()) {
								Thread.sleep(5);
							}
						}
					} else {
						Thread.sleep(workDurationMillis);
					}
				} catch (InterruptedException e) {
					// do nothing
				}
				sub.worked(1);
			}
		};

		if (!callFromUIThread) {
			assertFalse(expectDialogShown,
					"callFromUIThread=false always fails before any dialog could ever show - pass expectDialogShown=false");
			Throwable thrown = runOnNonUiThreadAndCaptureThrowable(
					() -> progressService.run(fork, cancelable, longRunningRunnable));
			SWTException swtException = assertInstanceOf(SWTException.class, thrown,
					() -> String.format(
							"Expected IProgressService#run(fork=%s, cancelable=%s) called off the UI thread to fail "
									+ "with an SWTException (it always does, regardless of fork/cancelable/"
									+ "runInBackground, because ProgressManager.run computes the dialog's default "
									+ "parent shell via Display.getShells() before doing anything else), but got: %s",
							fork, cancelable, thrown));
			assertEquals(SWT.ERROR_THREAD_INVALID_ACCESS, swtException.code,
					"Expected the invalid-thread-access SWTException, but got a different SWTException: "
							+ swtException);
			return;
		}

		boolean dialogShown = runAndDetectProgressDialog(
				() -> progressService.run(fork, cancelable, longRunningRunnable));

		assertEquals(expectDialogShown, dialogShown,
				String.format(
						"Expected progress dialog shown=%s for fork=%s, cancelable=%s, runInBackground=%s, pumpUiEventsWhileRunning=%s",
						expectDialogShown, fork, cancelable, runInBackgroundPref, pumpUiEventsWhileRunning));
	}

	private static void runInBackground(boolean value) {
		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.RUN_IN_BACKGROUND, value);
	}

	/**
	 * Runs {@code action} on a plain, non-UI thread (i.e. not the SWT display
	 * thread) and returns whatever {@link Throwable} it throws - or
	 * {@code null} if it completes without throwing - after joining that
	 * thread. Used to verify that {@link IProgressService#run} refuses to be
	 * called off the UI thread; see
	 * {@link #assertDialogShown(boolean, boolean, boolean, boolean, boolean, boolean)}
	 * for why.
	 */
	private Throwable runOnNonUiThreadAndCaptureThrowable(ThrowingRunnable action) throws InterruptedException {
		Throwable[] thrown = new Throwable[1];
		Thread nonUiThread = new Thread(() -> {
			try {
				action.run();
			} catch (Throwable t) {
				thrown[0] = t;
			}
		}, "ProgressServiceTest-non-UI-caller");
		nonUiThread.start();
		nonUiThread.join();
		return thrown[0];
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
	 * synchronously right before the freeze.
	 */
	private boolean runAndDetectProgressDialog(ThrowingRunnable action) throws Exception {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Set<Shell> preexistingShells = new HashSet<>(Arrays.asList(display.getShells()));
		AtomicBoolean dialogShown = new AtomicBoolean(false);
		Listener showListener = event -> {
			if (event.widget instanceof Shell shell && !preexistingShells.contains(shell)) {
				dialogShown.set(true);
			}
		};

		// SWT fires  Show  synchronously as part of  Shell.setVisible(true)  itself so
		// it will fire even during the frozen, non-pumping case (i.e. fork == false).
		display.addFilter(SWT.Show, showListener);
		try {
			action.run();
		} finally {
			display.removeFilter(SWT.Show, showListener);
		}
		return dialogShown.get();
	}

	interface ThrowingRunnable {
		void run() throws Exception;
	}

}
