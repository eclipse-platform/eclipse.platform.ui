package org.eclipse.ui.tests.internal;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.operations.AdvancedValidationUserApprover;

import junit.framework.TestCase;

/**
 * @since 3.5
 *
 */
public abstract class ResourceActionTest extends TestCase {

	/**
	 *
	 */
	public ResourceActionTest() {
		super();
	}

	/**
	 * @param name the test name
	 */
	public ResourceActionTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		AdvancedValidationUserApprover.AUTOMATED_MODE = true;
	}

	@Override
	protected void tearDown() throws Exception {
		AdvancedValidationUserApprover.AUTOMATED_MODE = false;
		super.tearDown();
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

		public boolean fRan = false;

		public TestDeleteResourceAction(IShellProvider provider) {
			super(provider);
			fTestingMode = true;
		}

		@Override
		public void run() {
			super.run();
			fRan = true;
		}

		public boolean didRun() {
			return fRan;
		}
	}
}