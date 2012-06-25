/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Igor Fedorenko - testNestedProjects
 *******************************************************************************/
package org.eclipse.core.tests.internal.alias;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Tests out of sync cases and refreshLocal in the presence of duplicate
 * resources.
 */
public class SyncAliasTest extends ResourceTest {
	public static Test suite() {
		return new TestSuite(SyncAliasTest.class);
	}

	public SyncAliasTest() {
		super();
	}

	public SyncAliasTest(String name) {
		super(name);
	}

	/**
	 * Tests synchronization in presence of nested projects.
	 * See bug 244315 for details.
	 */
	public void testNestedProjects() throws CoreException {
		final IWorkspaceRoot root = getWorkspace().getRoot();
		File fsRoot = root.getLocation().toFile();
		File fsParent = new File(fsRoot, "parent");
		fsParent.mkdirs();
		final File childProject = new File(fsParent, "nestedProject");
		childProject.mkdirs();
		final IProject nestedProject = root.getProject("nestedProject");
		final IFolder nestedTarget = nestedProject.getFolder("target");

		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				//create top level project
				IProject parent = root.getProject("parent");
				parent.create(monitor);
				parent.open(monitor);
				parent.refreshLocal(IResource.DEPTH_INFINITE, monitor);

				//create project physically nested in top level project
				IProjectDescription description = getWorkspace().newProjectDescription("nestedProject");
				description.setLocation(new Path(childProject.getAbsolutePath()));
				nestedProject.create(description, monitor);
				nestedProject.open(monitor);

				//now create a folder in the nested project, which should also cause it to appear in parent
				nestedTarget.create(false /*force*/, true /*local*/, monitor);
			}
		}, new NullProgressMonitor());

		//at this point the nested target exists, but the alias in the parent project has not been created
		//IProject parent = root.getProject("parent");
		//assertTrue(parent.getFolder("nestedProject/target").exists()); -> this will fail

		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				File file = new File(childProject, "target/classes/META-INF/ejb.xml");
				file.getParentFile().mkdirs();
				try {
					FileOutputStream fos = new FileOutputStream(file);
					fos.close();
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.core.tests.resources", "Test failed due to unexpected IOException"));
				}
				nestedTarget.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}
		}, new NullProgressMonitor());
	}
}
