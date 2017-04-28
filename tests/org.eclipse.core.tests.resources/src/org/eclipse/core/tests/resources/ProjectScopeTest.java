/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

public class ProjectScopeTest extends ResourceTest {
	public static Test suite() {
		return new TestSuite(ProjectScopeTest.class);
	}

	public void testEqualsAndHashCode() {
		IProject project = getWorkspace().getRoot().getProject(getUniqueString());
		ProjectScope projectScope1 = new ProjectScope(project);
		ProjectScope projectScope2 = new ProjectScope(project);
		assertTrue(projectScope1.equals(projectScope2));
		assertTrue(projectScope2.equals(projectScope1));
		assertTrue(projectScope1.hashCode() == projectScope2.hashCode());
	}
}
