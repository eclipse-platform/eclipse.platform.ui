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

import java.text.SimpleDateFormat;
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
		
		
		// first fire a job with cursor set to true and check the cursor
		LongJob jobWithCursor = new LongJob();
		
		progressService.schedule(jobWithCursor, 0, true);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
		System.out.println("after schedule: " + dateFormat.format(new Date()));
		
		while(jobWithCursor.getState() != Job.RUNNING) {
			Thread.sleep(100);
		}
		System.out.println("after running:  " + dateFormat.format(new Date()));
		
		processEvents();
		System.out.println("after process:  " + dateFormat.format(new Date()));
		forceUpdate();		
		System.out.println("after update:   " + dateFormat.format(new Date()));
		processEvents();
		System.out.println("after process2: " + dateFormat.format(new Date()));
		
		Cursor cursor = ((Control) ((PartSite)site).getModel().getWidget()).getCursor();
		System.out.println("after getCursor:" + dateFormat.format(new Date()));
		assertNotNull(cursor);
		
		jobWithCursor.cancel();
		System.out.println("after cancel:   " + dateFormat.format(new Date()));
		processEvents();
		System.out.println("after process3: " + dateFormat.format(new Date()));

		 // wait till this job is done
		while(jobWithCursor.getState() == Job.RUNNING) {
			Thread.sleep(100);
		}
		System.out.println("after done:     " + dateFormat.format(new Date()));
		
		processEvents();
		System.out.println("after process4: " + dateFormat.format(new Date()));
		forceUpdate();
		System.out.println("after update2:  " + dateFormat.format(new Date()));
		processEvents();
		System.out.println("after process5: " + dateFormat.format(new Date()));
		cursor = ((Control) ((PartSite)site).getModel().getWidget()).getCursor();
		assertNull(cursor); // no jobs, no cursor

		// Now fire two jobs, first one with cursor & delay, the second one without any cursor or delay
		// Till the first job starts running, there should not be a cursor, after it starts running cursor should be present

		LongJob jobWithoutCursor = new LongJob();
		jobWithCursor = new LongJob();
		
		progressService.schedule(jobWithCursor, 2000, true);
		progressService.schedule(jobWithoutCursor, 0, false);
		System.out.println("after schedule2:" + dateFormat.format(new Date()));
		
		while(jobWithoutCursor.getState() != Job.RUNNING) {
			Thread.sleep(100);
		}
		
		processEvents();
		System.out.println("after process6: " + dateFormat.format(new Date()));

		// we just want the jobWithoutCursor running
		assertTrue(jobWithCursor.getState() != Job.RUNNING); 
		
		forceUpdate();
		System.out.println("after update3:  " + dateFormat.format(new Date()));
		processEvents();
		System.out.println("after process7: " + dateFormat.format(new Date()));
		cursor = ((Control) ((PartSite)site).getModel().getWidget()).getCursor();
		assertNull(cursor); // jobWithoutCursor is scheduled to run first - no cursor now
		
		while(jobWithCursor.getState() != Job.RUNNING) {
			Thread.sleep(100);
		}
		System.out.println("after done2:    " + dateFormat.format(new Date()));
		
		processEvents();
		System.out.println("after process8: " + dateFormat.format(new Date()));
		
		// both jobs should be running
		assertTrue(jobWithCursor.getState() == Job.RUNNING && jobWithoutCursor.getState() == Job.RUNNING);

		forceUpdate();		
		System.out.println("after update4:  " + dateFormat.format(new Date()));
		processEvents();
		System.out.println("after process9: " + dateFormat.format(new Date()));
		cursor = ((Control) ((PartSite)site).getModel().getWidget()).getCursor();
		assertNotNull(cursor); // both running now - cursor should be set
		System.out.println("end:            " + dateFormat.format(new Date()));
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
