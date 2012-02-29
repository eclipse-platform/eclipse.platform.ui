/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.session;

import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.Test;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.AutomatedTests;
import org.eclipse.core.tests.resources.WorkspaceSessionTest;
import org.eclipse.core.tests.session.WorkspaceSessionTestSuite;

/**
 * Test for bug 369177
 */
public class TestBug369177 extends WorkspaceSessionTest {
	public TestBug369177() {
		super();
	}

	public TestBug369177(String name) {
		super(name);
	}

	public void test01_prepareWorkspace() throws CoreException, URISyntaxException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("project");
		ensureExistsInWorkspace(project, true);

		IFile link = project.getFile("link_to_file");
		link.createLink(new URI("bug369177:/dummy_path.txt"), IResource.ALLOW_MISSING_LOCAL, getMonitor());

		workspace.save(true, getMonitor());
	}

	public void test02_startWorkspace() {
		// nothing to do, if we get this far without an error then we have already passed the test
	}

	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, TestBug369177.class);
	}
}
