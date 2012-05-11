/*******************************************************************************
 *  Copyright (c) 2010, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.resources.ResourceTest;

public class Bug_331445 extends ResourceTest {
	/**
	 * Constructor for Bug_331445.
	 */
	public Bug_331445() {
		super();
	}

	/**
	 * Constructor for Bug_331445.
	 * @param name
	 */
	public Bug_331445(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(Bug_331445.class);
	}

	public void testBug() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(getUniqueString());

		ensureExistsInWorkspace(project, true);

		String variableName = "a" + getUniqueString();
		String variablePath = "mem:/MyProject";
		String folderName = "MyFolder";
		String rawLinkFolderLocation = variableName + "/" + folderName;
		String linkFolderLocation = variablePath + "/" + folderName;

		try {
			project.getPathVariableManager().setURIValue(variableName, new URI(variablePath));
		} catch (CoreException e) {
			fail("1.0", e);
		} catch (URISyntaxException e) {
			fail("1.1", e);
		}

		IFolder folder = project.getFolder(getUniqueString());

		try {
			folder.createLink(new Path(rawLinkFolderLocation), IResource.ALLOW_MISSING_LOCAL, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}

		assertNull("3.0", folder.getLocation());
		assertEquals("4.0", new Path(rawLinkFolderLocation), folder.getRawLocation());
		try {
			assertEquals("5.0", new URI(linkFolderLocation), folder.getLocationURI());
		} catch (URISyntaxException e) {
			fail("5.1", e);
		}
		try {
			assertEquals("6.0", new URI(rawLinkFolderLocation), folder.getRawLocationURI());
		} catch (URISyntaxException e) {
			fail("6.1", e);
		}
	}
}
