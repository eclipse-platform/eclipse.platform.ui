/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
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
/**
 * Constructor for PreferenceExportTest.
 * @param name
 */
public PreferenceExportTest(String name) {
	super(name);
}
public PreferenceExportTest() {
	super("");
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