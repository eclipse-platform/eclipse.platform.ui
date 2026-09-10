package org.eclipse.ui.tests.internal;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.operations.AdvancedValidationUserApprover;
import org.junit.After;
import org.junit.Before;

/**
 * @since 3.5
 */
public abstract class ResourceActionTest {

	@Before
	public void setUp() throws Exception {
		AdvancedValidationUserApprover.AUTOMATED_MODE = true;
	}

	@After
	public void tearDown() throws Exception {
		AdvancedValidationUserApprover.AUTOMATED_MODE = false;
		pollers.clear();
	}

	protected static void joinDeleteResourceActionJobs() {
		// Join twice as there are two jobs now
		String deleteActionJobName = IDEWorkbenchMessages.DeleteResourceAction_jobName;
		joinJob(deleteActionJobName);
		joinJob(deleteActionJobName);
	}

	private static void joinJob(String deleteActionJobName) {
		boolean joined = false;
		while (!joined) {
			try {
				Job.getJobManager().join(deleteActionJobName, null);
				joined = true;
			} catch (InterruptedException ex) {
				// we might be blocking some other thread, spin the event loop
				// to run syncExecs
				processUIEvents();
				// and now keep trying to join
			}
		}
	}

	private static final List<Runnable> pollers = new ArrayList<>();

	/**
	 * Answers the next modal dialog with the given title from the event loop it
	 * runs: sets the check boxes with the given labels and presses the button
	 * with the given label. The returned flag reports whether the dialog was
	 * seen. Polling stops at the end of the test.
	 */
	protected static boolean[] answerDialog(String title, String button, String... checkBoxes) {
		boolean[] answered = { false };
		Display display = Display.getCurrent();
		Runnable poller = new Runnable() {
			@Override
			public void run() {
				if (!pollers.contains(this)) {
					return;
				}
				Shell shell = findShell(display, title);
				if (shell == null) {
					display.timerExec(50, this);
					return;
				}
				pollers.remove(this);
				for (String checkBox : checkBoxes) {
					Button check = findButton(shell, checkBox);
					assertNotNull(checkBox, check);
					check.setSelection(true);
					check.notifyListeners(SWT.Selection, null);
				}
				Button push = findButton(shell, button);
				assertNotNull(button, push);
				answered[0] = true;
				push.notifyListeners(SWT.Selection, null);
			}
		};
		pollers.add(poller);
		display.timerExec(50, poller);
		return answered;
	}

	private static Shell findShell(Display display, String title) {
		for (Shell shell : display.getShells()) {
			if (title.equals(shell.getText()) && shell.isVisible()) {
				return shell;
			}
		}
		return null;
	}

	private static Button findButton(Composite parent, String label) {
		String plain = label.replace("&", "");
		for (Control child : parent.getChildren()) {
			if (child instanceof Button button && plain.equals(button.getText().replace("&", ""))) {
				return button;
			}
			if (child instanceof Composite composite) {
				Button found = findButton(composite, label);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	/**
	 * After an internal action, see if there are any outstanding SWT events.
	 */
	protected static void processUIEvents() {
		Display display = Display.getCurrent();
		while (display.readAndDispatch()) {
		}
	}

	/**
	 * Subclass the delete action and go into testing mode, which limits user
	 * dialogs.
	 *
	 * @since 3.2
	 */
	protected static class TestDeleteResourceAction extends DeleteResourceAction {

		public TestDeleteResourceAction(IShellProvider provider) {
			super(provider);
			fTestingMode = true;
		}

	}
}