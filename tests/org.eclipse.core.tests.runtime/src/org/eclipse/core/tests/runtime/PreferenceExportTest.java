/**********************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;

/**
 * Tests the Preferences import/export feature.
 */
public class PreferenceExportTest extends RuntimeTest {
public static Test suite() {
	TestSuite suite= new TestSuite(PreferenceExportTest.class);
	return suite;
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
/**
 * Tests exporting a preference that is different from the default value, but the same
 * as the default-default value.  See bug 31458.
 */
public void testExportEmptyPreference() {
	final String key1 = "SomeTestKey";
	final String key2 = "OtherTestKey";
	final String default1 = "SomeTestValue";
	final int default2 = 5;
	IPath exportPath = new Path(System.getProperty("user.dir")).append(Long.toString(System.currentTimeMillis()));
	exportPath.toFile().delete();
	//add a property change listener that asserts key identity
	Plugin runtime = Platform.getPlugin(Platform.PI_RUNTIME);
	Preferences prefs = runtime.getPluginPreferences();
	try {
		//add a preference on the runtime plugin
		prefs.setDefault(key1, default1);
		prefs.setValue(key1, Preferences.STRING_DEFAULT_DEFAULT);
		prefs.setDefault(key2, default2);
		prefs.setValue(key2, Preferences.INT_DEFAULT_DEFAULT);
		
		runtime.savePluginPreferences();
		
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
	IPath exportPath = new Path(System.getProperty("user.dir")).append(Long.toString(System.currentTimeMillis()));
	exportPath.toFile().delete();
	//add a property change listener that asserts key identity
	Plugin runtime = Platform.getPlugin(Platform.PI_RUNTIME);
	Preferences prefs = runtime.getPluginPreferences();
	Preferences.IPropertyChangeListener listener = new Preferences.IPropertyChangeListener() {
		public void propertyChange(Preferences.PropertyChangeEvent event) {
			assertTrue("1.0", event.getProperty() == key);
		}
	};
	prefs.addPropertyChangeListener(listener);
	try {
		//add a preference on the runtime plugin
		prefs.setValue(key, initialValue);
		runtime.savePluginPreferences();
		
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