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
package org.eclipse.core.tests.internal.preferences;

import java.io.*;
import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.core.tests.runtime.RuntimeTest;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @since 3.0
 */
public class PreferencesServiceTest extends RuntimeTest {

	class ExportVerifier {

		private IEclipsePreferences node;
		private ByteArrayOutputStream output;
		private Set expected;
		private String[] excludes;

		public ExportVerifier(IEclipsePreferences node, String[] excludes) {
			super();
			this.node = node;
			this.excludes = excludes;
			this.expected = new HashSet();
		}

		void addExpected(String path, String key) {
			this.expected.add(EclipsePreferences.encodePath(path, key));
		}

		void addVersion() {
			expected.add("file_export_version");
		}

		void setExcludes(String[] excludes) {
			this.excludes = excludes;
		}

		void addExportRoot(IEclipsePreferences root) {
			expected.add('!' + root.absolutePath());
		}

		void verify() {
			IPreferencesService service = Platform.getPreferencesService();
			this.output = new ByteArrayOutputStream();
			try {
				service.exportPreferences(node, output, excludes);
			} catch (CoreException e) {
				fail("0.0", e);
			}
			ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
			Properties properties = new Properties();
			try {
				properties.load(input);
			} catch (IOException e) {
				fail("1.0", e);
			} finally {
				try {
					input.close();
				} catch (IOException e) {
					// ignore
				}
			}

			if (properties.isEmpty()) {
				assertTrue("2.0", expected.isEmpty());
				return;
			}
			assertEquals("3.0", expected.size(), properties.size());
			for (Iterator i = expected.iterator(); i.hasNext();) {
				String key = (String) i.next();
				assertNotNull("4.0." + key, properties.get(key));
			}
		}
	}

	public PreferencesServiceTest(String name) {
		super(name);
	}

	public static Test suite() {
		// all test methods are named "test..."
		return new TestSuite(PreferencesServiceTest.class);
//				TestSuite suite = new TestSuite();
//				suite.addTest(new PreferencesServiceTest("testValidateVersions"));
//				return suite;
	}

	public void testImportExportBasic() {
		IPreferencesService service = Platform.getPreferencesService();

		// create test node hierarchy
		String qualifier = getRandomString() + '1';
		IEclipsePreferences test = new TestScope().getNode(qualifier);
		String key = getRandomString() + 'k';
		String value = getRandomString() + 'v';
		String key1 = "http://eclipse.org:24";
		String value1 = getRandomString() + "v1";
		String actual = test.get(key, null);
		assertNull("1.0", actual);
		test.put(key, value);
		test.put(key1, value1);
		actual = test.get(key, null);
		assertEquals("1.1", value, actual);
		actual = test.get(key1, null);
		assertEquals("1.2", value1, actual);

		// export it
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			service.exportPreferences(test, output, null);
		} catch (CoreException e) {
			fail("2.0", e);
		} finally {
			try {
				output.close();
			} catch (IOException e1) {
				// ignore
			}
		}
		byte[] bytes = output.toByteArray();

		// add new values
		String newKey = getRandomString() + '3';
		String newValue = getRandomString() + '4';
		actual = test.get(newKey, null);
		assertNull("3.0", actual);
		test.put(newKey, newValue);
		actual = test.get(newKey, null);
		assertEquals("3.1", newValue, actual);
		String newOldValue = getRandomString() + '5';
		test.put(key, newOldValue);
		actual = test.get(key, null);
		assertEquals("3.2", newOldValue, actual);

		// import 
		ByteArrayInputStream input = new ByteArrayInputStream(bytes);
		try {
			service.importPreferences(input);
		} catch (CoreException e) {
			fail("4.0", e);
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				// ignore
			}
		}

		// verify
		test = new TestScope().getNode(qualifier);
		actual = test.get(key, null);
		assertEquals("5.0", value, actual);
		actual = test.get(key1, null);
		assertEquals("5.1", value1, actual);
		actual = test.get(newKey, null);
		assertNull("5.2", actual);
		// ensure that the node isn't dirty (has been saved after the import)
		assertTrue("5.3", test instanceof TestScope);
		assertTrue("5.4", !((TestScope) test).isDirty());

		// clear all
		try {
			test.clear();
		} catch (BackingStoreException e) {
			fail("6.0", e);
		}
		actual = test.get(key, null);
		assertNull("6.1", actual);
		actual = test.get(key1, null);
		assertNull("6.2", actual);
		actual = test.get(newKey, null);
		assertNull("6.3", actual);

		// import
		input = new ByteArrayInputStream(bytes);
		try {
			service.importPreferences(input);
		} catch (CoreException e) {
			fail("7.0", e);
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				// ignore
			}
		}

		// verify
		test = new TestScope().getNode(qualifier);
		actual = test.get(key, null);
		assertEquals("8.0", value, actual);
		actual = test.get(key1, null);
		assertEquals("8.1", value1, actual);
		actual = test.get(newKey, null);
		assertNull("8.2", actual);
	}

	private void assertEquals(String message, String[] one, String[] two) {
		if (one == null && two == null)
			return;
		if (one == two)
			return;
		assertNotNull(message + ".1", one);
		assertNotNull(message + ".2", two);
		assertEquals(message + ".3", one.length, two.length);
		for (int i = 0; i < one.length; i++)
			assertEquals(message + ".4." + i, one[i], two[i]);
	}

	public void testLookupOrder() {
		IPreferencesService service = Platform.getPreferencesService();
		String[] defaultOrder = new String[] {"project", //$NON-NLS-1$ 
				InstanceScope.SCOPE, //
				ConfigurationScope.SCOPE, //
				DefaultScope.SCOPE};
		String[] fullOrder = new String[] {"a", "b", "c"};
		String[] nullKeyOrder = new String[] {"e", "f", "g"};
		String qualifier = getRandomString();
		String key = getRandomString();

		// bogus set parms
		try {
			service.setDefaultLookupOrder(null, key, fullOrder);
			fail("0.0");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			service.setDefaultLookupOrder(qualifier, key, new String[] {"a", null, "b"});
			fail("0.1");
		} catch (IllegalArgumentException e) {
			// expected
		}

		// nothing set
		String[] order = service.getDefaultLookupOrder(qualifier, key);
		assertNull("1.0", order);
		order = service.getLookupOrder(qualifier, key);
		assertNotNull("1.1", order);
		assertEquals("1.2", defaultOrder, order);

		order = service.getDefaultLookupOrder(qualifier, null);
		assertNull("1.3", order);
		order = service.getLookupOrder(qualifier, null);
		assertNotNull("1.4", order);
		assertEquals("1.5", defaultOrder, order);

		// set for qualifier/key
		service.setDefaultLookupOrder(qualifier, key, fullOrder);
		order = service.getDefaultLookupOrder(qualifier, key);
		assertNotNull("2.2", order);
		assertEquals("2.3", fullOrder, order);
		order = service.getLookupOrder(qualifier, key);
		assertNotNull("2.4", order);
		assertEquals("2.5", fullOrder, order);

		// nothing set for qualifier/null
		order = service.getDefaultLookupOrder(qualifier, null);
		assertNull("3.0", order);
		order = service.getLookupOrder(qualifier, null);
		assertNotNull("3.1", order);
		assertEquals("3.2", defaultOrder, order);

		// set for qualifier/null
		service.setDefaultLookupOrder(qualifier, null, nullKeyOrder);
		order = service.getDefaultLookupOrder(qualifier, null);
		assertNotNull("4.0", order);
		assertEquals("4.1", nullKeyOrder, order);
		order = service.getLookupOrder(qualifier, null);
		assertNotNull("4.2", order);
		assertEquals("4.3", nullKeyOrder, order);
		order = service.getDefaultLookupOrder(qualifier, key);
		assertNotNull("4.4", order);
		assertEquals("4.5", fullOrder, order);
		order = service.getLookupOrder(qualifier, key);
		assertNotNull("4.6", order);
		assertEquals("4.7", fullOrder, order);

		// clear qualifier/key (find qualifier/null)
		service.setDefaultLookupOrder(qualifier, key, null);
		order = service.getDefaultLookupOrder(qualifier, key);
		assertNull("5.0", order);
		order = service.getLookupOrder(qualifier, key);
		assertNotNull("5.1", order);
		assertEquals("5.2", nullKeyOrder, order);

		// clear qualifier/null (find returns default-default)
		service.setDefaultLookupOrder(qualifier, null, null);
		order = service.getDefaultLookupOrder(qualifier, key);
		assertNull("6.0", order);
		order = service.getLookupOrder(qualifier, key);
		assertNotNull("6.1", order);
		assertEquals("6.2", defaultOrder, order);

		order = service.getDefaultLookupOrder(qualifier, null);
		assertNull("6.3", order);
		order = service.getLookupOrder(qualifier, null);
		assertNotNull("6.4", order);
		assertEquals("6.5", defaultOrder, order);
	}

	public void testGetWithNodes() {
		IPreferencesService service = Platform.getPreferencesService();
		String qualifier = getRandomString();
		String key = getRandomString();
		String expected = getRandomString();

		// nothing set - navigation
		Preferences node = service.getRootNode().node(TestScope.SCOPE).node(qualifier);
		String actual = node.get(key, null);
		assertNull("10", actual);

		// nothing set - service searching
		actual = service.get(key, null, new Preferences[] {node});
		assertNull("2.0", actual);

		// set value
		node.put(key, expected);

		// value is set - navigation
		actual = node.get(key, null);
		assertNotNull("3.0", actual);
		assertEquals("3.1", expected, actual);

		// value is set - service searching
		actual = service.get(key, null, new Preferences[] {node});
		assertNotNull("4.0", actual);
		assertEquals("4.1", expected, actual);

		// return default value if node list is null
		actual = service.get(key, null, null);
		assertNull("5.0", actual);

		// skip over null nodes
		actual = service.get(key, null, new Preferences[] {null, node});
		assertNotNull("6.0", actual);
		assertEquals("6.1", expected, actual);

		// set the value in the default scope as well
		Preferences defaultNode = service.getRootNode().node(DefaultScope.SCOPE).node(qualifier);
		String defaultValue = getRandomString();
		defaultNode.put(key, defaultValue);
		actual = defaultNode.get(key, null);
		assertNotNull("7.0", actual);
		assertEquals("7.1", defaultValue, actual);

		// pass in both nodes
		actual = service.get(key, null, new Preferences[] {node, defaultNode});
		assertNotNull("8.0", actual);
		assertEquals("8.1", expected, actual);
		// skip nulls
		actual = service.get(key, null, new Preferences[] {null, node, null, defaultNode, null});
		assertNotNull("8.2", actual);
		assertEquals("8.3", expected, actual);
		// reverse the order
		actual = service.get(key, null, new Preferences[] {defaultNode, node});
		assertNotNull("8.4", actual);
		assertEquals("8.5", defaultValue, actual);
		// skip nulls
		actual = service.get(key, null, new Preferences[] {null, null, defaultNode, null, node, null});
		assertNotNull("8.6", actual);
		assertEquals("8.7", defaultValue, actual);
	}

	public void testSearchingStringBasics() {
		IPreferencesService service = Platform.getPreferencesService();
		String qualifier = getRandomString();
		String key = getRandomString();
		Preferences node = service.getRootNode().node(TestScope.SCOPE).node(qualifier);
		Preferences defaultNode = service.getRootNode().node(DefaultScope.SCOPE).node(qualifier);
		String value = getRandomString();
		String defaultValue = getRandomString() + '1';
		String actual = null;

		ArrayList list = new ArrayList();
		list.add(null);
		list.add(new IScopeContext[] {});
		list.add(new IScopeContext[] {null});
		list.add(new IScopeContext[] {new TestScope()});
		list.add(new IScopeContext[] {new TestScope(), new DefaultScope()});
		list.add(new IScopeContext[] {new DefaultScope(), new TestScope()});
		list.add(new IScopeContext[] {new DefaultScope()});
		IScopeContext[][] contexts = (IScopeContext[][]) list.toArray(new IScopeContext[list.size()][]);

		// nothing is set
		for (int i = 0; i < contexts.length; i++) {
			actual = service.getString(qualifier, key, null, contexts[i]);
			assertNull("1.0." + i, actual);
		}

		// set a default value
		defaultNode.put(key, defaultValue);
		actual = defaultNode.get(key, null);
		assertNotNull("2.0", actual);
		assertEquals("2.1", defaultValue, actual);

		// should find it because "default" is in the default-default lookup order
		for (int i = 0; i < contexts.length; i++) {
			actual = service.getString(qualifier, key, null, contexts[i]);
			assertNotNull("3.0." + i, actual);
			assertEquals("3.1." + i, defaultValue, actual);
		}

		// set a real value
		node.put(key, value);
		actual = node.get(key, null);
		assertNotNull("4.0", actual);
		assertEquals("4.1", value, actual);

		// should find the default value since the "test" scope isn't in the lookup order
		for (int i = 0; i < contexts.length; i++) {
			actual = service.getString(qualifier, key, null, contexts[i]);
			assertNotNull("5.0." + i, actual);
			assertEquals("5.1." + i, defaultValue, actual);
		}

		// set the lookup order for qualifier/null
		String[] setOrder = new String[] {TestScope.SCOPE, DefaultScope.SCOPE};
		service.setDefaultLookupOrder(qualifier, null, setOrder);
		String[] order = service.getLookupOrder(qualifier, null);
		assertNotNull("6.0", order);
		assertEquals("6.1", setOrder, order);

		// get the value, should be the real one
		for (int i = 0; i < contexts.length; i++) {
			actual = service.getString(qualifier, key, null, contexts[i]);
			assertNotNull("7.0." + i, actual);
			assertEquals("7.1." + i, value, actual);
		}

		// set the order to be the reverse for the qualifier/key
		setOrder = new String[] {DefaultScope.SCOPE, TestScope.SCOPE};
		service.setDefaultLookupOrder(qualifier, key, setOrder);
		order = service.getLookupOrder(qualifier, key);
		assertNotNull("8.0", order);
		assertEquals("8.1", setOrder, order);

		// get the value, should be the default one
		for (int i = 0; i < contexts.length; i++) {
			actual = service.getString(qualifier, key, null, contexts[i]);
			assertNotNull("9.0." + i, actual);
			assertEquals("9.1." + i, defaultValue, actual);
		}
	}

	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Platform.getPreferencesService().getRootNode().node(TestScope.SCOPE).removeNode();
	}

	public void testGet() {
		IPreferencesService service = Platform.getPreferencesService();
		String qualifier = getRandomString();
		Preferences node = service.getRootNode().node(TestScope.SCOPE).node(qualifier);
		service.setDefaultLookupOrder(qualifier, null, new String[] {TestScope.SCOPE});

		// fun with paths

		String searchPath = "a";
		node.put("a", searchPath);
		assertEquals("3.0", searchPath, service.getString(qualifier, searchPath, null, null));

		searchPath = "a/b";
		node.node("a").put("b", searchPath);
		assertEquals("4.0", searchPath, service.getString(qualifier, searchPath, null, null));

		searchPath = "a//b";
		node.node("a").put("b", searchPath);
		assertEquals("5.0", searchPath, service.getString(qualifier, searchPath, null, null));

		searchPath = "a/b//c";
		node.node("a").node("b").put("c", searchPath);
		assertEquals("6.0", searchPath, service.getString(qualifier, searchPath, null, null));

		searchPath = "a/b//c/d";
		node.node("a").node("b").put("c/d", searchPath);
		assertEquals("7.0", searchPath, service.getString(qualifier, searchPath, null, null));

		searchPath = "/a";
		node.put("a", searchPath);
		assertEquals("8.0", searchPath, service.getString(qualifier, searchPath, null, null));

		searchPath = "/a/b";
		node.node("a").put("b", searchPath);
		assertEquals("9.0", searchPath, service.getString(qualifier, searchPath, null, null));

		searchPath = "///a";
		node.put("/a", searchPath);
		assertEquals("10.0", searchPath, service.getString(qualifier, searchPath, null, null));
	}

	public void testImportLegacy() {
		IPreferencesService service = Platform.getPreferencesService();
		String[] qualifiers = new String[] {getRandomString() + 1, getRandomString() + 2};
		String[] oldKeys = new String[] {getRandomString() + 3, getRandomString() + 4};
		String[] newKeys = new String[] {getRandomString() + 5, getRandomString() + 6};
		Preferences node = service.getRootNode().node(Plugin.PLUGIN_PREFERENCE_SCOPE);
		String actual;

		// nodes shouldn't exist
		for (int i = 0; i < qualifiers.length; i++) {
			try {
				assertTrue("1.0", !node.nodeExists(qualifiers[i]));
			} catch (BackingStoreException e) {
				fail("1.99", e);
			}
		}

		// store some values
		for (int i = 0; i < qualifiers.length; i++) {
			Preferences current = node.node(qualifiers[i]);
			for (int j = 0; j < oldKeys.length; j++) {
				current.put(oldKeys[j], getRandomString());
				actual = current.get(oldKeys[j], null);
				assertNotNull("2.0." + current.absolutePath() + IPath.SEPARATOR + oldKeys[j], actual);
			}
			for (int j = 0; j < newKeys.length; j++) {
				actual = current.get(newKeys[j], null);
				assertNull("2.1." + current.absolutePath() + IPath.SEPARATOR + newKeys[j], actual);
			}
		}

		// import a legacy file
		try {
			service.importPreferences(getLegacyExportFile(qualifiers, newKeys));
		} catch (CoreException e) {
			fail("3.0", e);
		}

		// old values shouldn't exist anymore
		for (int i = 0; i < qualifiers.length; i++) {
			Preferences current = node.node(qualifiers[i]);
			for (int j = 0; j < oldKeys.length; j++)
				assertNull("4.0." + current.absolutePath() + IPath.SEPARATOR + oldKeys[j], current.get(oldKeys[j], null));
			for (int j = 0; j < newKeys.length; j++) {
				actual = current.get(newKeys[j], null);
				assertNotNull("4.1." + current.absolutePath() + IPath.SEPARATOR + newKeys[j], actual);
			}
		}
	}

	private InputStream getLegacyExportFile(String[] qualifiers, String[] keys) {
		Properties properties = new Properties();
		for (int i = 0; i < qualifiers.length; i++) {
			// version id
			properties.put(qualifiers[i], "2.1.3");
			for (int j = 0; j < keys.length; j++)
				properties.put(qualifiers[i] + IPath.SEPARATOR + keys[j], getRandomString());
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			properties.store(output, null);
		} catch (IOException e) {
			fail("#getLegacyExportFile", e);
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				// ignore
			}
		}
		InputStream input = new ByteArrayInputStream(output.toByteArray());
		return input;
	}

	/*
	 * - create a child node with some key/value pairs
	 * - set the excludes to be the child name
	 * - export the parent
	 * - don't expect anything to be exported
	 */
	public void testExportExcludes1() {

		// add some random key/value pairs
		String qualifier = getRandomString();
		String child = "child";
		IEclipsePreferences node = new TestScope().getNode(qualifier);
		Preferences childNode = node.node(child);
		childNode.put("a", "v1");
		childNode.put("b", "v2");
		childNode.put("c", "v3");

		// set the excludes list so it doesn't export anything
		String[] excludes = new String[] {child};

		ExportVerifier verifier = new ExportVerifier(node, excludes);
		verifier.verify();

		// make the child path absolute and try again
		verifier.setExcludes(new String[] {'/' + child});
		verifier.verify();
	}

	/*
	 * - basic export
	 * - set a key/value pair on a node
	 * - export that node
	 * - nothing in the excludes list
	 * - expect that k/v pair to be in the file
	 */
	public void testExportExcludes2() {
		String qualifier = getRandomString();
		IEclipsePreferences node = new TestScope().getNode(qualifier);
		String key = getRandomString();
		String value = getRandomString();
		node.put(key, value);
		String[] excludesList = new String[] {};

		ExportVerifier verifier = new ExportVerifier(node, excludesList);
		verifier.addExpected(node.absolutePath(), key);
		verifier.addVersion();
		verifier.addExportRoot(node);
		verifier.verify();
	}

	/*
	 * - add 2 key/value pairs to a node
	 * - add one of them to the excludes list
	 * - expect only the other key to exist
	 */
	public void testExportExcludes3() {
		String qualifier = getRandomString();
		IEclipsePreferences node = new TestScope().getNode(qualifier);
		String k1 = "a";
		String k2 = "b";
		String v1 = "1";
		String v2 = "2";
		node.put(k1, v1);
		node.put(k2, v2);
		String[] excludesList = new String[] {k1};

		ExportVerifier verifier = new ExportVerifier(node, excludesList);
		verifier.addExpected(node.absolutePath(), k2);
		verifier.addVersion();
		verifier.addExportRoot(node);
		verifier.verify();

		// make the excludes list absolute paths and try again
		verifier.setExcludes(new String[] {'/' + k1});
		verifier.verify();
	}

	/*
	 * - add key/value pairs to a node
	 * - export containing non-matching string
	 * - expect all k/v pairs
	 */
	public void testExportExcludes4() {
		String qualifier = getRandomString();
		IEclipsePreferences node = new TestScope().getNode(qualifier);
		String k1 = "a";
		String k2 = "b";
		String v1 = "1";
		String v2 = "2";
		node.put(k1, v1);
		node.put(k2, v2);
		String[] excludesList = new String[] {"bar"};

		ExportVerifier verifier = new ExportVerifier(node, excludesList);
		verifier.addVersion();
		verifier.addExportRoot(node);
		verifier.addExpected(node.absolutePath(), k1);
		verifier.addExpected(node.absolutePath(), k2);
		verifier.verify();
	}

	/*
	 * - exporting default values shouldn't do anything
	 */
	public void testExportDefaults() {
		String qualifier = getRandomString();
		IEclipsePreferences node = new DefaultScope().getNode(qualifier);
		for (int i = 0; i < 10; i++)
			node.put(Integer.toString(i), getRandomString());

		ExportVerifier verifier = new ExportVerifier(node, null);
		verifier.verify();
	}

	/*
	 * - create 2 child with 2 key/value pairs each
	 * - excludes is a single key from one of the children
	 * - export parent
	 * - expect all values to be exported but that one
	 */
	public void testExportExcludes5() {
		String qualifier = getRandomString();
		IEclipsePreferences node = new TestScope().getNode(qualifier);
		Preferences child1 = node.node("c1");
		Preferences child2 = node.node("c2");
		String k1 = "a";
		String k2 = "b";
		String v1 = "1";
		String v2 = "2";
		child1.put(k1, v1);
		child1.put(k2, v2);
		child2.put(k1, v1);
		child2.put(k2, v2);
		String[] excludes = new String[] {child1.name() + '/' + k2};
		ExportVerifier verifier = new ExportVerifier(node, excludes);
		verifier.addVersion();
		verifier.addExportRoot(node);
		verifier.addExpected(child1.absolutePath(), k1);
		verifier.addExpected(child2.absolutePath(), k1);
		verifier.addExpected(child2.absolutePath(), k2);
	}

	public void testValidateVersions() {
		final char BUNDLE_VERSION_PREFIX = '@';
		
		// ensure that there is at least one value in the prefs
		Preferences scopeRoot = Platform.getPreferencesService().getRootNode().node(Plugin.PLUGIN_PREFERENCE_SCOPE);
		scopeRoot.node("org.eclipse.core.tests.runtime").put("key", "value");

		// no errors if the file doesn't exist
		IPath path = getRandomLocation();
		IStatus result = org.eclipse.core.runtime.Preferences.validatePreferenceVersions(path);
		assertTrue("1.0", result.isOK());

		// no errors for an empty file
		Properties properties = new Properties();
		OutputStream output = null;
		try {
			output = new FileOutputStream(path.toFile());
			properties.store(output, null);
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
		result = org.eclipse.core.runtime.Preferences.validatePreferenceVersions(path);
		assertTrue("2.0", result.isOK());

		// no errors for a file which we write out right now
		try {
			org.eclipse.core.runtime.Preferences.exportPreferences(path);
		} catch (CoreException e) {
			fail("3.0", e);
		}
		result = org.eclipse.core.runtime.Preferences.validatePreferenceVersions(path);
		assertTrue("3.1", result.isOK());

		// warning for old versions
		properties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(path.toFile());
			properties.load(input);
		} catch (IOException e) {
			fail("4.0", e);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					// ignore
				}
		}
		// change all version numbers to "0" so the validation will fail
		for (Enumeration e = properties.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			if (key.charAt(0) == BUNDLE_VERSION_PREFIX)
				properties.put(key, "0");
		}
		output = null;
		try {
			output = new FileOutputStream(path.toFile());
			properties.store(output, null);
		} catch (IOException e) {
			fail("4.1", e);
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					// ignore
				}
		}
		result = org.eclipse.core.runtime.Preferences.validatePreferenceVersions(path);
		assertTrue("4.2", !result.isOK());

	}
}