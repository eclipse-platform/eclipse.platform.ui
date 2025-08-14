/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.services;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.5
 * @author Prakash G.R.
 */
@RunWith(JUnit4.class)
public class WorkbenchSiteProgressServiceTest extends UITestCase {


	public WorkbenchSiteProgressServiceTest() {
		super(WorkbenchSiteProgressServiceTest.class.getSimpleName());
	}

	private IWorkbenchPart activePart;
	private IWorkbenchWindow window;
	private SiteUpdateJob updateJob;
	private WorkbenchSiteProgressService progressService;
	private IWorkbenchPartSite site;

	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z") //$NON-NLS-1$
			.withZone(ZoneId.systemDefault());

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

	@Test
	public void testWaitCursor() throws Exception {
		// Fire a job with cursor set to true and check the cursor

		LongJob jobWithCursor = new LongJob();

		try {
			progressService.schedule(jobWithCursor, 0, true);
			logTime("after schedule:   ");

			while (jobWithCursor.getState() != Job.RUNNING) {
				Thread.sleep(100);
			}
			logTime("after waiting:    ");

			processEvents();
			logTime("after process:    ");
			forceUpdate();
			logTime("after update:     ");
			processEvents();
			logTime("after process2:   ");

			Cursor cursor = ((Control) ((PartSite) site).getModel().getWidget())
					.getCursor();
			logTime("after getCursor:  ");
			assertNotNull(cursor);
		} finally {
			jobWithCursor.cancel();
			logTime("after cancel:     ");
			processEvents();
			logTime("after process3:   ");

			// wait till this job is done
			while (jobWithCursor.getState() == Job.RUNNING) {
				Thread.sleep(100);
			}
			logTime("after done:       ");

			processEvents();
			logTime("after process4:   ");
			forceUpdate();
			logTime("after update2:    ");
			processEvents();
			logTime("after process5:   ");
		}
		Cursor cursor = ((Control) ((PartSite) site).getModel().getWidget())
				.getCursor();
		logTime("after getCursor2: ");
		assertNull(cursor); // no jobs, no cursor
	}

	@Test
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
			logTime("after schedule:   ");

			while (jobWithoutCursor.getState() != Job.RUNNING) {
				Thread.sleep(100);
			}
			logTime("after waiting:    ");

			processEvents();
			logTime("after process:    ");

			// we just want the jobWithoutCursor running
			assertTrue(jobWithCursor.getState() != Job.RUNNING);

			forceUpdate();
			logTime("after update:     ");
			processEvents();
			logTime("after process2:   ");

			Cursor cursor = ((Control) ((PartSite) site).getModel().getWidget())
					.getCursor();
			logTime("after getCursor:  ");
			assertNull(cursor); // jobWithoutCursor is scheduled to run first -
								// no cursor now

			while (jobWithCursor.getState() != Job.RUNNING) {
				Thread.sleep(100);
			}
			logTime("after waiting2:   ");

			processEvents();
			logTime("after process3:   ");

			// both jobs should be running
			assertTrue(jobWithCursor.getState() == Job.RUNNING
					&& jobWithoutCursor.getState() == Job.RUNNING);

			forceUpdate();
			logTime("after update2:    ");
			processEvents();
			logTime("after process4:   ");
			cursor = ((Control) ((PartSite) site).getModel().getWidget())
					.getCursor();
			logTime("after getCursor2: ");
			assertNotNull(cursor); // both running now - cursor should be set
		} finally {
			jobWithCursor.cancel();
			logTime("after cancel:     ");
			jobWithoutCursor.cancel();
			logTime("after cancel2:    ");
			processEvents();
			logTime("after process5:   ");

			// wait till the jobs are done
			while (jobWithCursor.getState() == Job.RUNNING
					|| jobWithoutCursor.getState() == Job.RUNNING) {
				Thread.sleep(100);
			}
			logTime("after done:       ");

			processEvents();
			logTime("after process6:   ");
			forceUpdate();
			logTime("after update3:    ");
			processEvents();
			logTime("after process7:   ");
		}
		Cursor cursor = ((Control) ((PartSite) site).getModel().getWidget())
				.getCursor();
		logTime("after getCursor3: ");
		assertNull(cursor); // no jobs, no cursor
	}

	private void logTime(String message) {
		System.out.println(message + dateFormat.format(new Date().toInstant()));
	}

	static class LongJob extends Job{


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
