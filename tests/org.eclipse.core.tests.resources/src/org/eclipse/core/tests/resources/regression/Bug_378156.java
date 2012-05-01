/*******************************************************************************
 *  Copyright (c) 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.util.concurrent.Semaphore;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.CancelingProgressMonitor;
import org.eclipse.core.tests.resources.ResourceTest;
import org.eclipse.core.tests.resources.usecase.SignaledBuilder;

/**
 * Tests a timing problem where a canceled waiting thread could cause a change
 * in another thread to skip building.
 */
public class Bug_378156 extends ResourceTest {

	class ModifyFileJob extends WorkspaceJob {
		private IFile jobFile;
		private Semaphore jobFlag;
		private boolean cancel;

		/**
		 * Modifies a file and then waits for a signal before returning.
		 */
		public ModifyFileJob(IFile file, Semaphore semaphore) {
			super("Modifying " + file);
			this.jobFlag = semaphore;
			jobFile = file;
		}

		public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
			IProgressMonitor pm = cancel ? new CancelingProgressMonitor() : null;
			jobFile.setContents(getRandomContents(), IResource.NONE, pm);
			//wait for signal
			try {
				jobFlag.acquire();
			} catch (InterruptedException e) {
				fail("0.99", e);
			}
			return Status.OK_STATUS;
		}

		/**
		 * Tells this job to cancel itself while waiting
		 */
		public void setCancel() {
			this.cancel = true;
		}
	}

	class BuildListener implements IResourceChangeListener {
		private boolean buildEvent = false;

		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.PRE_BUILD)
				buildEvent = true;
		}

		public boolean buildEventReceived() {
			return buildEvent;
		}

	}

	public static Test suite() {
		return new TestSuite(Bug_378156.class);
	}

	public void testBug() throws Exception {
		//setup
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject project1 = root.getProject("Bug_378156");
		final IFile file = project1.getFile("content.txt");
		ensureExistsInWorkspace(project1, true);
		//add a builder that can tell us if it was called
		IProjectDescription desc = project1.getDescription();
		ICommand command = desc.newCommand();
		command.setBuilderName(SignaledBuilder.BUILDER_ID);
		desc.setBuildSpec(new ICommand[] {command});
		project1.setDescription(desc, getMonitor());
		ensureExistsInWorkspace(file, getRandomContents());
		waitForBuild();

		//initialize the builder
		SignaledBuilder builder = SignaledBuilder.getInstance(project1);
		builder.reset();

		//create a job that will modify the file and then wait for a signal
		final Semaphore semaphore = new Semaphore(0);
		ModifyFileJob runningJob = new ModifyFileJob(file, semaphore);
		runningJob.setRule(file);
		runningJob.schedule();

		//create another copy of the job and immediately cancel it before it gets the lock
		ModifyFileJob waitingJob = new ModifyFileJob(file, semaphore);
		waitingJob.setCancel();
		waitingJob.schedule();
		waitingJob.join();

		//now let the first job finish
		semaphore.release();
		runningJob.join();

		//the builder should have run if the bug is fixed
		assertTrue("1.0", builder.wasExecuted());
	}
}
