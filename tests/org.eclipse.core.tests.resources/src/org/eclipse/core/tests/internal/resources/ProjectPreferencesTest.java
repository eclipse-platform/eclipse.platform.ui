/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.tests.resources.ResourceTest;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @since 3.0
 */
public class ProjectPreferencesTest extends ResourceTest {

	private static final String DIR_NAME = ".settings";
	private static final String FILE_EXTENSION = "prefs";

	class Tracer implements IEclipsePreferences.IPreferenceChangeListener {
		public StringBuffer log = new StringBuffer();

		private String typeCode(Object value) {
			if (value == null) {
				return "";
			}
			if (value instanceof Boolean) {
				return "B";
			}
			if (value instanceof Integer) {
				return "I";
			}
			if (value instanceof Long) {
				return "L";
			}
			if (value instanceof Float) {
				return "F";
			}
			if (value instanceof Double) {
				return "D";
			}
			if (value instanceof String) {
				return "S";
			}
			assertTrue("0.0", false);
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
		 */
		public void preferenceChange(PreferenceChangeEvent event) {
			log.append("[");
			log.append(event.getKey());
			log.append(":");
			log.append(typeCode(event.getOldValue()));
			log.append(event.getOldValue() == null ? "null" : event.getOldValue());
			log.append("->");
			log.append(typeCode(event.getNewValue()));
			log.append(event.getNewValue() == null ? "null" : event.getNewValue());
			log.append("]");
		}
	}

	public static Test suite() {
		// all test methods are named "test..."
		return new TestSuite(ProjectPreferencesTest.class);
		//		TestSuite suite = new TestSuite();
		//		suite.addTest(new ProjectPreferencesTest("testLoadIsImport"));
		//		return suite;
	}

	public ProjectPreferencesTest(String name) {
		super(name);
	}

	public void testSimple() {
		IProject project = getProject("foo");
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
		list.add(new IScopeContext[] {null});
		IScopeContext[][] contextsWithoutScope = (IScopeContext[][]) list.toArray(new IScopeContext[list.size()][]);

		list = new ArrayList();
		list.add(new IScopeContext[] {projectContext});
		list.add(new IScopeContext[] {null, projectContext});
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
		IProject project = getProject(getUniqueString());
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
		File file = getFileInFilesystem(project, qualifier);
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
		String newValue = "newValue" + getUniqueString();
		props.put(newKey, newValue);

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

		IFile workspaceFile = getFileInWorkspace(project, qualifier);

		// ensure that the file is out-of-sync with the workspace
		// by changing the lastModified time
		touchInFilesystem(workspaceFile);

		// resource change is fired
		try {
			workspaceFile.refreshLocal(IResource.DEPTH_ZERO, getMonitor());
		} catch (CoreException e) {
			fail("3.1", e);
		}

		// validate new settings
		actual = node.get(key, null);
		assertEquals("4.1", value, actual);
		actual = node.get(newKey, null);
		assertEquals("4.2", newValue, actual);
	}

	/**
	 * Regression test for bug 60896 - Project preferences remains when deleting/creating project
	 */
	public void testProjectDelete() {
		// create the project
		IProject project = getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);
		// set some settings
		String qualifier = getUniqueString();
		String key = getUniqueString();
		String value = getUniqueString();
		IScopeContext context = new ProjectScope(project);
		Preferences node = context.getNode(qualifier);
		Preferences parent = node.parent().parent();
		node.put(key, value);
		assertEquals("1.0", value, node.get(key, null));

		try {
			// delete the project
			project.delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}

		try {
			// project pref should not exist
			assertTrue("3.0", !parent.nodeExists(project.getName()));
		} catch (BackingStoreException e) {
			fail("3.1", e);
		}

		// create a project with the same name
		ensureExistsInWorkspace(project, true);

		// ensure that the preference value is not set
		assertNull("4.0", context.getNode(qualifier).get(key, null));
	}

	/** See bug 91244, bug 93398 and bug 211006. */
	public void testProjectMove() {
		IProject project1 = getProject(getUniqueString());
		IProject project2 = getProject(getUniqueString());
		
		ensureExistsInWorkspace(new IResource[] {project1}, true);
		String qualifier = getUniqueString();
		String key = getUniqueString();
		String value = getUniqueString();
		Preferences node = new ProjectScope(project1).getNode(qualifier);
		node.put(key, value);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.0", e);
		}
		// move project
		try {
			project1.move(new Path(project2.getName()), false, null);
		} catch (CoreException e) {
			fail("2.0", e);
		}
		
		// ensure that preferences for the old project are removed
		node = Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE);
		assertNotNull("2.1", node);
		try {
			assertTrue("2.2", !node.nodeExists(project1.getName()));
		} catch (BackingStoreException e) {
			fail("2.3", e);
		}
		
		// ensure preferences are preserved
		node = Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE);
		assertNotNull("2.3", node);
		try {
			assertTrue("2.4", node.nodeExists(project2.getName()));
		} catch (BackingStoreException e) {
			fail("2.5", e);
		}
		node = node.node(project2.getName());
		assertNotNull("3.1", node);
		try {
			assertTrue("3.2", node.nodeExists(qualifier));
		} catch (BackingStoreException e) {
			fail("3.3", e);
		}
		node = node.node(qualifier);
		assertNotNull("4.1", node);
		assertEquals("4.2", value, node.get(key, null));
	}

	/**
	 * Regression test for Bug 60925 - project preferences do not show up in workspace.
	 * 
	 * Initially we were using java.io.File APIs and writing the preferences files
	 * directly to disk. We need to convert to use Resource APIs so changes
	 * show up in the workspace immediately.
	 */
	public void test_60925() {
		// setup
		IProject project = getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);
		String qualifier = getUniqueString();
		IFile file = getFileInWorkspace(project, qualifier);

		// should be nothing in the file system
		assertTrue("0.0", !file.exists());
		assertTrue("0.1", !file.getLocation().toFile().exists());

		// store a preference key/value pair
		IScopeContext context = new ProjectScope(project);
		String key = getUniqueString();
		String value = getUniqueString();
		Preferences node = context.getNode(qualifier);
		node.put(key, value);

		// flush changes to disk
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.0", e);
		}

		// changes should appear in the workspace
		assertTrue("2.0", file.exists());
		assertTrue("2.1", file.isSynchronized(IResource.DEPTH_ZERO));
	}

	/**
	 * Bug 55410 - [runtime] prefs: keys and valid chars
	 *
	 * Problems with a dot "." as a key name
	 */
	public void test_55410() {
		IProject project1 = getProject(getUniqueString());
		ensureExistsInWorkspace(new IResource[] {project1}, true);
		Preferences node = new ProjectScope(project1).getNode(ResourcesPlugin.PI_RESOURCES).node("subnode");
		String key1 = ".";
		String key2 = "x";
		String value1 = getUniqueString();
		String value2 = getUniqueString();
		node.put(key1, value1);
		node.put(key2, value2);
		assertEquals("0.8", value1, node.get(key1, null));
		assertEquals("0.9", value2, node.get(key2, null));
		IFile prefsFile = getFileInWorkspace(project1, ResourcesPlugin.PI_RESOURCES);
		assertTrue("1.0", !prefsFile.exists());
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.1", e);
		}
		assertTrue("1.1", prefsFile.exists());
		Properties props = new Properties();
		InputStream contents = null;
		try {
			contents = prefsFile.getContents();
		} catch (CoreException e) {
			fail("1.2", e);
		}
		try {
			props.load(contents);
		} catch (IOException e) {
			fail("1.3", e);
		} finally {
			if (contents != null)
				try {
					contents.close();
				} catch (IOException e) {
					// ignore
				}
		}
		assertEquals("2.0", value2, props.getProperty("subnode/" + key2));
		assertEquals("2.1", value1, props.getProperty("subnode/" + key1));
	}

	/**
	 * Bug 61277 - preferences and project moves
	 *
	 * Investigate what happens with project preferences when the
	 * project is moved.
	 */
	public void test_61277a() {
		IProject project = getProject(getUniqueString());
		IProject destProject = getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);
		ensureDoesNotExistInWorkspace(destProject);
		IScopeContext context = new ProjectScope(project);
		String qualifier = getUniqueString();
		Preferences node = context.getNode(qualifier);
		String key = getUniqueString();
		String value = getUniqueString();
		node.put(key, value);
		assertEquals("1.0", value, node.get(key, null));

		try {
			// save the prefs
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.1", e);
		}

		// rename the project
		try {
			project.move(destProject.getFullPath(), true, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}

		context = new ProjectScope(destProject);
		node = context.getNode(qualifier);
		assertEquals("3.0", value, node.get(key, null));
	}

	/**
	 * Bug 61277 - preferences and project moves
	 *
	 * Investigate what happens with project preferences when the
	 * project is moved.
	 */
	public void test_61277b() {
		IProject project1 = getProject(getUniqueString());
		IProject project2 = getProject(getUniqueString());
		ensureExistsInWorkspace(new IResource[] {project1}, true);
		Preferences node = new ProjectScope(project1).getNode(ResourcesPlugin.PI_RESOURCES);
		node.put("key", "value");
		assertTrue("1.0", !getFileInWorkspace(project1, ResourcesPlugin.PI_RESOURCES).exists());
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.99", e);
		}
		assertTrue("1.1", getFileInWorkspace(project1, ResourcesPlugin.PI_RESOURCES).exists());
		// move project and ensures charsets settings are preserved
		try {
			project1.move(project2.getFullPath(), false, null);
		} catch (CoreException e) {
			fail("2.99", e);
		}
		assertTrue("2.0", getFileInWorkspace(project2, ResourcesPlugin.PI_RESOURCES).exists());
		node = new ProjectScope(project2).getNode(ResourcesPlugin.PI_RESOURCES);
		assertEquals("2.1", "value", node.get("key", null));
	}

	/**
	 * Bug 61277 - preferences and project moves
	 *
	 * Investigate what happens with project preferences when the
	 * project is moved.
	 * 
	 * Problems with a key which is the empty string.
	 */
	public void test_61277c() {
		IProject project1 = getProject(getUniqueString());
		ensureExistsInWorkspace(new IResource[] {project1}, true);
		Preferences node = new ProjectScope(project1).getNode(ResourcesPlugin.PI_RESOURCES);
		String key1 = "key";
		String emptyKey = "";
		String value1 = getUniqueString();
		String value2 = getUniqueString();
		node.put(key1, value1);
		node.put(emptyKey, value2);
		assertTrue("1.0", !getFileInWorkspace(project1, ResourcesPlugin.PI_RESOURCES).exists());

		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.1", e);
		}
		assertTrue("1.2", getFileInWorkspace(project1, ResourcesPlugin.PI_RESOURCES).exists());

		// move project and ensures charsets settings are preserved
		IProject project2 = getProject(getUniqueString());
		try {
			project1.move(project2.getFullPath(), false, null);
		} catch (CoreException e) {
			fail("2.99", e);
		}
		assertTrue("2.0", getFileInWorkspace(project2, ResourcesPlugin.PI_RESOURCES).exists());

		node = new ProjectScope(project2).getNode(ResourcesPlugin.PI_RESOURCES);
		assertEquals("2.1", value1, node.get(key1, null));
		assertEquals("2.2", value2, node.get(emptyKey, null));
	}

	private void touchInFilesystem(IFile file) {
		for (int count = 0; count < 30 && file.isSynchronized(IResource.DEPTH_ZERO); count++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
			file.getLocation().toFile().setLastModified(System.currentTimeMillis());
		}
		assertTrue("File not out of sync: " + file.getLocation().toOSString(), !file.isSynchronized(IResource.DEPTH_ZERO));
	}

	/*
	 * Bug 61843 - Saving project preferences failed
	 * 
	 * The project preferences are being accessing (for the first time) from 
	 * within a resource change listener reacting to a change in the workspace. 
	 */
	public void test_61843() {
		// create the project and manually give it a settings file
		final String qualifier = getUniqueString();
		final IProject project = getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);
		IFile settingsFile = getFileInWorkspace(project, qualifier);

		// write some property values in the settings file
		Properties properties = new Properties();
		properties.put("key", "value");
		OutputStream output = null;
		try {
			File file = settingsFile.getLocation().toFile();
			file.getParentFile().mkdirs();
			output = new BufferedOutputStream(new FileOutputStream(file));
			properties.store(output, null);
		} catch (FileNotFoundException e) {
			fail("1.0", e);
		} catch (IOException e) {
			fail("1.1", e);
		} finally {
			try {
				if (output != null)
					output.close();
			} catch (IOException e) {
				// ignore
			}
		}

		// add a log listener to ensure that no errors are reported silently
		ILogListener logListener = new ILogListener() {
			public void logging(IStatus status, String plugin) {
				Throwable exception = status.getException();
				if (exception == null || !(exception instanceof CoreException))
					return;
				if (IResourceStatus.WORKSPACE_LOCKED == ((CoreException) exception).getStatus().getCode())
					fail("3.0");
			}
		};

		// listener to react to changes in the workspace
		IResourceChangeListener rclistener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				new ProjectScope(project).getNode(qualifier);
			}
		};

		// add the listeners
		Platform.addLogListener(logListener);
		getWorkspace().addResourceChangeListener(rclistener, IResourceChangeEvent.POST_CHANGE);

		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("4.0", e);
		} finally {
			Platform.removeLogListener(logListener);
			getWorkspace().removeResourceChangeListener(rclistener);
		}
	}

	/*
	 * Bug 65068 - When the preferences file is deleted, the corresponding preferences 
	 * should be forgotten.
	 */
	public void test_65068() {
		IProject project = getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);
		Preferences node = new ProjectScope(project).getNode(ResourcesPlugin.PI_RESOURCES);
		String key = "key";
		String value = getUniqueString();
		node.put(key, value);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.1", e);
		}
		assertTrue("1.2", getFileInWorkspace(project, ResourcesPlugin.PI_RESOURCES).exists());
		node = new ProjectScope(project).getNode(ResourcesPlugin.PI_RESOURCES);
		assertEquals("1.3", value, node.get(key, null));
		ensureDoesNotExistInWorkspace(project.getFolder(DIR_NAME));
		node = new ProjectScope(project).getNode(ResourcesPlugin.PI_RESOURCES);
		assertNull("2.0", node.get(key, null));
	}

	/*
	 * Bug 95052 - external property removals are not detected.
	 */
	public void test_95052() {
		IProject project = getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);
		Preferences node = new ProjectScope(project).getNode(ResourcesPlugin.PI_RESOURCES);
		node.put("key1", "value1");
		node.put("key2", "value2");
		node.put("key3", "value3");
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.1", e);
		}
		IFile prefFile = getFileInWorkspace(project, ResourcesPlugin.PI_RESOURCES);
		assertTrue("1.2", prefFile.exists());
		Properties properties = new Properties();
		InputStream contents = null;
		try {
			contents = prefFile.getContents();
		} catch (CoreException e) {
			fail("1.3", e);
		}
		try {
			properties.load(contents);
		} catch (IOException e) {
			fail("1.4", e);
		} finally {
			try {
				contents.close();
			} catch (IOException e) {
				//
			}
		}
		assertEquals("2.0", "value1", properties.get("key1"));
		assertEquals("2.1", "value2", properties.get("key2"));
		assertEquals("2.2", "value3", properties.get("key3"));
		// add a new property
		properties.put("key0", "value0");
		// change an existing property		
		properties.put("key2", "value2".toUpperCase());
		// removes a property
		properties.remove("key3");
		ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
		try {
			properties.store(tempOutput, null);
		} catch (IOException e) {
			// should never happen, we are not doing I/O			
			fail("2.4", e);
		}
		ByteArrayInputStream tempInput = new ByteArrayInputStream(tempOutput.toByteArray());
		try {
			prefFile.setContents(tempInput, false, false, getMonitor());
		} catch (CoreException e) {
			fail("2.5", e);
		}

		// here, project preferences should have caught up with the changes		
		node = new ProjectScope(project).getNode(ResourcesPlugin.PI_RESOURCES);
		// property was added
		assertEquals("3.0", "value0", node.get("key0", null));
		// property value was not changed		
		assertEquals("3.1", "value1", node.get("key1", null));
		// property value was changed to upper case		
		assertEquals("3.2", "value2".toUpperCase(), node.get("key2", null));
		// property was deleted
		assertNull("3.3", node.get("key3", null));
	}

	public void testProjectOpenClose() {
		IProject project = getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);
		String qualifier = getUniqueString();
		String key = getUniqueString();
		String value = getUniqueString();
		Preferences node = new ProjectScope(project).getNode(qualifier);
		node.put(key, value);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.0", e);
		}
		// close the project
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("1.1", e);
		}
		// now reopen the project and ensure the settings were not forgotten
		try {
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		node = new ProjectScope(project).getNode(qualifier);
		assertEquals("2.1", value, node.get(key, null));
	}

	public void testContentType() {
		IContentType prefsType = Platform.getContentTypeManager().getContentType(ResourcesPlugin.PI_RESOURCES + ".preferences");
		assertNotNull("1.0", prefsType);
		IContentType associatedType = Platform.getContentTypeManager().findContentTypeFor("some.qualifier." + EclipsePreferences.PREFS_FILE_EXTENSION);
		assertEquals("1.1", prefsType, associatedType);
	}

	public void testListenerOnChangeFile() {
		// setup
		IProject project = getProject(getUniqueString());
		String qualifier = "org.eclipse.core.tests.resources";
		String key = "key" + getUniqueString();
		String value = "value" + getUniqueString();
		IScopeContext projectContext = new ProjectScope(project);
		// create project
		ensureExistsInWorkspace(project, true);
		// set preferences
		Preferences node = projectContext.getNode(qualifier);
		node.put(key, value);
		Tracer tracer = new Tracer();
		((IEclipsePreferences) node).addPreferenceChangeListener(tracer);
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
		File file = getFileInFilesystem(project, qualifier);
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

		// reset the listener
		tracer.log.setLength(0);
		// change settings in the file
		String newKey = "newKey" + getUniqueString();
		String newValue = "newValue" + getUniqueString();
		props.put(newKey, newValue);

		// save the file via the IFile API
		IFile workspaceFile = getFileInWorkspace(project, qualifier);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			props.store(output, null);
			// don't need to close stream since its a byte array
		} catch (IOException e) {
			fail("2.0", e);
		}
		input = new ByteArrayInputStream(output.toByteArray());
		try {
			workspaceFile.setContents(input, IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("2.1", e);
		}

		// validate new settings
		actual = node.get(key, null);
		assertEquals("4.1", value, actual);
		actual = node.get(newKey, null);
		assertEquals("4.2", newValue, actual);

		// validate the change events
		assertEquals("4.3", "[" + newKey + ":null->S" + newValue + "]", tracer.log.toString());
	}

	private static IProject getProject(String name) {
		return getWorkspace().getRoot().getProject(name);
	}

	private static IFile getFileInWorkspace(IProject project, String qualifier) {
		return project.getFile(new Path(DIR_NAME).append(qualifier).addFileExtension(FILE_EXTENSION));
	}

	private static File getFileInFilesystem(IProject project, String qualifier) {
		return project.getLocation().append(DIR_NAME).append(qualifier).addFileExtension(FILE_EXTENSION).toFile();
	}

	/*
	 * Test to ensure that discovering a new pref file (e.g. loading from a repo) 
	 * is the same as doing an import. (ensure the modify listeners are called)
	 */
	public void testLoadIsImport() {

		// setup
		IProject project = getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);
		IScopeContext context = new ProjectScope(project);
		String qualifier = "test.load.is.import";
		IEclipsePreferences node = context.getNode(qualifier);
		String key = "key";
		String oldValue = "old value";
		String newValue = "new value";
		IPreferencesService service = Platform.getPreferencesService();

		// set the values in the nodes and flush the values to the file system
		node.put(key, oldValue);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.99", e);
		}
		assertEquals("1.00", oldValue, node.get(key, null));

		// copy the data into a buffer for later use
		File fileInFS = getFileInFilesystem(project, qualifier);
		InputStream input = null;
		OutputStream output = null;
		byte[] buffer = null;
		try {
			input = new BufferedInputStream(new FileInputStream(fileInFS));
			output = new ByteArrayOutputStream(1024);
			transferData(input, output);
			buffer = ((ByteArrayOutputStream) output).toByteArray();
		} catch (IOException e) {
			fail("2.99", e);
		}

		// remove the file from the project
		IFile fileInWS = getFileInWorkspace(project, qualifier);
		try {
			fileInWS.delete(IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("3.90", e);
		}
		assertTrue("3.0", !fileInWS.exists());
		assertTrue("3.1", !fileInFS.exists());
		IEclipsePreferences projectNode = (IEclipsePreferences) service.getRootNode().node(ProjectScope.SCOPE).node(project.getName());
		try {
			// when the pref file is deleted, the node will be cleared, but not removed
			assertTrue("3.2", isNodeCleared(projectNode, new String[]{qualifier}));
		} catch (BackingStoreException e) {
			fail("3.91", e);
		}
		//		assertNull("3.3", projectNode.node(qualifier).get(oldKey, null));

		// create the file in the project and discover it via a refresh local
		try {
			output = new BufferedOutputStream(new FileOutputStream(fileInFS));
		} catch (FileNotFoundException e) {
			fail("4.90", e);
		}
		input = new BufferedInputStream(new ByteArrayInputStream(buffer));
		transferData(input, output);
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("4.91", e);
		}
		// ensure that the resource changes happen
		waitForBuild();

		// verification - note that the preference modify listener gets called
		// here so that's why we are checking for "new value" and not the original one
		node = context.getNode(qualifier);
		assertEquals("5.0", newValue, node.get(key, null));
	}
	
	/**
	 * @param node the node to check
	 * @param childrenNames the names of children to check
	 * @return true, if the node and its children have no associated values
	 * @throws BackingStoreException
	 */
	private boolean isNodeCleared(Preferences node, String[] childrenNames) throws BackingStoreException {	
		// check if the node has associate values
		if (node.keys().length !=0) return false;
	
		// perform a subsequent check for the node children
		Preferences childNode = null;
		for (int i=0; i<childrenNames.length; i++){
			childNode = node.node(childrenNames[i]);
			if (!isNodeCleared(childNode, childNode.childrenNames())) return false;
		}
		return true;
	}
}
