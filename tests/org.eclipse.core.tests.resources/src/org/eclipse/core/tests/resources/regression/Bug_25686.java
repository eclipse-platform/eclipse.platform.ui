/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0 which
 * accompanies this distribution, and is available at http://www.eclipse.
 * org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.resources.regression;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class Bug_25686 extends EclipseWorkspaceTest {
/**
 * Constructor for Bug_25686.
 */
public Bug_25686() {
	super();
}
/**
 * Constructor for Bug_25686.
 * @param name
 */
public Bug_25686(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(Bug_25686.class);
}
/**
 * Bug states that JDT cannot copy the .project file from the project root to
 * the build output folder.
 */
public void testBug() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder outputFolder = project.getFolder("bin");
	IFile description = project.getFile(".project");
	IFile destination = outputFolder.getFile(".project");
	ensureExistsInWorkspace(new IResource[] {project, outputFolder}, true);
	
	assertTrue("0.0", description.exists());
	try {
		description.copy(destination.getFullPath(), IResource.NONE, getMonitor());
	} catch (CoreException e) {
		fail("0.99", e);
	}
	assertTrue("0.1", destination.exists());
}
}

