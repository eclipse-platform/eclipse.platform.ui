/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial test suite
 ******************************************************************************/
package org.eclipse.core.tests.plugintests;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;

public class SavePreferencesTests extends WorkspaceSessionTest {

public SavePreferencesTests() {
	super(null);
}
public SavePreferencesTests(String name) {
	super(name);
}

public void testChangeExistingPreference() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	
	IPluginDescriptor resPlugin = registry.getPluginDescriptor("changeExistingPreference");
	Preferences prefs = null;
	Plugin plugin = null;
	try {
		plugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("1.0 core exception activating \"changeExistingPreference\"");
	}
	prefs = plugin.getPluginPreferences();
	// We should have only one preference
	String[] defaultNames = prefs.defaultPropertyNames();
	String[] prefNames = prefs.propertyNames();
	assertTrue("1.1 One default preferences", defaultNames.length == 1);
	assertTrue("1.2 No explicit preferences", prefNames.length == 0);
	assertTrue("1.3 Default preference name", defaultNames[0].equals("ExistingPreference"));
	assertTrue("1.4 Default preference value",
		prefs.getString("ExistingPreference").equals("From the local plugin directory"));
	
	// Now change the value of this preference
	prefs.setValue("ExistingPreference", "From the test, testChangeExistingPreference");
	plugin.savePluginPreferences();
}

public void testChangeExistingPreference2() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	
	IPluginDescriptor resPlugin = registry.getPluginDescriptor("changeExistingPreference");
	Preferences prefs = null;
	Plugin plugin = null;
	try {
		plugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("2.0 core exception activating \"changeExistingPreference\"");
	}
	prefs = plugin.getPluginPreferences();
	// We should have only one preference with a default value
	// and an explicitly set value (because we set it in
	// testChangeExistingPreference.
	String[] defaultNames = prefs.defaultPropertyNames();
	String[] prefNames = prefs.propertyNames();
	assertTrue("2.1 One default preferences", defaultNames.length == 1);
	assertTrue("2.2 One explicit preference", prefNames.length == 1);
	assertTrue("2.3 Default preference name", defaultNames[0].equals("ExistingPreference"));
	assertTrue("2.4 Explicit preference name", prefNames[0].equals("ExistingPreference"));
	assertTrue("2.5 Default preference value",
		prefs.getDefaultString("ExistingPreference").equals("From the local plugin directory"));
	assertTrue("2.6 Explicit preference value",
		prefs.getString("ExistingPreference").equals("From the test, testChangeExistingPreference"));

	// Now change the value of this preference back to the default
	prefs.setValue("ExistingPreference", "From the local plugin directory");
	plugin.savePluginPreferences();
}

public void testChangeExistingPreference3() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	
	IPluginDescriptor resPlugin = registry.getPluginDescriptor("changeExistingPreference");
	Preferences prefs = null;
	Plugin plugin = null;
	try {
		plugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("3.0 core exception activating \"changeExistingPreference\"");
	}
	prefs = plugin.getPluginPreferences();
	// We should be back to having just the default preference
	// since we set the explicit value back to the default value in
	// testChangeExistingPreference2
	String[] defaultNames = prefs.defaultPropertyNames();
	String[] prefNames = prefs.propertyNames();
	assertTrue("3.1 One default preferences", defaultNames.length == 1);
	assertTrue("3.2 No explicit preferences", prefNames.length == 0);
	assertTrue("3.3 Default preference name", defaultNames[0].equals("ExistingPreference"));
	assertTrue("3.4 Default preference value",
		prefs.getDefaultString("ExistingPreference").equals("From the local plugin directory"));
}

public void testAddNewPreference() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	
	IPluginDescriptor resPlugin = registry.getPluginDescriptor("addNewPreference");
	Preferences prefs = null;
	Plugin plugin = null;
	try {
		plugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("4.0 core exception activating \"addNewPreference\"");
	}
	prefs = plugin.getPluginPreferences();
	// We should have only one preference
	String[] defaultNames = prefs.defaultPropertyNames();
	String[] prefNames = prefs.propertyNames();
	assertTrue("4.1 One default preferences", defaultNames.length == 1);
	assertTrue("4.2 No explicit preferences", prefNames.length == 0);
	assertTrue("4.3 Default preference name", defaultNames[0].equals("ExistingPreference"));
	assertTrue("4.4 Default preference value",
		prefs.getString("ExistingPreference").equals("From the local plugin directory of addNewPreference"));
	
	// Now add a new preference
	prefs.setValue("newPreference", "From the test, testAddNewPreference");
	plugin.savePluginPreferences();
}

public void testAddNewPreference2() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	
	IPluginDescriptor resPlugin = registry.getPluginDescriptor("addNewPreference");
	Preferences prefs = null;
	Plugin plugin = null;
	try {
		plugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("5.0 core exception activating \"addNewPreference\"");
	}
	prefs = plugin.getPluginPreferences();
	// We should have 2 preferences (one set by default and the
	// other set explicitly in testAddNewPreference
	String[] defaultNames = prefs.defaultPropertyNames();
	String[] prefNames = prefs.propertyNames();
	assertTrue("5.1 One default preferences", defaultNames.length == 1);
	assertTrue("5.2 One explicit preferences", prefNames.length == 1);
	assertTrue("5.3 Default preference name", defaultNames[0].equals("ExistingPreference"));
	assertTrue("5.4 Explicit preference name", prefNames[0].equals("newPreference"));
	assertTrue("5.5 Default preference value",
		prefs.getDefaultString("ExistingPreference").equals("From the local plugin directory of addNewPreference"));
	assertTrue("5.6 Explicit preference value",
		prefs.getString("newPreference").equals("From the test, testAddNewPreference"));
}

}

