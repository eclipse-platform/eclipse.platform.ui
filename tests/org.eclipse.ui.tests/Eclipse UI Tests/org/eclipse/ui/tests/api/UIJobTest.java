/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import junit.framework.Assert;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * @since 3.1
 */
public class UIJobTest extends UITestCase {

    protected IWorkbenchWindow fWindow;

    protected IWorkbenchPage fPage;

    private String EDITOR_ID = "org.eclipse.ui.tests.api.IEditorActionBarContributorTest";

    /**
     * Constructor for IEditorPartTest
     */
    public UIJobTest(String testName) {
        super(testName);
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        fWindow = openTestWindow();
        fPage = fWindow.getActivePage();
    }

    private volatile boolean testJobRan = false;
    private volatile boolean backgroundThreadStarted = false;
    private volatile boolean backgroundThreadInterrupted = false;
    private volatile boolean backgroundThreadFinishedWhenUIJobRan = false;
    
    /**
     * Test to ensure that calling join() on a UIJob will block the calling
     * thread until the job has finished. 
     * 
     * @throws Exception
     * @since 3.1
     */
    public void testJoin() throws Exception {

        testJobRan = false;
        backgroundThreadStarted = false;
        backgroundThreadInterrupted = false;

        final UIJob testJob = new UIJob("blah blah blah") {
	        /* (non-Javadoc)
	         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
	         */
	        public IStatus runInUIThread(IProgressMonitor monitor) {
	            testJobRan = true;
	            
	            return Status.OK_STATUS;
	        }
        };
        
        testJob.setPriority(Job.INTERACTIVE);
        
        // Background thread that will try to schedule and join a UIJob. If all goes well
        // it should deadlock since we're intentionally blocking the UI thread, preventing
        // it from running.
        Thread testThread = new Thread() {
        /* (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        public void run() {
            testJob.schedule();
            
            backgroundThreadStarted = true;
            
            try {
                testJob.join();
            } catch (InterruptedException e) {
                backgroundThreadInterrupted = true;
            }
        }
        };
        
        // This job does nothing. We use a bogus low-priority job instead of a simple
        // sleep(xxxx) in order to ensure that we don't wake up before the test job was
        // scheduled.
        Job delayJob = new Job("blah") {
            protected IStatus run(IProgressMonitor monitor) {
                
                return Status.OK_STATUS;
            }
            
        };
        delayJob.setPriority(Job.LONG);
        
        try {
	        // Schedule the test thread
	        testThread.start();
	        
	        // Measure current time
	        long currentTime = System.currentTimeMillis();
	                
	        // Sleep for at least 200ms
	        delayJob.schedule(200);
	        delayJob.join();
	        
	        long finalTime = System.currentTimeMillis();
	        
	        // Ensure that we slept for at least 1s
	        Assert.assertTrue("Did not sleep",finalTime - currentTime >= 200);
	        
	        Assert.assertTrue("Background do not start",backgroundThreadStarted);
	        Assert.assertFalse("Test job ran",testJobRan);
	        Assert.assertFalse("Background was interrupted",backgroundThreadInterrupted);
	        
	        // Now run the event loop. Give the asyncExec a chance to run.
			//Use the display provided by the shell if possible
	        Display display = fWindow.getShell().getDisplay();
	        
	        while (!testJobRan) {
	            
	            // If we've waited more than 3s for the test job to run, then something is wrong.
	            if (finalTime - System.currentTimeMillis() > 3000) {
	                break;
	            }
	            if (!display.readAndDispatch()) {
	                display.sleep();
	            }
	        }
	        
	        // Now that the event queue is empty, check that our final state is okay.
	        Assert.assertTrue("Background thread did not start",backgroundThreadStarted);
	        Assert.assertTrue("Test job did not run",testJobRan);
	        Assert.assertFalse("Background thread was interrupted", backgroundThreadInterrupted);
	        Assert.assertFalse("Background thread finished when the job ran",backgroundThreadFinishedWhenUIJobRan);
	        
        } finally {
            
        }
        
    }
    

}
