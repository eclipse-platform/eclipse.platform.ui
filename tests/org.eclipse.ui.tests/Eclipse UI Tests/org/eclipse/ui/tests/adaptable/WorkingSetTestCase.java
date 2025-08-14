/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.adaptable;

import static org.eclipse.ui.PlatformUI.getWorkbench;

import org.eclipse.core.internal.propertytester.ResourceMappingPropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.IResourceActionFilter;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test that Working Sets adapt to resource mappings
 */
@RunWith(JUnit4.class)
public class WorkingSetTestCase extends UITestCase {

	public WorkingSetTestCase() {
		super(WorkingSetTestCase.class.getSimpleName());
	}

	private ResourceMapping getResourceMapping(IWorkingSet set) {
		return set.getAdapter(ResourceMapping.class);
	}

	private IWorkbenchAdapter getWorkbenchAdapter(IWorkingSet set) {
		return set.getAdapter(IWorkbenchAdapter.class);
	}

	private void assertMatches(ResourceMapping mapping, IResource[] resources) throws CoreException {
		assertTrue(mapping != null);
		ResourceTraversal[] traversals = mapping.getTraversals(null, null);
		assertTrue(traversals.length == resources.length);
		for (ResourceTraversal traversal : traversals) {
			boolean found = false;
			for (IResource element : resources) {
				if (element.equals(traversal.getResources()[0])) {
					found = true;
				}
			}
			assertTrue(found);
		}

	}

	private IProject createProject(String name) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getName() + name);
		project.create(null);
		project.open(IResource.NONE, null);
		return project;
	}

	@Test
	public void testWorkSetAdaptation() throws CoreException {

		// First, test that the set adapts to a ResourceMapping
		IWorkingSetManager man = getWorkbench().getWorkingSetManager();
		IResource[] resources = new IResource[3];
		resources[0] = createProject("Project0");
		resources[1] = createProject("Project1");
		resources[2] = createProject("Project2");
		IWorkingSet set = man.createWorkingSet("test", resources);
		ResourceMapping mapping = getResourceMapping(set);
		assertMatches(mapping, resources);

		// Next, test that the set adapts to an IWorkbenchAdapter
		IWorkbenchAdapter adapter = getWorkbenchAdapter(set);
		String name = adapter.getLabel(set);
		assertEquals("test", name);

		// Test the persistent property filter
		QualifiedName key = new QualifiedName("org.eclipse.ui.test", "set");
		ResourceMappingPropertyTester tester = new ResourceMappingPropertyTester();

		// Test with no persistent properties set
		assertFalse(tester.test(getResourceMapping(set), IResourceActionFilter.PROJECT_PERSISTENT_PROPERTY, new Object[] { "org.eclipse.ui.test.set", "one"}, null));

		// Test with one set on a subset of projects
		resources[0].setPersistentProperty(key, "one");
		assertTrue(tester.test(getResourceMapping(set), IResourceActionFilter.PROJECT_PERSISTENT_PROPERTY, new Object[] { "org.eclipse.ui.test.set", "one"}, null));
		assertFalse(tester.test(getResourceMapping(set), IResourceActionFilter.PROJECT_PERSISTENT_PROPERTY, new Object[] { "org.eclipse.ui.test.set", "wrong"}, null));

		// Test again with the property set to two different values
		resources[1].setPersistentProperty(key, "two");
		assertFalse(tester.test(getResourceMapping(set), IResourceActionFilter.PROJECT_PERSISTENT_PROPERTY, new Object[] { "org.eclipse.ui.test.set", "one"}, null));
		assertFalse(tester.test(getResourceMapping(set), IResourceActionFilter.PROJECT_PERSISTENT_PROPERTY, new Object[] { "org.eclipse.ui.test.set", "two"}, null));

		// Test with them all set
		resources[1].setPersistentProperty(key, "one");
		resources[2].setPersistentProperty(key, "one");
		assertTrue(tester.test(getResourceMapping(set), IResourceActionFilter.PROJECT_PERSISTENT_PROPERTY, new Object[] { "org.eclipse.ui.test.set", "one"}, null));
		assertFalse(tester.test(getResourceMapping(set), IResourceActionFilter.PROJECT_PERSISTENT_PROPERTY, new Object[] { "org.eclipse.ui.test.set", "two"}, null));

		// Test with a closed project in the set
		((IProject)resources[0]).close(null);
		assertTrue(tester.test(getResourceMapping(set), IResourceActionFilter.PROJECT_PERSISTENT_PROPERTY, new Object[] { "org.eclipse.ui.test.set", "one"}, null));
	}

}
