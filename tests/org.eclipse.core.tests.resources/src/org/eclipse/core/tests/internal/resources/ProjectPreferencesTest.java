/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.osgi.service.prefs.Preferences;

/**
 * @since 3.0
 */
public class ProjectPreferencesTest extends EclipseWorkspaceTest {

	public static Test suite() {
		// all test methods are named "test..."
		return new TestSuite(ProjectPreferencesTest.class);
		//		TestSuite suite = new TestSuite();
		//		suite.addTest(new ProjectPreferencesTest("testSimple"));
		//		return suite;
	}

	public void testSimple() {
		IProject project = getWorkspace().getRoot().getProject("foo");
		String qualifier = "org.eclipse.core.tests.resources";
		String key = "key" + getUniqueString();
		String instanceValue = "instance" + getUniqueString();
		String projectValue = "project" + getUniqueString();
		IScopeContext context = new ProjectScope(project);
		ensureExistsInWorkspace(project, true);

		// set a preference value in the instance scope
		IPreferencesService service = Platform.getPreferencesService();
		Preferences node = service.getRootNode().node(InstanceScope.SCOPE).node(qualifier);
		node.put(key, instanceValue);
		String actual = node.get(key, null);
		assertNotNull("1.0", actual);
		assertEquals("1.1", instanceValue, actual);

		// get the value through service searching
		actual = service.getString(qualifier, key, null, null);
		assertNotNull("2.0", actual);
		assertEquals("2.1", instanceValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[0]);
		assertNotNull("2.2", actual);
		assertEquals("2.3", instanceValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{null});
		assertNotNull("2.4", actual);
		assertEquals("2.5", instanceValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{null, context});
		assertNotNull("2.6", actual);
		assertEquals("2.7", instanceValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{context});
		assertNotNull("2.8", actual);
		assertEquals("2.9", instanceValue, actual);

		// set a preference value in the project scope
		node = service.getRootNode().node(ProjectScope.SCOPE).node(project.getName()).node(qualifier);
		node.put(key, projectValue);
		actual = node.get(key, null);
		assertNotNull("3.0", actual);
		assertEquals("3.1", projectValue, actual);

		// get the value through service searching
		actual = service.getString(qualifier, key, null, null);
		assertNotNull("4.0", actual);
		assertEquals("4.1", instanceValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[0]);
		assertNotNull("4.2", actual);
		assertEquals("4.3", instanceValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{null});
		assertNotNull("4.4", actual);
		assertEquals("4.5", instanceValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{context});
		assertNotNull("4.6", actual);
		assertEquals("4.7", projectValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{null, context});
		assertNotNull("4.8", actual);
		assertEquals("4.9", projectValue, actual);

		// remove the project scope value
		node = service.getRootNode().node(ProjectScope.SCOPE).node(project.getName()).node(qualifier);
		node.remove(key);
		actual = node.get(key, null);
		assertNull("5.0", actual);

		// get the value through service searching
		actual = service.getString(qualifier, key, null, null);
		assertNotNull("6.0", actual);
		assertEquals("6.1", instanceValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[0]);
		assertNotNull("6.2", actual);
		assertEquals("6.3", instanceValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{null});
		assertNotNull("6.4", actual);
		assertEquals("6.5", instanceValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{null, context});
		assertNotNull("6.6", actual);
		assertEquals("6.7", instanceValue, actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{context});
		assertNotNull("6.8", actual);
		assertEquals("6.9", instanceValue, actual);

		// remove the instance value so there is nothing
		node = service.getRootNode().node(InstanceScope.SCOPE).node(qualifier);
		node.remove(key);
		actual = node.get(key, null);
		assertNull("7.0", actual);
		actual = service.getString(qualifier, key, null, null);
		assertNull("7.1", actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[0]);
		assertNull("7.2", actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{null});
		assertNull("7.3", actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{null, context});
		assertNull("7.4", actual);
		actual = service.getString(qualifier, key, null, new IScopeContext[]{context});
		assertNull("7.5", actual);
	}

}
