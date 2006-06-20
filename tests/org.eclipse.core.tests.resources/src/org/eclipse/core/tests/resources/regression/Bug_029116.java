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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Test regression of bug 29116.  In this bug, triggering a builder during
 * installation of a nature caused an assertion failure.
 */
public class Bug_029116 extends ResourceTest {
	public Bug_029116() {
		super();
	}

	public Bug_029116(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(Bug_029116.class);
	}

	public void testBug() {
		// Create some resource handles
		IProject project = getWorkspace().getRoot().getProject("PROJECT");

		try {
			// Create and open a project
			project.create(getMonitor());
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}

		// Create and set a build spec for the project
		try {
			IProjectDescription desc = project.getDescription();
			desc.setNatureIds(new String[] {NATURE_29116});
			project.setDescription(desc, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
	}

}
