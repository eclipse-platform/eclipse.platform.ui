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
import java.util.ArrayList;
import java.util.Random;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.core.tests.runtime.RuntimeTest;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @since 3.0
 */
public class PreferencesServiceTest extends RuntimeTest {

	private static Random random = new Random();

	public PreferencesServiceTest(String name) {
		super(name);
	}

	public static Test suite() {
		// all test methods are named "test..."
		return new TestSuite(PreferencesServiceTest.class);
		//		TestSuite suite = new TestSuite();
		//		suite.addTest(new PreferencesServiceTest("testListeners2"));
		//		return suite;
	}

	public void testImportExportBasic() {
		IPreferencesService service = Platform.getPreferencesService();

		// create test node hierarchy
		String qualifier = getRandomString() + '1';
		IEclipsePreferences test = new TestScope().getNode(qualifier);
		String key = getRandomString() + 'k';
		String value = getRandomString() + 'v';
		String actual = test.get(key, null);
		assertNull("1.0", actual);
		test.put(key, value);
		actual = test.get(key, null);
		assertEquals("1.1", value, actual);

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
		actual = test.get(newKey, null);
		assertNull("5.1", actual);
		// ensure that the node isn't dirty (has been saved after the import)
		assertTrue("5.2", test instanceof TestScope);
		assertTrue("5.3", !((TestScope) test).isDirty());

		// clear all
		try {
			test.clear();
		} catch (BackingStoreException e) {
			fail("6.0", e);
		}
		actual = test.get(key, null);
		assertNull("6.1", actual);
		actual = test.get(newKey, null);
		assertNull("6.2", actual);

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
		actual = test.get(newKey, null);
		assertNull("8.1", actual);
	}

	public void testImportExportExcludes() {
		// TODO
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

	private String getRandomString() {
		return Long.toString(random.nextLong());
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
		// TODO
		if (true)
			return;
		IPreferencesService service = Platform.getPreferencesService();
		String qualifier = getRandomString();
		String key = new Path(getRandomString()).append(getRandomString()).toString();
		Preferences node = service.getRootNode().node(TestScope.SCOPE).node(qualifier);
		String value = getRandomString();
		String actual = null;

		// set a real value
		node.put(key, value);
		actual = node.get(key, null);
		assertNotNull("1.0", actual);
		assertEquals("1.1", value, actual);

		actual = service.getString(qualifier, key, null, null);
		assertNotNull("2.0", actual);
		assertEquals("2.1", value, actual);
	}
}