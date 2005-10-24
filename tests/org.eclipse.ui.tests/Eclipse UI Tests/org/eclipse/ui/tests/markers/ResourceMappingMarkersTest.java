package org.eclipse.ui.tests.markers;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;
import org.eclipse.ui.views.markers.internal.ProblemView;

public class ResourceMappingMarkersTest extends AbstractNavigatorTest {

	/**
	 * Create an instance of the receiver.
	 * 
	 * @param testName
	 */
	public ResourceMappingMarkersTest(String testName) {
		super(testName);
	}

	/**
	 * Set up the receiver.
	 * 
	 * @throws Exception
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();
	}

	public void testResourceMappings() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		ResourceMappingTestView view;

		try {
			view = (ResourceMappingTestView) page
					.showView("org.eclipse.ui.tests.resourceMappingView");
		} catch (PartInitException e) {
			assertTrue(e.getLocalizedMessage(), false);
			return;
		}
		final boolean[] waiting = new boolean[] { true };

		final ProblemView problemView;
		try {
			problemView = (ProblemView) page
					.showView("org.eclipse.ui.views.ProblemView");
		} catch (PartInitException e) {
			assertTrue(e.getLocalizedMessage(), false);
			return;
		}

		IJobChangeListener doneListener = new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (problemView.getCurrentMarkers().toArray().length > 0)
					waiting[0] = false;
			}
		};

		problemView.addUpdateFinishListener(doneListener);
		view.addMarkerToFirstProject();
		long timeOut = System.currentTimeMillis() + 2000;
		waiting[0] = problemView.getCurrentMarkers().toArray().length == 0;

		while (waiting[0] && System.currentTimeMillis() < timeOut) {
			view.getSite().getShell().getDisplay().readAndDispatch();
		}

		assertTrue("No markers generated", problemView.getCurrentMarkers()
				.toArray().length > 0);
		problemView.removeUpdateFinishListener(doneListener);

	}
}
