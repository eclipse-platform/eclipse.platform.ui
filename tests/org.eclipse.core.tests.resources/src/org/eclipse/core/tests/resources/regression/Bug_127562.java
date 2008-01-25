/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Changing a project description requires the workspace root
 * scheduling rule.
 */
public class Bug_127562 extends ResourceTest {

	public Bug_127562(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(Bug_127562.class);
	}

	public void testBug() {
		final IProject project = getWorkspace().getRoot().getProject("Bug127562");
		ensureExistsInWorkspace(project, true);
		IProjectDescription description = null;
		try {
			description = project.getDescription();
			description.setComment("Foo");
		} catch (CoreException e) {
			fail("1.99", e);
		}
		final IProjectDescription finalDescription = description;
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					project.setDescription(finalDescription, getMonitor());
				}
			}, getWorkspace().getRoot(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("4.99", e);
		}
	}
}
