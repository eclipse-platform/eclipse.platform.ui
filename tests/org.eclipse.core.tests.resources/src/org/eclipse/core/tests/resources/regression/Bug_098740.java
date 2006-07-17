/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * A parent container (projects and folders) would become out-of-sync if any of
 * its children could not be deleted for some reason. These platform-
 * specific test cases ensure that it does not happen.
 */
public class Bug_098740 extends ResourceTest {

	public Bug_098740(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(Bug_098740.class);
	}

	public void testBug() {
		IProject project = getWorkspace().getRoot().getProject("Bug98740");
		ensureExistsInWorkspace(project, true);
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("0.99", e);
		}
		try {
			project.members();
			fail("1.0");
		} catch (CoreException e) {
			//should fail
		}
		try {
			IResourceVisitor visitor = new IResourceVisitor() {
				public boolean visit(IResource resource) {
					return true;
				}
			};
			project.accept(visitor, IResource.DEPTH_INFINITE, IResource.NONE);
			fail("2.0");
		} catch (CoreException e) {
			//should fail
		}
	}
}
