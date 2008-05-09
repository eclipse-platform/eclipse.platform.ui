/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.resources.ResourceTest;

public class Bug_231301 extends ResourceTest {
	/**
	 * Constructor for Bug_231301.
	 */
	public Bug_231301() {
		super();
	}

	/**
	 * Constructor for Bug_231301.
	 * @param name
	 */
	public Bug_231301(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(Bug_231301.class);
	}

	public void testBug() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project1 = workspace.getRoot().getProject("Project1");
		project1.create(null);
		project1.open(null);

		final IProject project2 = workspace.getRoot().getProject("Project2");
		project2.create(null);
		project2.open(null);

		final WorkspaceJob job = new WorkspaceJob("job") {
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				project2.close(null);
				return Status.OK_STATUS;
			}
		};

		// the listener will schedule another project close
		workspace.addResourceChangeListener(new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				if (event.getResource() == project1) {
					// because notification is run in a protected block,
					// this job will start after the notification
					job.schedule();
					//give the job a chance to start before continuing
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						//ignore
					}
				}
			}
		}, IResourceChangeEvent.PRE_CLOSE);
		
		// close project
		project1.close(getMonitor());
		
		try {
			job.join();
		} catch (InterruptedException e) {
			fail("1.0", e);
		}

		assertTrue("2.0: " + job.getResult().toString(), job.getResult().isOK());
		assertTrue("3.0", !project1.isOpen());
		assertTrue("4.0", !project2.isOpen());
	}
}
