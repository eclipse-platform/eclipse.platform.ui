/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.perf;

import java.util.ArrayList;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.core.tests.internal.preferences.TestScope;
import org.eclipse.core.tests.runtime.RuntimeTest;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class PreferencePerformanceTest extends RuntimeTest {

	public PreferencePerformanceTest() {
		super();
	}

	public PreferencePerformanceTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(PreferencePerformanceTest.class);
		//		TestSuite suite = new TestSuite(PreferencePerformanceTest.class.getName());
		//		suite.addTest(new PreferencePerformanceTest("testGetString1000Keys"));
		//		return suite;
	}

	private IEclipsePreferences getScopeRoot() {
		return (IEclipsePreferences) Platform.getPreferencesService().getRootNode().node(TestScope.SCOPE);
	}

	public void testGetString1000SequentialKeys() {
		final String qualifier = getUniqueString();
		String[][] kvp = getSequentialKeys(1000);
		final String[] keys = kvp[0];
		final String[] values = kvp[1];

		new PerformanceTestRunner() {
			Preferences prefs;

			protected void setUp() {
				prefs = getScopeRoot().node(qualifier);
				for (int i = 0; i < keys.length; i++)
					prefs.put(keys[i], values[i]);
			}

			protected void test() {
				for (int i = 0; i < keys.length; i++)
					prefs.get(keys[i], null);
			}

			protected void tearDown() {
				try {
					prefs.removeNode();
				} catch (BackingStoreException e) {
					fail("0.99", e);
				}
			}
		}.run(this, 10, 50);
	}

	public void testGetString1000UniqueKeys() {
		final String qualifier = getUniqueString();
		String[][] kvp = getUniqueKeys(1000);
		final String[] keys = kvp[0];
		final String[] values = kvp[1];

		new PerformanceTestRunner() {
			Preferences prefs;

			protected void setUp() {
				prefs = getScopeRoot().node(qualifier);
				for (int i = 0; i < keys.length; i++)
					prefs.put(keys[i], values[i]);
			}

			protected void test() {
				for (int i = 0; i < keys.length; i++)
					prefs.get(keys[i], null);
			}

			protected void tearDown() {
				try {
					prefs.removeNode();
				} catch (BackingStoreException e) {
					fail("0.99", e);
				}
			}
		}.run(this, 10, 50);
	}

	public void testGetString1000CommonPrefixKeys() {
		final String qualifier = getUniqueString();
		String[][] kvp = getCommonPrefixKeys(1000, qualifier);
		final String[] keys = kvp[0];
		final String[] values = kvp[1];

		new PerformanceTestRunner() {
			Preferences prefs;

			protected void setUp() {
				prefs = getScopeRoot().node(qualifier);
				for (int i = 0; i < keys.length; i++)
					prefs.put(keys[i], values[i]);
			}

			protected void test() {
				for (int i = 0; i < keys.length; i++)
					prefs.get(keys[i], null);
			}

			protected void tearDown() {
				try {
					prefs.removeNode();
				} catch (BackingStoreException e) {
					fail("0.99", e);
				}
			}
		}.run(this, 10, 50);
	}

	public void testPutString1000Keys() {
		final String qualifier = getUniqueString();
		final ArrayList keyList = new ArrayList();
		final ArrayList valueList = new ArrayList();

		for (int i = 0; i < 1000; i++) {
			keyList.add(getUniqueString() + Integer.toString(i));
			valueList.add(Integer.toString(i));
		}

		final String[] keys = (String[]) keyList.toArray(new String[keyList.size()]);
		final String[] values = (String[]) valueList.toArray(new String[valueList.size()]);

		new PerformanceTestRunner() {
			Preferences prefs;

			protected void setUp() {
				prefs = getScopeRoot().node(qualifier);
			}

			protected void test() {
				for (int i = 0; i < keys.length; i++)
					prefs.put(keys[i], values[i]);
			}

			protected void tearDown() {
				try {
					prefs.removeNode();
				} catch (BackingStoreException e) {
					fail("0.99", e);
				}
			}
		}.run(this, 10, 50);
	}

	private String[][] getSequentialKeys(int size) {
		ArrayList keyList = new ArrayList();
		ArrayList valueList = new ArrayList();
		for (int i = 0; i < size; i++) {
			keyList.add(Integer.toString(i));
			valueList.add(Integer.toString(i));
		}
		String[][] result = new String[2][];
		result[0] = (String[]) keyList.toArray(new String[keyList.size()]);
		result[1] = (String[]) valueList.toArray(new String[valueList.size()]);
		return result;
	}

	private String[][] getUniqueKeys(int size) {
		ArrayList keyList = new ArrayList();
		ArrayList valueList = new ArrayList();
		for (int i = 0; i < size; i++) {
			keyList.add(getUniqueString() + Integer.toString(i));
			valueList.add(Integer.toString(i));
		}
		String[][] result = new String[2][];
		result[0] = (String[]) keyList.toArray(new String[keyList.size()]);
		result[1] = (String[]) valueList.toArray(new String[valueList.size()]);
		return result;
	}

	private String[][] getCommonPrefixKeys(int size, String prefix) {
		ArrayList keyList = new ArrayList();
		ArrayList valueList = new ArrayList();
		for (int i = 0; i < size; i++) {
			keyList.add(prefix + '.' + getUniqueString());
			valueList.add(Integer.toString(i));
		}
		String[][] result = new String[2][];
		result[0] = (String[]) keyList.toArray(new String[keyList.size()]);
		result[1] = (String[]) valueList.toArray(new String[valueList.size()]);
		return result;
	}

	public void testRemoveString1000Keys() {
		final String qualifier = getUniqueString();
		final ArrayList keyList = new ArrayList();
		final ArrayList valueList = new ArrayList();

		for (int i = 0; i < 1000; i++) {
			keyList.add(getUniqueString() + Integer.toString(i));
			valueList.add(Integer.toString(i));
		}

		final String[] keys = (String[]) keyList.toArray(new String[keyList.size()]);
		final String[] values = (String[]) valueList.toArray(new String[valueList.size()]);

		new PerformanceTestRunner() {
			Preferences prefs;

			protected void setUp() {
				prefs = getScopeRoot().node(qualifier);
				for (int i = 0; i < keys.length; i++)
					prefs.put(keys[i], values[i]);
			}

			protected void test() {
				for (int i = 0; i < keys.length; i++)
					prefs.remove(keys[i]);
			}

			protected void tearDown() {
				try {
					prefs.removeNode();
				} catch (BackingStoreException e) {
					fail("0.99", e);
				}
			}
		}.run(this, 50, 1);
	}

}
