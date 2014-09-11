/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.progress.WorkbenchSiteProgressService;
import org.eclipse.ui.internal.progress.WorkbenchSiteProgressService.SiteUpdateJob;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.5
 * @author Prakash G.R.
 */
public class WorkbenchSiteProgressServiceTest extends UITestCase{

	
	public WorkbenchSiteProgressServiceTest(String testName) {
		super(testName);
	}
	
    private IWorkbenchPart activePart;
	private IWorkbenchWindow window;
	private SiteUpdateJob updateJob;
	private WorkbenchSiteProgressService progressService;
	private IWorkbenchPartSite site;
    
	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		window = openTestWindow("org.eclipse.ui.resourcePerspective");
		activePart = window.getActivePage().getActivePart();
		assertNotNull(activePart);

		site = activePart.getSite();
		progressService = (WorkbenchSiteProgressService) site.getService(IWorkbenchSiteProgressService.class);
		updateJob = progressService.getUpdateJob();
	}
	
	public void forceUpdate() {
		// ugly trick, but keeps the test going ...
		updateJob.run(new NullProgressMonitor());
	}
	
	public void testWaitCursor() throws Exception {
		// Fire a job with cursor set to true and check the cursor

		LongJob jobWithCursor = new LongJob();

		try {
			progressService.schedule(jobWithCursor, 0, true);

			while (jobWithCursor.getState() != Job.RUNNING) {
				Thread.sleep(100);
			}

			processEvents();
			forceUpdate();
			processEvents();

			Cursor cursor = ((Control) ((PartSite) site).getModel().getWidget())
			        .getCursor();
			assertNotNull(cursor);
		} finally {
			jobWithCursor.cancel();
			processEvents();

			// wait till this job is done
			while (jobWithCursor.getState() == Job.RUNNING) {
				Thread.sleep(100);
			}

			processEvents();
			forceUpdate();
			processEvents();
		}
		Cursor cursor = ((Control) ((PartSite) site).getModel().getWidget())
		        .getCursor();
		assertNull(cursor); // no jobs, no cursor
	}

	public void testWaitCursorConcurrentJobs() throws Exception {
		// Fire two jobs, first one with cursor & delay,
		// the second one without any cursor or delay.
		// Till the first job starts running, there should not be a cursor,
		// after it starts running cursor should be present.

		LongJob jobWithoutCursor = new LongJob();
		LongJob jobWithCursor = new LongJob();

		try {
			progressService.schedule(jobWithCursor, 2000, true);
			progressService.schedule(jobWithoutCursor, 0, false);

			while (jobWithoutCursor.getState() != Job.RUNNING) {
				Thread.sleep(100);
			}

			processEvents();

			// we just want the jobWithoutCursor running
			assertTrue(jobWithCursor.getState() != Job.RUNNING);

			forceUpdate();
			processEvents();
			Cursor cursor = ((Control) ((PartSite) site).getModel().getWidget())
			        .getCursor();
			assertNull(cursor); // jobWithoutCursor is scheduled to run first -
								// no cursor now

			while (jobWithCursor.getState() != Job.RUNNING) {
				Thread.sleep(100);
			}

			processEvents();

			// both jobs should be running
			assertTrue(jobWithCursor.getState() == Job.RUNNING
			        && jobWithoutCursor.getState() == Job.RUNNING);

			forceUpdate();
			processEvents();
			cursor = ((Control) ((PartSite) site).getModel().getWidget())
			        .getCursor();
			assertNotNull(cursor); // both running now - cursor should be set
		} finally {
			jobWithCursor.cancel();
			jobWithoutCursor.cancel();
			processEvents();

			// wait till the jobs are done
			while (jobWithCursor.getState() == Job.RUNNING
			        || jobWithoutCursor.getState() == Job.RUNNING) {
				Thread.sleep(100);
			}

			processEvents();
			forceUpdate();
			processEvents();
		}
		Cursor cursor = ((Control) ((PartSite) site).getModel().getWidget())
		        .getCursor();
		assertNull(cursor); // no jobs, no cursor
	}

	class LongJob extends Job{


		public LongJob() {
			super("LongJob");
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			monitor.beginTask("job starts", 1000);
			for (int i = 0; i < 1000; i++) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				if(monitor.isCanceled()) {
					break;
				}
				monitor.worked(1);
			}
			return Status.OK_STATUS;
		}
	}

}
