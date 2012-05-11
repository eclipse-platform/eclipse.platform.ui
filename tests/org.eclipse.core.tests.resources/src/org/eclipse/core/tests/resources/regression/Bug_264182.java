/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.tests.resources.ResourceTest;

public class Bug_264182 extends ResourceTest {

	IProject project;
	IFile dotProject;

	/**
	 * Constructor for Bug_264182.
	 */
	public Bug_264182() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();

		// create a project
		project = getWorkspace().getRoot().getProject(getUniqueString());
		try {
			project.create(new NullProgressMonitor());
			project.open(new NullProgressMonitor());
		} catch (CoreException e) {
			fail("Project creation failed", e);
		}

		// set the description read-only
		dotProject = project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		setReadOnly(dotProject, true);
	}

	protected void tearDown() throws Exception {
		// make the description writable
		setReadOnly(dotProject, false);
		super.tearDown();
	}

	/**
	 * Constructor for Bug_264182.
	 * @param name
	 */
	public Bug_264182(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(Bug_264182.class);
	}

	public void testBug() {
		// create a linked resource
		final IFile file = project.getFile(getUniqueString());
		IFileStore tempFileStore = getTempStore();
		createFileInFileSystem(tempFileStore);
		try {
			file.createLink(tempFileStore.toURI(), IResource.NONE, new NullProgressMonitor());
			fail("1.0");
		} catch (CoreException e) {
			// should fail updating the description
		}

		// the file should not exist in the workspace
		assertDoesNotExistInWorkspace(file);
	}
}
