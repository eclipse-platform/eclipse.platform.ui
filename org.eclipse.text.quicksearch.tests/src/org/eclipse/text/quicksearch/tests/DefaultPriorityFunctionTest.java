/*******************************************************************************
 *  Copyright (c) 2019 Julian Honnen
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Julian Honnen <julian.honnen@vector.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.text.quicksearch.internal.core.priority.DefaultPriorityFunction;
import org.eclipse.text.quicksearch.internal.core.priority.PriorityFunction;
import org.eclipse.text.quicksearch.internal.ui.QuickSearchActivator;
import org.junit.Before;
import org.junit.Test;

public class DefaultPriorityFunctionTest {

	private DefaultPriorityFunction fPriorityFunction;

	@Before
	public void setup() {
		fPriorityFunction = new DefaultPriorityFunction();
		fPriorityFunction.configure(QuickSearchActivator.getDefault().getPreferences());
	}

	@Test
	public void testIgnoreLinkedContainers() throws Exception {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject p1 = root.getProject("p1");
		p1.create(null);
		p1.open(null);
		IProject p2 = root.getProject("p2");
		p2.create(null);
		p2.open(null);

		IFolder f1 = p1.getFolder("f1");
		f1.create(true, true, null);

		IFolder linkedp1 = p2.getFolder("p1");
		linkedp1.createLink(p1.getLocationURI(), 0, null);

		IFolder linkedF1 = p2.getFolder("f1");
		linkedF1.createLink(f1.getLocationURI(), 0, null);

		assertEquals(PriorityFunction.PRIORITY_IGNORE, fPriorityFunction.priority(linkedp1), 1.0);
		assertNotEquals(PriorityFunction.PRIORITY_IGNORE, fPriorityFunction.priority(p1), 1.0);
		assertEquals(PriorityFunction.PRIORITY_IGNORE, fPriorityFunction.priority(linkedF1), 1.0);
		assertNotEquals(PriorityFunction.PRIORITY_IGNORE, fPriorityFunction.priority(f1), 1.0);
	}

	@Test
	public void testDoNotIgnoreVirtualFolder() throws Exception {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject p3 = root.getProject("p3");
		p3.create(null);
		p3.open(null);

		IFolder f2 = p3.getFolder("f2");
		f2.create(IResource.VIRTUAL, true, null);

		assertNotEquals(PriorityFunction.PRIORITY_IGNORE, fPriorityFunction.priority(f2), 1.0);
	}

}
