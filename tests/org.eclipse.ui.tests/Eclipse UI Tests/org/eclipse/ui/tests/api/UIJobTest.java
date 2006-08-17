/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.1
 */
public class UIJobTest extends UITestCase {

    protected IWorkbenchWindow fWindow;

    protected IWorkbenchPage fPage;

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

    private volatile boolean uiJobFinished = false;
    private volatile boolean backgroundThreadStarted = false;
    private volatile boolean backgroundThreadInterrupted = false;
    private volatile boolean backgroundThreadFinishedBeforeUIJob = false;
    private volatile boolean backgroundThreadFinished = false;
    private volatile boolean uiJobFinishedBeforeBackgroundThread = false;
    
    /**
     * Test to ensure that calling join() on a UIJob will block the calling
     * thread until the job has finished. 
     * 
     * @throws Exception
     * @since 3.1
     */
    public void testJoin() throws Exception {

        uiJobFinished = false;
        backgroundThreadStarted = false;
        backgroundThreadFinished = false;
        backgroundThreadInterrupted = false;
        uiJobFinishedBeforeBackgroundThread = false;

        final UIJob testJob = new UIJob("blah blah blah") {
	        /* (non-Javadoc)
	         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
	         */
	        public IStatus runInUIThread(IProgressMonitor monitor) {
	            backgroundThreadFinishedBeforeUIJob = backgroundThreadFinished;
	            uiJobFinished = true;
	            
	            return Status.OK_STATUS;
	        }
        };
        
        testJob.setPriority(Job.INTERACTIVE);
        
        // Background thread that will try to schedule and join a UIJob. If all goes well
        // it should lock up since we're intentionally blocking the UI thread, preventing
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
                uiJobFinishedBeforeBackgroundThread = uiJobFinished;
                backgroundThreadFinished = true;
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
	                
	        // We need to create a situation in which the run() method of the UIJob will have
	        // been called and exited, but the runInUIThread() method will not have been called.
	        // We then put everything else to sleep, making a high probability that the background
	        // thread would wake up if there was nothing blocking it. Note: it is possible that the 
	        // test would still pass if there was a problem and the background thread failed to
	        // wake up for reasons other than its join()... but it creates a high probability of 
	        // things failing if something goes wrong.
	        //
	        // The point of this join() is to block the UI thread, making it impossible to 
	        // run *syncExecs. Joining a low priority job means that the UIJob should get
	        // scheduled first (meaning its run(...) method will be called). The fact that we're 
	        // in the UI thread right now means that the UIJob's runInUIThread method shouldn't 
	        // be called.
	        
	        // Block the UI thread until the UIJob's run() method is called.
	        delayJob.schedule(200);
	        delayJob.join();
	        
	        long finalTime = System.currentTimeMillis();
	        
	        // Ensure that we slept for at least 200ms
	        Assert.assertTrue("We tried to sleep the UI thread, but it woke up too early. ",
	                finalTime - currentTime >= 200);
	        
	        Assert.assertTrue("Background thread did not start, so there was no possibility "
	                + "of testing whether its behavior was correct. This is not a test failure. "
	                + "It means we were unable to run the test. ",
	                backgroundThreadStarted);
	        
	        Assert.assertFalse("A UI job somehow ran to completion while the UI thread was blocked", uiJobFinished);
	        Assert.assertFalse("Background job managed to run to completion, even though it joined a UI thread that still hasn't finished",
	                backgroundThreadFinished);
	        Assert.assertFalse("Background thread was interrupted", backgroundThreadInterrupted);
	        
	        // Now run the event loop. Give the asyncExec a chance to run.
			//Use the display provided by the shell if possible
	        Display display = fWindow.getShell().getDisplay();
	        
	        // Wait until both threads have run to completion. If we wait for more than 3s, something
	        // probably deadlocked.
	        while (!(uiJobFinished && backgroundThreadFinished) ) {    
	            // If we've waited more than 3s for the test job to run, then something is wrong.
	            if (finalTime - System.currentTimeMillis() > 3000) {
	                break;
	            }
	            if (!display.readAndDispatch()) {
	                display.sleep();
	            }
	        }
	        
	        // Now that the event queue is empty, check that our final state is okay.
	        Assert.assertTrue("Background thread did not finish (possible deadlock)", backgroundThreadFinished);
	        Assert.assertTrue("Test job did not finish (possible deadlock)", uiJobFinished);
	        Assert.assertFalse("Background thread was interrupted ", backgroundThreadInterrupted);
	        Assert.assertFalse("Background thread finished before the UIJob, even though the background thread was supposed to be waiting for the UIJob", 
	                backgroundThreadFinishedBeforeUIJob);
	        
	        // This is the whole point of the test: ensure that the background job actually waited for the UI job
	        // to run to completion.
	        Assert.assertFalse("Background thread finished before the UIJob, even though the background thread was supposed to be waiting for the UIJob", 
	                backgroundThreadFinishedBeforeUIJob);
	        
	        // Paranoia check: this is really the same test as above, but it confirms that both
	        // threads agreed on the answer.
	        Assert.assertTrue("Background thread finished before the UIJob, even though the background thread was supposed to be waiting for the UIJob", 
	                uiJobFinishedBeforeBackgroundThread);
        } finally {
            
        }
        
    }
    

}
