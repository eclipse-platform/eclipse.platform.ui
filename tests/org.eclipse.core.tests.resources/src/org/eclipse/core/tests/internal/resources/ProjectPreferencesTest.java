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

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.osgi.service.prefs.BackingStoreException;
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
		IScopeContext projectContext = new ProjectScope(project);
		IScopeContext instanceContext = new InstanceScope();
		ensureExistsInWorkspace(project, true);

		ArrayList list = new ArrayList();
		list.add(null);
		list.add(new IScopeContext[0]);
		list.add(new IScopeContext[]{null});
		IScopeContext[][] contextsWithoutScope = (IScopeContext[][]) list.toArray(new IScopeContext[list.size()][]);

		list = new ArrayList();
		list.add(new IScopeContext[]{projectContext});
		list.add(new IScopeContext[]{null, projectContext});
		IScopeContext[][] contextsWithScope = (IScopeContext[][]) list.toArray(new IScopeContext[list.size()][]);

		// set a preference value in the instance scope
		IPreferencesService service = Platform.getPreferencesService();
		Preferences node = instanceContext.getNode(qualifier);
		node.put(key, instanceValue);
		String actual = node.get(key, null);
		assertNotNull("1.0", actual);
		assertEquals("1.1", instanceValue, actual);

		// get the value through service searching
		for (int i = 0; i < contextsWithoutScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithoutScope[i]);
			assertNotNull("2.0." + i, actual);
			assertEquals("2.1." + i, instanceValue, actual);
		}
		for (int i = 0; i < contextsWithScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithScope[i]);
			assertNotNull("2.2." + i, actual);
			assertEquals("2.3." + i, instanceValue, actual);
		}

		// set a preference value in the project scope
		node = projectContext.getNode(qualifier);
		node.put(key, projectValue);
		actual = node.get(key, null);
		assertNotNull("3.0", actual);
		assertEquals("3.1", projectValue, actual);

		// get the value through service searching
		for (int i = 0; i < contextsWithoutScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithoutScope[i]);
			assertNotNull("4.0." + i, actual);
			assertEquals("4.1." + i, instanceValue, actual);
		}
		for (int i = 0; i < contextsWithScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithScope[i]);
			assertNotNull("4.2." + i, actual);
			assertEquals("4.3." + i, projectValue, actual);
		}

		// remove the project scope value
		node = projectContext.getNode(qualifier);
		node.remove(key);
		actual = node.get(key, null);
		assertNull("5.0", actual);

		// get the value through service searching
		for (int i = 0; i < contextsWithoutScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithoutScope[i]);
			assertNotNull("6.0." + i, actual);
			assertEquals("6.1." + i, instanceValue, actual);
		}
		for (int i = 0; i < contextsWithScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithScope[i]);
			assertNotNull("6.2." + i, actual);
			assertEquals("6.3." + i, instanceValue, actual);
		}

		// remove the instance value so there is nothing
		node = instanceContext.getNode(qualifier);
		node.remove(key);
		actual = node.get(key, null);
		for (int i = 0; i < contextsWithoutScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithoutScope[i]);
			assertNull("7.0." + i, actual);
		}
		for (int i = 0; i < contextsWithScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithScope[i]);
			assertNull("7.1." + i, actual);
		}
	}

	public void testListener() {
		// setup
		IProject project = getWorkspace().getRoot().getProject("foo");
		String qualifier = "org.eclipse.core.tests.resources";
		String key = "key" + getUniqueString();
		String value = "value" + getUniqueString();
		IScopeContext projectContext = new ProjectScope(project);
		// create project
		ensureExistsInWorkspace(project, true);
		// set preferences
		Preferences node = projectContext.getNode(qualifier);
		node.put(key, value);
		String actual = node.get(key, null);
		assertNotNull("1.0", actual);
		assertEquals("1.1", value, actual);
		try {
			// flush
			node.flush();
		} catch (BackingStoreException e) {
			fail("0.0", e);
		}

		// get settings filename
		File file = project.getLocation().append(".settings").append(qualifier + ".prefs").toFile();
		Properties props = new Properties();
		InputStream input = null;
		try {
			input = new BufferedInputStream(new FileInputStream(file));
			props.load(input);
		} catch (IOException e) {
			fail("1.0", e);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					// ignore
				}
		}

		// change settings in the file
		String newKey = "newKey" + getUniqueString();
		String fullPath = Path.ROOT.append(ProjectScope.SCOPE).append(project.getName()).append(qualifier).append(newKey).toString();
		String newValue = "newValue" + getUniqueString();
		props.put(fullPath, newValue);

		// save the file and ensure timestamp is different
		OutputStream output = null;
		try {
			output = new BufferedOutputStream(new FileOutputStream(file));
			props.store(output, null);
		} catch (IOException e) {
			fail("2.0", e);
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					// ignore
				}
		}

		// resource change is fired
		IFile workspaceFile = project.getFolder(".settings").getFile(qualifier + ".prefs");
		try {
			workspaceFile.refreshLocal(IResource.DEPTH_ZERO, getMonitor());
		} catch (CoreException e) {
			fail("3.1", e);
		}
		// wait for notification to happen
		try {
			Platform.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
		} catch (OperationCanceledException e) {
			fail("3.2", e);
		} catch (InterruptedException e) {
			fail("3.3", e);
		}

		// validate new settings
		actual = node.get(key, null);
		assertNotNull("4.0", actual);
		assertEquals("4.1", value, actual);
		actual = node.get(newKey, null);
		assertNotNull("4.2", actual);
		assertEquals("4.3", newValue, actual);
	}
}