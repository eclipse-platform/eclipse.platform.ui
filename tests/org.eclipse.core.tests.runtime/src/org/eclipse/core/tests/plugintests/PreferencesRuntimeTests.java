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

import java.io.*;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.SafeFileInputStream;
import org.eclipse.core.internal.runtime.SafeFileOutputStream;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;

public class PreferencesRuntimeTests extends WorkspaceSessionTest {
public PreferencesRuntimeTests() {
	super(null);
}
public PreferencesRuntimeTests(String name) {
	super(name);
}

public static void copyFile (String sourceName, String destName) {
	// A crude mechanism for creating a preference file
	SafeFileInputStream sourceIn = null;
	SafeFileOutputStream destOut = null;
	try {
		sourceIn = new SafeFileInputStream(sourceName);
		destOut = new SafeFileOutputStream(destName);
		int buffer = sourceIn.read();
		while (buffer != -1) {
			destOut.write(buffer);
			buffer = sourceIn.read();
		}
	} catch (IOException ioe) {
		fail("0.0 Trouble creating file " + destName + " from original file " + sourceName + ".");
	} finally {
		try {
			sourceIn.close();
			destOut.close();
		} catch (IOException e) {
			fail("0.1 Trouble closing file " + destName + " or " + sourceName + ".");
		}
	}
}

public void testPluginPrefs() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	
	// No preferences for this plugin
	IPluginDescriptor resPlugin = registry.getPluginDescriptor("noPreferencesPlugin");
	Preferences prefs = null;
	try {
		prefs = resPlugin.getPlugin().getPluginPreferences();
	} catch (CoreException ce) {
		fail("0.1 core exception from getPlugin");
	}
	String[] defaultNames = prefs.defaultPropertyNames();
	String[] prefNames = prefs.propertyNames();
	assertTrue("1.0 No default preferences", defaultNames.length == 0);
	assertTrue("1.1 No explicit preferences", prefNames.length == 0);

	//-----------------------------------------------------------	
	// Preferences in the plugin state area
	resPlugin = registry.getPluginDescriptor("stateLocalPreferences");
	// Move the preferences file into position
	Plugin runtimePlugin = null;
	try {
		runtimePlugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("0.2 core exception from getPlugin");
	}
	String destString = runtimePlugin.getStateLocation().append("pref_store.ini").toOSString();
	// get rid of the protocol
	String sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalpref_store.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	copyFile(sourceName, destString);
	prefs = runtimePlugin.getPluginPreferences();
	defaultNames = prefs.defaultPropertyNames();
	prefNames = prefs.propertyNames();
	assertTrue("2.0 No default preferences", defaultNames.length == 0);
	assertTrue("2.1 One explicit preference", prefNames.length == 1);
	assertTrue("2.2 Preference name", prefNames[0].equals("StateLocalPreference"));
	assertTrue("2.3 Preference value",
		prefs.getString("StateLocalPreference").equals("From the plugin state area"));

	//-----------------------------------------------------------	
	// Preferences in the local plugin area
	resPlugin = registry.getPluginDescriptor("pluginLocalPreferences");
	prefs = null;
	try {
		prefs = resPlugin.getPlugin().getPluginPreferences();
	} catch (CoreException ce) {
		fail("0.3 core exception from getPlugin");
	}
	defaultNames = prefs.defaultPropertyNames();
	prefNames = prefs.propertyNames();
	assertTrue("3.0 One default preferences", defaultNames.length == 1);
	assertTrue("3.1 No explicit preferences", prefNames.length == 0);
	assertTrue("3.2 Default preference name", defaultNames[0].equals("PluginLocalPreference"));
	assertTrue("3.3 Default preference value",
		prefs.getString("PluginLocalPreference").equals("From the local plugin directory"));
	
	//-----------------------------------------------------------	
	// Preferences in the primary feature plugin.
	// There are are preferences related to this particular plugin
	// and other plugins
	resPlugin = registry.getPluginDescriptor("primaryFeaturePluginPreferences");
	sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalplugin_customization.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	IPluginDescriptor primaryFeatureDescriptor = registry.getPluginDescriptor(primaryFeaturePluginId);
	String destName = ((PluginDescriptor)primaryFeatureDescriptor).getLocation() +
		"plugin_customization.ini";
	destName = destName.substring(destName.indexOf(':') + 1);
	copyFile(sourceName, destName);
	prefs = null;
	try {
		prefs = resPlugin.getPlugin().getPluginPreferences();
	} catch (CoreException ce) {
		fail("0.4 core exception from getPlugin");
	}
	defaultNames = prefs.defaultPropertyNames();
	prefNames = prefs.propertyNames();
	assertTrue("4.0 One default preference", defaultNames.length == 1);
	assertTrue("4.1 No explicit preferences", prefNames.length == 0);
	assertTrue("4.2 Default preference name", defaultNames[0].equals("PrimaryFeaturePreference"));
	assertTrue("4.3 Default preference value",
		prefs.getString("PrimaryFeaturePreference").equals("From the primary feature plugin directory"));
		
	//-----------------------------------------------------------	
	// Preferences in the plugin state area and in the local
	// plugin area
	resPlugin = registry.getPluginDescriptor("stateAndPluginLocal");
	runtimePlugin = null;
	try {
		runtimePlugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("0.5 core exception from getPlugin");
	}
	destString = runtimePlugin.getStateLocation().append("pref_store.ini").toOSString();
	// get rid of the protocol
	sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalpref_store.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	copyFile(sourceName, destString);
	prefs = null;
	prefs = runtimePlugin.getPluginPreferences();
	defaultNames = prefs.defaultPropertyNames();
	prefNames = prefs.propertyNames();
	assertTrue("5.0 Two default preferences", defaultNames.length == 2);
	assertTrue("5.1 Two explicit preferences", prefNames.length == 2);
	// Do we have the right names for the explicit preferences?
	boolean foundStateLocalPreference = false;
	boolean foundCommonPreference = false;
	for (int i = 0; i < prefNames.length; i++) {
		if (prefNames[i].equals("StateLocalPreference"))
			foundStateLocalPreference = true;
		else if (prefNames[i].equals("commonStateAndPluginLocalPreference"))
			foundCommonPreference = true;
	}
	assertTrue("5.2 Got right default preference names",
		foundStateLocalPreference && foundCommonPreference);
	// Do we have the right names for the default preferences?
	boolean foundPluginLocalPreference = false;
	foundCommonPreference = false;
	for (int i = 0; i < defaultNames.length; i++) {
		if (defaultNames[i].equals("PluginLocalPreference"))
			foundPluginLocalPreference = true;
		else if (defaultNames[i].equals("commonStateAndPluginLocalPreference"))
			foundCommonPreference = true;
	}
	assertTrue("5.3 Got right explicit preference names",
		foundPluginLocalPreference && foundCommonPreference);
	// Check preference values
	assertTrue("5.4 StateLocalPreference value",
		prefs.getString("StateLocalPreference").equals("From the plugin state area of plugin stateAndPluginLocal"));
	assertTrue("5.5 PluginLocalPreference value",
		prefs.getString("PluginLocalPreference").equals("From the local plugin directory of plugin stateAndPluginLocal"));
	assertTrue("5.6 commonStateAndPluginLocalPreference value",
		prefs.getString("commonStateAndPluginLocalPreference").equals("Common preference from the plugin state area of plugin stateAndPluginLocal"));

	//-----------------------------------------------------------
	// Preferences in the plugin state area and the primary 
	// feature plugin
	resPlugin = registry.getPluginDescriptor("stateAndPrimaryFeature");
	runtimePlugin = null;
	try {
		runtimePlugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("0.6 core exception from getPlugin");
	}
	destString = runtimePlugin.getStateLocation().append("pref_store.ini").toOSString();
	// get rid of the protocol
	sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalpref_store.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	copyFile(sourceName, destString);
	sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalplugin_customization.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	destName = ((PluginDescriptor)primaryFeatureDescriptor).getLocation() +
		"plugin_customization.ini";
	destName = destName.substring(destName.indexOf(':') + 1);
	copyFile(sourceName, destName);
	prefs = null;
	prefs = runtimePlugin.getPluginPreferences();
	defaultNames = prefs.defaultPropertyNames();
	prefNames = prefs.propertyNames();
	assertTrue("6.0 Two default preferences", defaultNames.length == 2);
	assertTrue("6.1 Two explicit preferences", prefNames.length == 2);
	// Do we have the right names for the explicit preferences?
	foundStateLocalPreference = false;
	foundCommonPreference = false;
	for (int i = 0; i < prefNames.length; i++) {
		if (prefNames[i].equals("StateLocalPreference"))
			foundStateLocalPreference = true;
		else if (prefNames[i].equals("commonStateAndPrimaryFeaturePreference"))
			foundCommonPreference = true;
	}
	assertTrue("6.2 Got right default preference names",
		foundStateLocalPreference && foundCommonPreference);
	// Do we have the right names for the default preferences?
	boolean foundPrimaryFeaturePreference = false;
	foundCommonPreference = false;
	for (int i = 0; i < defaultNames.length; i++) {
		if (defaultNames[i].equals("PrimaryFeaturePreference"))
			foundPrimaryFeaturePreference = true;
		else if (defaultNames[i].equals("commonStateAndPrimaryFeaturePreference"))
			foundCommonPreference = true;
	}
	assertTrue("6.3 Got right explicit preference names",
		foundPrimaryFeaturePreference && foundCommonPreference);
	// Check preference values
	assertTrue("6.4 StateLocalPreference value",
		prefs.getString("StateLocalPreference").equals("From the plugin state area of plugin stateAndPrimaryFeature"));
	assertTrue("6.5 PrimaryFeaturePreference value",
		prefs.getString("PrimaryFeaturePreference").equals("From the primary feature plugin directory via the plugin stateAndPrimaryFeature"));
	assertTrue("6.6 commonStateAndPluginLocalPreference value",
		prefs.getString("commonStateAndPrimaryFeaturePreference").equals("Common preference from the plugin state area of plugin stateAndPrimaryFeature"));
		
	//-----------------------------------------------------------	
	// Preferences in the local plugin area and the primary feature
	// plugin
	resPlugin = registry.getPluginDescriptor("pluginLocalAndPrimaryFeaturePreferences");
	sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalplugin_customization.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	destName = ((PluginDescriptor)primaryFeatureDescriptor).getLocation() +
		"plugin_customization.ini";
	destName = destName.substring(destName.indexOf(':') + 1);
	copyFile(sourceName, destName);
	prefs = null;
	try {
		prefs = resPlugin.getPlugin().getPluginPreferences();
	} catch (CoreException ce) {
		fail("0.7 core exception from getPlugin");
	}
	defaultNames = prefs.defaultPropertyNames();
	prefNames = prefs.propertyNames();
	assertTrue("7.0 Three default preferences", defaultNames.length == 3);
	assertTrue("7.1 No explicit preferences", prefNames.length == 0);
	// Do we have the right names for the default preferences?
	foundPluginLocalPreference = false;
	foundCommonPreference = false;
	foundPrimaryFeaturePreference = false;
	for (int i = 0; i < defaultNames.length; i++) {
		if (defaultNames[i].equals("PluginLocalPreference"))
			foundPluginLocalPreference = true;
		else if (defaultNames[i].equals("commonPluginLocalAndPrimaryFeaturePreference"))
			foundCommonPreference = true;
		else if (defaultNames[i].equals("PrimaryFeaturePreference"))
			foundPrimaryFeaturePreference = true;
	}
	assertTrue("7.2 Got right default preference names",
		foundPluginLocalPreference && foundCommonPreference && foundPrimaryFeaturePreference);
	// Check preference values
	assertTrue("7.3 PluginLocalPreference value",
		prefs.getString("PluginLocalPreference").equals("From the local plugin directory of the plugin pluginLocalAndPrimaryFeaturePreferences"));
	assertTrue("7.4 PrimaryFeaturePreference value",
		prefs.getString("PrimaryFeaturePreference").equals("From the primary feature plugin directory via the plugin pluginLocalAndPrimaryFeaturePreferences"));
	assertTrue("7.5 commonPluginLocalAndPrimaryFeaturePreference value",
		prefs.getString("commonPluginLocalAndPrimaryFeaturePreference").equals("Common preference from the primary feature of plugin pluginLocalAndPrimaryFeaturePreferences"));
	
	//-----------------------------------------------------------	
	// Preferences in the plugin state area, the local plugin area
	// and the primary feature plugin
	resPlugin = registry.getPluginDescriptor("stateAndPluginLocalAndPrimaryFeature");
	runtimePlugin = null;
	try {
		runtimePlugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("0.8 core exception from getPlugin");
	}
	destString = runtimePlugin.getStateLocation().append("pref_store.ini").toOSString();
	// get rid of the protocol
	sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalpref_store.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	copyFile(sourceName, destString);
	sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalplugin_customization.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	destName = ((PluginDescriptor)primaryFeatureDescriptor).getLocation() +
		"plugin_customization.ini";
	destName = destName.substring(destName.indexOf(':') + 1);
	copyFile(sourceName, destName);
	prefs = null;
	prefs = runtimePlugin.getPluginPreferences();
	defaultNames = prefs.defaultPropertyNames();
	prefNames = prefs.propertyNames();
	assertTrue("8.0 Three default preferences", defaultNames.length == 3);
	assertTrue("8.1 Two explicit preferences", prefNames.length == 2);
	// Do we have the right names for the explicit preferences?
	foundStateLocalPreference = false;
	foundCommonPreference = false;
	for (int i = 0; i < prefNames.length; i++) {
		if (prefNames[i].equals("StateLocalPreference"))
			foundStateLocalPreference = true;
		else if (prefNames[i].equals("commonStateAndPluginLocalAndPrimaryFeaturePreference"))
			foundCommonPreference = true;
	}
	assertTrue("8.2 Got right default preference names",
		foundStateLocalPreference && foundCommonPreference);
	// Do we have the right names for the default preferences?
	foundPluginLocalPreference = false;
	foundCommonPreference = false;
	foundPrimaryFeaturePreference = false;
	for (int i = 0; i < defaultNames.length; i++) {
		if (defaultNames[i].equals("PluginLocalPreference"))
			foundPluginLocalPreference = true;
		else if (defaultNames[i].equals("commonStateAndPluginLocalAndPrimaryFeaturePreference"))
			foundCommonPreference = true;
		else if (defaultNames[i].equals("PrimaryFeaturePreference"))
			foundPrimaryFeaturePreference = true;
	}
	assertTrue("8.3 Got right default preference names",
		foundPluginLocalPreference && foundCommonPreference && foundPrimaryFeaturePreference);
	// Check preference values
	assertTrue("8.3 StateLocalPreference value",
		prefs.getString("StateLocalPreference").equals("From the plugin state area of plugin stateAndPluginLocalAndPrimaryFeature"));
	assertTrue("8.4 PluginLocalPreference value",
		prefs.getString("PluginLocalPreference").equals("From the local plugin directory of plugin stateAndPluginLocalAndPrimaryFeature"));
	assertTrue("8.5 PrimaryFeaturePreference value",
		prefs.getString("PrimaryFeaturePreference").equals("From the primary feature plugin directory via the plugin stateAndPluginLocalAndPrimaryFeature"));
	assertTrue("8.6 commonStateAndPluginLocalAndPrimaryFeaturePreference value",
		prefs.getString("commonStateAndPluginLocalAndPrimaryFeaturePreference").equals("Common preference from the plugin state area of plugin stateAndPluginLocalAndPrimaryFeature"));
}

public void testFragmentPrefs() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	
	//-----------------------------------------------------------	
	// No preferences for this plugin
//	IPluginDescriptor resPlugin = registry.getPluginDescriptor("noPreferencesPlugin");
//	Preferences prefs = null;
//	try {
//		prefs = resPlugin.getPlugin().getPluginPreferences();
//	} catch (CoreException ce) {
//		fail("1.0 core exception from getPlugin");
//	}
//	String[] defaultNames = prefs.defaultPropertyNames();
//	String[] prefNames = prefs.propertyNames();
//	assertTrue("1.1 No default preferences", defaultNames.length == 0);
//	assertTrue("1.2 No explicit preferences", prefNames.length == 0);

	//-----------------------------------------------------------	
	// Preferences in the plugin state area
//	resPlugin = registry.getPluginDescriptor("defaultLocalPreferences");
//	// Move the preferences file into position
//	Plugin runtimePlugin = null;
//	try {
//		runtimePlugin = resPlugin.getPlugin();
//	} catch (CoreException ce) {
//		fail("2.0 core exception from getPlugin");
//	}
//	String destString = runtimePlugin.getStateLocation().append("pref_store.ini").toOSString();
//	// get rid of the protocol
//	String sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalpref_store.ini";
//	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
//	copyFile(sourceName, destString);
//	prefs = runtimePlugin.getPluginPreferences();
//	defaultNames = prefs.defaultPropertyNames();
//	prefNames = prefs.propertyNames();
//	assertTrue("2.1 No default preferences", defaultNames.length == 0);
//	assertTrue("2.2 One explicit preference", prefNames.length == 1);
//	assertTrue("2.3 Preference name", prefNames[0].equals("DefaultLocalPreference"));
//	assertTrue("2.4 Preference value",
//		prefs.getString("DefaultLocalPreference").equals("From the plugin state area"));

	//-----------------------------------------------------------	
	// Preferences in the local plugin area for a fragment of this 
	// plugin
//	resPlugin = registry.getPluginDescriptor("pluginLocalPreferences");
//	prefs = null;
//	try {
//		prefs = resPlugin.getPlugin().getPluginPreferences();
//	} catch (CoreException ce) {
//		fail("0.3 core exception from getPlugin");
//	}
//	defaultNames = prefs.defaultPropertyNames();
//	prefNames = prefs.propertyNames();
//	assertTrue("3.0 One default preferences", defaultNames.length == 1);
//	assertTrue("3.1 No explicit preferences", prefNames.length == 0);
//	assertTrue("3.2 Default preference name", defaultNames[0].equals("PluginLocalPreference"));
//	assertTrue("3.3 Default preference value",
//		prefs.getString("PluginLocalPreference").equals("From the local plugin directory"));
	
		
	//-----------------------------------------------------------	
	// Preferences in the plugin state area and in the local
	// plugin area

	//-----------------------------------------------------------
	// Preferences in the plugin state area and the primary 
	// feature plugin
		
	//-----------------------------------------------------------	
	// Preferences in the local plugin area and the primary feature
	// plugin
	
	//-----------------------------------------------------------	
	// Preferences in the plugin state area, the local plugin area
	// and the primary feature plugin
}
	
}
