/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;

/**
 * Tests the Preferences import/export feature.
 */
public class PreferenceExportTest extends RuntimeTest {
	public static Test suite() {
		return new TestSuite(PreferenceExportTest.class);
	}

	public PreferenceExportTest() {
		super("");
	}

	/**
	 * Constructor for PreferenceExportTest.
	 * @param name
	 */
	public PreferenceExportTest(String name) {
		super(name);
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		//remove all properties from runtime tests plugin
		Plugin testPlugin = Platform.getPlugin(PI_RUNTIME_TESTS);
		Preferences prefs = testPlugin.getPluginPreferences();
		String[] defaultNames = prefs.defaultPropertyNames();
		for (int i = 0; i < defaultNames.length; i++) {
			prefs.setDefault(defaultNames[i], Preferences.STRING_DEFAULT_DEFAULT);
		}
		String[] names = prefs.propertyNames();
		for (int i = 0; i < names.length; i++) {
			prefs.setToDefault(names[i]);
		}
		testPlugin.savePluginPreferences();
	}

	/**
	 * Tests exporting a preference that is different from the default value, but the same
	 * as the default-default value.  See bug 31458.
	 */
	public void testExportEmptyPreference() {
		final String key1 = "SomeTestKey";
		final String key2 = "SomeOtherTestKey";
		final String default1 = "SomeTestValue";
		final int default2 = 5;
		IPath exportPath = new Path(System.getProperty("java.io.tmpdir")).append(Long.toString(System.currentTimeMillis()));
		exportPath.toFile().delete();
		//add a property change listener that asserts key identity
		Plugin testPlugin = Platform.getPlugin(PI_RUNTIME_TESTS);
		Preferences prefs = testPlugin.getPluginPreferences();
		try {
			//add a preference on the runtime plugin
			prefs.setDefault(key1, default1);
			prefs.setValue(key1, Preferences.STRING_DEFAULT_DEFAULT);
			prefs.setDefault(key2, default2);
			prefs.setValue(key2, Preferences.INT_DEFAULT_DEFAULT);

			testPlugin.savePluginPreferences();

			//export preferences
			try {
				Preferences.exportPreferences(exportPath);
			} catch (CoreException e) {
				fail("1.1", e);
			}

			//change the property value
			prefs.setToDefault(key1);
			prefs.setToDefault(key2);

			assertEquals("1.0", default1, prefs.getString(key1));
			assertEquals("1.1", default2, prefs.getInt(key2));

			//reimport the property
			try {
				Preferences.importPreferences(exportPath);
			} catch (CoreException e) {
				fail("1.2", e);
			}

			//ensure the imported preference is set		
			assertEquals("2.0", Preferences.STRING_DEFAULT_DEFAULT, prefs.getString(key1));
			assertEquals("2.1", Preferences.INT_DEFAULT_DEFAULT, prefs.getInt(key2));

		} finally {
			exportPath.toFile().delete();
		}
	}

	/**
	 * Tests that identity tests on preference keys after
	 * export/import will still work. This is to safeguard
	 * against programming errors in property change listeners.
	 * See bug 20193.
	 */
	public void testKeyIdentityAfterExport() {
		final String key = "SomeTestKey";
		String initialValue = "SomeTestValue";
		IPath exportPath = new Path(System.getProperty("java.io.tmpdir")).append(Long.toString(System.currentTimeMillis()));
		exportPath.toFile().delete();
		//add a property change listener that asserts key identity
		Plugin testPlugin = Platform.getPlugin(PI_RUNTIME_TESTS);
		Preferences prefs = testPlugin.getPluginPreferences();
		Preferences.IPropertyChangeListener listener = new Preferences.IPropertyChangeListener() {
			public void propertyChange(Preferences.PropertyChangeEvent event) {
				assertSame("1.0", event.getProperty(), key);
			}
		};
		prefs.addPropertyChangeListener(listener);
		try {
			//add a preference on the runtime plugin
			prefs.setValue(key, initialValue);
			testPlugin.savePluginPreferences();

			//export preferences
			try {
				Preferences.exportPreferences(exportPath);
			} catch (CoreException e) {
				fail("1.1", e);
			}

			//change the property value
			prefs.setValue(key, "SomeOtherValue");

			//reimport the property
			try {
				Preferences.importPreferences(exportPath);
			} catch (CoreException e) {
				fail("1.2", e);
			}

			//set the property to default
			prefs.setToDefault(key);

			//reimport the property
			try {
				Preferences.importPreferences(exportPath);
			} catch (CoreException e) {
				fail("1.3", e);
			}
		} finally {
			exportPath.toFile().delete();
			if (prefs != null)
				prefs.removePropertyChangeListener(listener);
		}
	}
}