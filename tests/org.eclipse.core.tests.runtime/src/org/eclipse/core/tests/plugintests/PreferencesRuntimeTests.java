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
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.SafeFileInputStream;
import org.eclipse.core.internal.runtime.SafeFileOutputStream;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.PluginFragmentModel;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;

public class PreferencesRuntimeTests extends PrimaryFeaturePreferenceHelperTests {
	
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
	// Preferences in the local area for a fragment of this 
	// plugin
	IPluginDescriptor resPlugin = registry.getPluginDescriptor("fragmentLocalPreferencesPlugin");
	Preferences prefs = null;
	try {
		prefs = resPlugin.getPlugin().getPluginPreferences();
	} catch (CoreException ce) {
		fail("0.1 core exception from getPlugin");
	}
	String[] defaultNames = prefs.defaultPropertyNames();
	String[] prefNames = prefs.propertyNames();
	assertTrue("1.0 One default preferences", defaultNames.length == 1);
	assertTrue("1.1 No explicit preferences", prefNames.length == 0);
	assertTrue("1.2 Default preference name", defaultNames[0].equals("FragmentLocalPreference"));
	assertTrue("1.3 Default preference value",
		prefs.getString("FragmentLocalPreference").equals("From the local fragment directory"));
	
		
	//-----------------------------------------------------------	
	// Preferences in the plugin state area and in the local
	// area for a fragment of this plugin
	resPlugin = registry.getPluginDescriptor("stateAndFragmentLocalPlugin");
	Plugin runtimePlugin = null;
	try {
		runtimePlugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("0.2 core exception from getPlugin");
	}
	String destString = runtimePlugin.getStateLocation().append("pref_store.ini").toOSString();
	// get rid of the protocol
	String sourceName = ((PluginDescriptor)resPlugin).getFragments()[0].getLocation() + "originalpref_store.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	copyFile(sourceName, destString);
	prefs = null;
	prefs = runtimePlugin.getPluginPreferences();
	defaultNames = prefs.defaultPropertyNames();
	prefNames = prefs.propertyNames();
	assertTrue("2.0 Two default preferences", defaultNames.length == 2);
	assertTrue("2.1 Two explicit preferences", prefNames.length == 2);
	// Do we have the right names for the explicit preferences?
	boolean foundStateLocalPreference = false;
	boolean foundCommonPreference = false;
	for (int i = 0; i < prefNames.length; i++) {
		if (prefNames[i].equals("StateLocalPreference"))
			foundStateLocalPreference = true;
		else if (prefNames[i].equals("commonStateAndFragmentLocalPreference"))
			foundCommonPreference = true;
	}
	assertTrue("2.2 Got right default preference names",
		foundStateLocalPreference && foundCommonPreference);
	// Do we have the right names for the default preferences?
	boolean foundPluginLocalPreference = false;
	foundCommonPreference = false;
	for (int i = 0; i < defaultNames.length; i++) {
		if (defaultNames[i].equals("FragmentLocalPreference"))
			foundPluginLocalPreference = true;
		else if (defaultNames[i].equals("commonStateAndFragmentLocalPreference"))
			foundCommonPreference = true;
	}
	assertTrue("2.3 Got right explicit preference names",
		foundPluginLocalPreference && foundCommonPreference);
	// Check preference values
	assertTrue("2.4 StateLocalPreference value",
		prefs.getString("StateLocalPreference").equals("From the plugin state area of the fragment for stateAndFragmentLocal"));
	assertTrue("2.5 FragmentLocalPreference value",
		prefs.getString("FragmentLocalPreference").equals("From the local fragment directory of stateAndFragmentLocal"));
	assertTrue("2.6 commonStateAndFragmentLocalPreference value",
		prefs.getString("commonStateAndFragmentLocalPreference").equals("Common preference from the plugin state area for stateAndFragmentLocal"));

	//-----------------------------------------------------------	
	// Preferences in the local area for a fragment of this plugin
	// and the primary feature
	resPlugin = registry.getPluginDescriptor("fragmentLocalAndPrimaryFeaturePlugin");
	sourceName = ((PluginDescriptor)resPlugin).getFragments()[0].getLocation() + "/originalplugin_customization.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	IPluginDescriptor primaryFeatureDescriptor = registry.getPluginDescriptor(primaryFeaturePluginId);
	destString = ((PluginDescriptor)primaryFeatureDescriptor).getLocation() +
		"plugin_customization.ini";
	destString = destString.substring(destString.indexOf(':') + 1);
	copyFile(sourceName, destString);
	prefs = null;
	try {
		prefs = resPlugin.getPlugin().getPluginPreferences();
	} catch (CoreException ce) {
		fail("0.3 core exception from getPlugin");
	}
	defaultNames = prefs.defaultPropertyNames();
	prefNames = prefs.propertyNames();
	assertTrue("3.0 Three default preferences", defaultNames.length == 3);
	assertTrue("3.1 No explicit preferences", prefNames.length == 0);
	// Do we have the right names for the default preferences?
	foundPluginLocalPreference = false;
	foundCommonPreference = false;
	boolean foundPrimaryFeaturePreference = false;
	for (int i = 0; i < defaultNames.length; i++) {
		if (defaultNames[i].equals("FragmentLocalPreference"))
			foundPluginLocalPreference = true;
		else if (defaultNames[i].equals("commonFragmentLocalAndPrimaryFeaturePreference"))
			foundCommonPreference = true;
		else if (defaultNames[i].equals("PrimaryFeaturePreference"))
			foundPrimaryFeaturePreference = true;
	}
	assertTrue("3.2 Got right default preference names",
		foundPluginLocalPreference && foundCommonPreference && foundPrimaryFeaturePreference);
	// Check preference values
	assertTrue("3.3 FragmentLocalPreference value",
		prefs.getString("FragmentLocalPreference").equals("From the local fragment directory of the plugin fragmentLocalAndPrimaryFeaturePlugin"));
	assertTrue("3.4 PrimaryFeaturePreference value",
		prefs.getString("PrimaryFeaturePreference").equals("From the primary feature plugin directory via the plugin fragmentLocalAndPrimaryFeaturePlugin"));
	assertTrue("3.5 commonFragmentLocalAndPrimaryFeaturePreference value",
		prefs.getString("commonFragmentLocalAndPrimaryFeaturePreference").equals("Common preference from the primary feature of plugin fragmentLocalAndPrimaryFeaturePlugin"));
	
	//-----------------------------------------------------------	
	// Preferences in the plugin state area, the local area for a
	// fragment of this plugin and the primary feature 
	resPlugin = registry.getPluginDescriptor("stateAndFragmentLocalAndPrimaryFeaturePreferencesPlugin");
	runtimePlugin = null;
	try {
		runtimePlugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("0.4 core exception from getPlugin");
	}
	destString = runtimePlugin.getStateLocation().append("pref_store.ini").toOSString();
	// get rid of the protocol
	sourceName = ((PluginDescriptor)resPlugin).getFragments()[0].getLocation() + "/originalpref_store.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	copyFile(sourceName, destString);
	sourceName = ((PluginDescriptor)resPlugin).getFragments()[0].getLocation() + "/originalplugin_customization.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	destString = ((PluginDescriptor)primaryFeatureDescriptor).getLocation() +
		"plugin_customization.ini";
	destString = destString.substring(destString.indexOf(':') + 1);
	copyFile(sourceName, destString);
	prefs = null;
	prefs = runtimePlugin.getPluginPreferences();
	defaultNames = prefs.defaultPropertyNames();
	prefNames = prefs.propertyNames();
	assertTrue("4.0 Three default preferences", defaultNames.length == 3);
	assertTrue("4.1 Two explicit preferences", prefNames.length == 2);
	// Do we have the right names for the explicit preferences?
	foundStateLocalPreference = false;
	foundCommonPreference = false;
	for (int i = 0; i < prefNames.length; i++) {
		if (prefNames[i].equals("StateLocalPreference"))
			foundStateLocalPreference = true;
		else if (prefNames[i].equals("commonStateAndFragmentLocalAndPrimaryFeaturePreference"))
			foundCommonPreference = true;
	}
	assertTrue("4.2 Got right default preference names",
		foundStateLocalPreference && foundCommonPreference);
	// Do we have the right names for the default preferences?
	foundPluginLocalPreference = false;
	foundCommonPreference = false;
	foundPrimaryFeaturePreference = false;
	for (int i = 0; i < defaultNames.length; i++) {
		if (defaultNames[i].equals("FragmentLocalPreference"))
			foundPluginLocalPreference = true;
		else if (defaultNames[i].equals("commonStateAndFragmentLocalAndPrimaryFeaturePreference"))
			foundCommonPreference = true;
		else if (defaultNames[i].equals("PrimaryFeaturePreference"))
			foundPrimaryFeaturePreference = true;
	}
	assertTrue("4.3 Got right default preference names",
		foundPluginLocalPreference && foundCommonPreference && foundPrimaryFeaturePreference);
	// Check preference values
	assertTrue("4.3 StateLocalPreference value",
		prefs.getString("StateLocalPreference").equals("From the plugin state area of plugin stateAndFragmentLocalAndPrimaryFeaturePreferencesPlugin"));
	assertTrue("4.4 FragmentLocalPreference value",
		prefs.getString("FragmentLocalPreference").equals("From the local fragment directory of plugin stateAndFragmentLocalAndPrimaryFeaturePreferencesPlugin"));
	assertTrue("4.5 PrimaryFeaturePreference value",
		prefs.getString("PrimaryFeaturePreference").equals("From the primary feature plugin directory via the plugin stateAndFragmentLocalAndPrimaryFeaturePreferencesPlugin"));
	assertTrue("4.6 commonStateAndFragmentLocalAndPrimaryFeaturePreference value",
		prefs.getString("commonStateAndFragmentLocalAndPrimaryFeaturePreference").equals("Common preference from the plugin state area of plugin stateAndFragmentLocalAndPrimaryFeaturePreferencesPlugin"));
}
	
private boolean buildPluginTestFile(String pluginId, String pluginSubDir, String basePrefName,
	String untransValue, String transValue, String prefixId) {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor(pluginId);
	assertNotNull("0.1 Can't find plugin " + pluginId, plugin);
	URL pluginRoot = plugin.find(new Path("./"));
	File transfile = null;
	if (pluginSubDir != null) {
		transfile = new File(pluginRoot.getFile() + pluginSubDir);
		transfile.mkdirs();
	} else 
		transfile = new File(pluginRoot.getFile());
	File rootfile = new File(pluginRoot.getFile());
	// Now build up the preferences file
	File prefFile = new File(rootfile, basePrefName + ".ini");
	try {
		FileOutputStream fs = new FileOutputStream(prefFile);
		PrintWriter w = new PrintWriter(fs);
		try {
			if (prefixId != null) {
				w.println(prefixId + "/TranslatedPreference=%translateThis");
				w.println(prefixId + "/UntranslatedPreference = " + untransValue);
			} else {
				w.println("TranslatedPreference=%translateThis");
				w.println("UntranslatedPreference = " + untransValue);
			}
			w.flush();
		} finally {
			w.close();
		}
	} catch (IOException ioe) {
		System.out.println ("Unable to write to preference file " + prefFile.getPath());
		return false;
	}
	// Now build up the translation file
	File prefTransFile = new File(transfile, basePrefName + ".properties");
	try {
		FileOutputStream fs = new FileOutputStream(prefTransFile);
		PrintWriter w = new PrintWriter(fs);
		try {
			w.println("translateThis = " + transValue);
			w.flush();
		} finally {
			w.close();
		}
	} catch (IOException ioe) {
		System.out.println ("Unable to write to translation file " + prefTransFile.getPath());
		return false;
	}
	return true;
}

private boolean buildFragmentTestFile(String pluginId,
	String pluginSubDir, String basePrefName,
	String untransValue, String transValue, String prefixId) {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor(pluginId);
	assertNotNull("0.1 Can't find plugin " + pluginId, plugin);
	PluginFragmentModel[] fragments = ((PluginDescriptor)plugin).getFragments();
	PluginFragmentModel testFragment = null;
	for (int i = 0; (i < fragments.length) && (testFragment == null); i++) {
		if (fragments[i].getId().equals("primaryFeatureFragmentTranslations"))
			testFragment = fragments[i];
	}
	// Should only be one fragment
	assertNotNull("0.2 Can't find test fragment", testFragment);
	URL fragmentRoot = null;
	try {
		fragmentRoot = new URL(testFragment.getLocation());
	} catch (MalformedURLException badURL) {
		return false;
	}
	URL rootDirectory = null;
	try {
		rootDirectory = new URL(((PluginDescriptor)plugin).getLocation());
	} catch (MalformedURLException badURL) {
		return false;
	}
	File file = null;
	if (pluginSubDir != null) {
		file = new File(fragmentRoot.getFile() + pluginSubDir);
		file.mkdirs();
	} else 
		file = new File(fragmentRoot.getFile());
	// Now build up the preference file
	File prefFile = new File(new File(rootDirectory.getFile()), basePrefName + ".ini");
	try {
		FileOutputStream fs = new FileOutputStream(prefFile);
		PrintWriter w = new PrintWriter(fs);
		try {
			if (prefixId != null) {
				w.println(prefixId + "/TranslatedPreference=%translateThis");
				w.println(prefixId + "/UntranslatedPreference = " + untransValue);
			} else {
				w.println("TranslatedPreference=%translateThis");
				w.println("UntranslatedPreference = " + untransValue);
			}
			w.flush();
		} finally {
			w.close();
		}
	} catch (IOException ioe) {
		System.out.println ("Unable to write to preference file " + prefFile.getPath());
		return false;
	}
	// Now build up the translation file
	File prefTransFile = new File(file, basePrefName + ".properties");
	try {
		FileOutputStream fs = new FileOutputStream(prefTransFile);
		PrintWriter w = new PrintWriter(fs);
		try {
			w.println("translateThis = " + transValue);
			w.flush();
		} finally {
			w.close();
		}
	} catch (IOException ioe) {
		System.out.println ("Unable to write to translation file " + prefTransFile.getPath());
		return false;
	}
	return true;
}

private String buildNLSubdirectory(int chopSegment) {
	// Build up nl related directories and test files
	String nl = BootLoader.getNL();
	nl = nl.replace('_', '/');
	// Chop off the number of segments stated
	int i = chopSegment;
	while (nl.length() > 0 && i > 0) {
		i--;
		int idx = nl.lastIndexOf('/');
		if (idx != -1)
			nl = nl.substring(0, idx);
		else 
			nl = "";
	}
	if ((nl.length() == 0) && (i > 0))
		// We couldn't get rid of all the segments we wanted to
		return null;
	return nl;
}

private Preferences getPreferences (String pluginId, String errorPrefix) {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor(pluginId);
	assertNotNull(errorPrefix + ".0 Can't find plugin " + pluginId);
	Plugin runtimePlugin = null;
	try {
		runtimePlugin = plugin.getPlugin();
	} catch (CoreException ce) {
		fail(errorPrefix + ".1 Can't activate plugin " + pluginId + ce.toString());
	}
	assertNotNull(errorPrefix + ".1 Can't activate plugin " + pluginId);
	Preferences prefs = runtimePlugin.getPluginPreferences();
	return prefs;
}

private void verifyPreferences (String pluginId, Preferences prefs,
	String errorPrefix, String untransValue, String transValue) {
	String[] defaultNames = prefs.defaultPropertyNames();
	String[] explicitNames = prefs.propertyNames();
	assertEquals(errorPrefix + ".0 Two default preferences", defaultNames.length, 2);
	assertEquals(errorPrefix + ".1 No explicit preferences", explicitNames.length, 0);
	boolean translatedPref = false;
	boolean untranslatedPref = false;
	for (int i = 0; i < defaultNames.length; i++) {
		if (defaultNames[i].equals("TranslatedPreference"))
			translatedPref = true;
		else if (defaultNames[i].equals("UntranslatedPreference"))
			untranslatedPref = true;
	}
	assertTrue(errorPrefix + ".2 Didn't find preference \"TranslatedPreference\"",
		translatedPref);
	assertTrue(errorPrefix + ".3 Didn't find preference \"UntranslatedPreference\"",
		untranslatedPref);
	// Now check the preference values
	String value = prefs.getString("TranslatedPreference");
	assertEquals(errorPrefix + ".4 Value of \"TranslatedPreference\"",
		value, transValue);
	value = prefs.getString("UntranslatedPreference");
	assertEquals(errorPrefix + ".5 Value of \"UntranslatedPreference\"",
		value, untransValue);
}

private void cleanupTestFiles(String pluginId, String fragmentId, String testDirectory, String prefFileName) {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor(pluginId);
	URL rootDir = null;
	if (fragmentId == null) {
		rootDir = plugin.find(new Path("./"));
	} else {
		PluginFragmentModel[] fragments = ((PluginDescriptor)plugin).getFragments();
		if (fragments.length != 1)
			return;
		try {
			rootDir = new URL(fragments[0].getLocation());
		} catch (MalformedURLException badURL) {
			return;
		}
	}
	if (rootDir == null)
		return;
	String rootString = rootDir.getFile();
	if (testDirectory != null)
		deleteDirectory(new File(rootString + "/" + testDirectory));
	new File(rootString + "/" + prefFileName).delete();
}

public void testPFTranslationPluginRoot() {
	// Test translations in the primary feature preferences when
	// the translation file is in the root plugin directory.
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	IPluginDescriptor primaryFeatureDescriptor = Platform.getPluginRegistry().getPluginDescriptor(primaryFeaturePluginId);
	assertNotNull("Can't find primary feature plugin " + primaryFeatureDescriptor);
	String rootDirectory = null;
	try {
		rootDirectory = new URL(((PluginDescriptor)primaryFeatureDescriptor).getLocation()).getFile();
	} catch (MalformedURLException badURL) {
		fail("Bad URL created for " + ((PluginDescriptor)primaryFeatureDescriptor).getLocation());
	}
	try {
		String untransValue = "Test string from " + primaryFeaturePluginId + " plugin root directory.";
		String transValue = "Translated string from " + primaryFeaturePluginId + " plugin root directory.";
		IPath sourcePath = new Path(rootDirectory).append("plugin_customization.ini");
		File prefFile = sourcePath.toFile();
		try {
			FileOutputStream fs = new FileOutputStream(prefFile);
			PrintWriter w = new PrintWriter(fs);
			try {
				w.println("primaryFeatureTranslations/TranslatedPreference=%translateThis");
				w.println("primaryFeatureTranslations/UntranslatedPreference = " + untransValue);
				w.flush();
			} finally {
				w.close();
			}
		} catch (IOException ioe) {
			fail ("Unable to write to preference file " + prefFile.getPath());
		}
		sourcePath = new Path(rootDirectory).append("plugin_customization.properties");
		File prefTransFile = new File(sourcePath.toString());
		try {
			FileOutputStream fs = new FileOutputStream(prefTransFile);
			PrintWriter w = new PrintWriter(fs);
			try {
				w.println("translateThis = " + transValue);
				w.flush();
			} finally {
				w.close();
			}
		} catch (IOException ioe) {
			fail ("Unable to write to translation file " + prefTransFile.getPath());
		}
		Preferences prefs = getPreferences("primaryFeatureTranslations", "1");
		verifyPreferences ("primaryFeatureTranslations", prefs, "1",
			untransValue, transValue);
	} finally {
			cleanupTestFiles(primaryFeaturePluginId, null, null, "plugin_customization.ini");
			cleanupTestFiles(primaryFeaturePluginId, null, null, "plugin_customization.properties");
	}
}

public void testPFTranslationsPluginNL1 () {
	// Test translations in the primary feature preferences when
	// the translation file is in the most specific nl directory
	// below the primary feature plugin.
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	try {
		String subDirectory = buildNLSubdirectory(0);
		String untransValue = "Test string from " + primaryFeaturePluginId + " plugin " + subDirectory + " directory.";
		String transValue = "Translated string from " + primaryFeaturePluginId + " plugin " + subDirectory + " directory.";
		if (!buildPluginTestFile(primaryFeaturePluginId,
			"nl/" + subDirectory, "plugin_customization", untransValue,
			transValue, "primaryFeatureTranslations"))
			// We don't expect this one to fail
			fail ("0.2 Could not build nl preference data for testPFTranslationsPluginNL1");
		else {
			Preferences prefs = getPreferences("primaryFeatureTranslations", "2");
			verifyPreferences("primaryFeatureTranslations", prefs, "2", untransValue, transValue);
		}
	} finally {
			cleanupTestFiles(primaryFeaturePluginId, null, "nl", "plugin_customization.ini");
	}
}

public void testPFTranslationsPluginNL2() {
	// Test translations in the primary feature preferences when
	// the translation file is in the nl/<first_segment> directory
	// below the primary feature plugin.  Note that if there is
	// only one segment to the locale string, the test 
	// testPFTranslationsPluginNL1 will have already done
	// this.  In this case, this test will do nothing.
	
	String nl = BootLoader.getNL();
	// there is at least one segment
	int localeSegments = 1;
	int i = nl.indexOf('_');
	while (i != -1) {
		localeSegments++;
		nl = nl.substring(i+1);
		i = nl.indexOf('_');
	}
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	if (localeSegments > 1) {
		try {
			String subDirectory = buildNLSubdirectory(1);
			String untransValue = "Test string from " + primaryFeaturePluginId + " plugin " + subDirectory + " directory.";
			String transValue = "Translated string from " + primaryFeaturePluginId + " plugin " + subDirectory + " directory.";
			if (!buildPluginTestFile(primaryFeaturePluginId,
				"nl/" + subDirectory, "plugin_customization", untransValue,
				transValue, "primaryFeatureTranslations"))
				// We don't expect this one to fail
				fail ("0.3 Could not build nl preference data for testPFTranslationsPluginNL2");
			else {
				Preferences prefs = getPreferences("primaryFeatureTranslations", "3");
				verifyPreferences("primaryFeatureTranslations", prefs, "3", untransValue, transValue);
			}
		} finally {
			cleanupTestFiles(primaryFeaturePluginId, null, "nl", "plugin_customization.ini");
		}
	}
}

public void testPFTranslationsPluginNL3() {
	// Test translations in the primary feature preferences when
	// the translation file is in the nl/ directory
	// below the primary feature plugin.  Note that these
	// translations should not be found.
	String nl = BootLoader.getNL();
	// there is at least one segment
	int localeSegments = 1;
	int i = nl.indexOf('_');
	while (i != -1) {
		localeSegments++;
		nl = nl.substring(i+1);
		i = nl.indexOf('_');
	}
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	try {
		String subDirectory = buildNLSubdirectory(localeSegments);
		String untransValue = "Test string from " + primaryFeaturePluginId + " plugin " + subDirectory + " directory.";
		String transValue = "Translated string from " + primaryFeaturePluginId + " plugin " + subDirectory + " directory.";
		if (buildPluginTestFile(primaryFeaturePluginId,
			"nl/" + subDirectory, "plugin_customization", untransValue,
			transValue, "primaryFeatureTranslations"))
		if (subDirectory == null)
			// We don't expect this one to fail
			fail ("0.4 Could not build nl preference data for testPFTranslationsPluginNL3");
		else {
			Preferences prefs = getPreferences("primaryFeatureTranslations", "4");
				verifyPreferences("primaryFeatureTranslations", prefs, "4", untransValue, "%translateThis");
		}
	} finally {
		cleanupTestFiles(primaryFeaturePluginId, null, "nl", "plugin_customization.ini");
	}
}

public void testPFTranslationFragmentRoot() {
	// Test translations in the primary feature preferences when
	// the translation file is in the root fragment directory.
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	IPluginDescriptor primaryFeatureDescriptor = Platform.getPluginRegistry().getPluginDescriptor(primaryFeaturePluginId);
	assertNotNull("Can't find primary feature plugin " + primaryFeatureDescriptor);
	String rootDirectory = null;
	String fragmentRootDirectory = null;
	PluginFragmentModel[] fragments = ((PluginDescriptor)primaryFeatureDescriptor).getFragments();
	PluginFragmentModel testFragment = null;
	for (int i = 0; (i < fragments.length) && (testFragment == null); i++) {
		if (fragments[i].getId().equals("primaryFeatureFragmentTranslations"))
			testFragment = fragments[i];
	}
	assertNotNull("5.0 Can't find test fragment for primary feature", testFragment);
	try {
		fragmentRootDirectory = new URL(testFragment.getLocation()).getFile();
	} catch (MalformedURLException badURL) {
		fail("Bad URL created for " + testFragment.getLocation());
	}
	try {
		rootDirectory = new URL(((PluginDescriptor)primaryFeatureDescriptor).getLocation()).getFile();
	} catch (MalformedURLException badURL) {
		fail("Bad URL created for " + ((PluginDescriptor)primaryFeatureDescriptor).getLocation());
	}
	try {
		String untransValue = "Test string from " + primaryFeaturePluginId + " plugin root directory.";
		String transValue = "Translated string from " + primaryFeaturePluginId + " fragment root directory.";
		IPath sourcePath = new Path(rootDirectory).append("plugin_customization.ini");
		File prefFile = sourcePath.toFile();
		try {
			FileOutputStream fs = new FileOutputStream(prefFile);
			PrintWriter w = new PrintWriter(fs);
			try {
				w.println("primaryFeatureTranslations/TranslatedPreference=%translateThis");
				w.println("primaryFeatureTranslations/UntranslatedPreference = " + untransValue);
				w.flush();
			} finally {
				w.close();
			}
		} catch (IOException ioe) {
			fail ("Unable to write to preference file " + prefFile.getPath());
		}
		sourcePath = new Path(fragmentRootDirectory).append("plugin_customization.properties");
		File prefTransFile = new File(sourcePath.toString());
		try {
			FileOutputStream fs = new FileOutputStream(prefTransFile);
			PrintWriter w = new PrintWriter(fs);
			try {
				w.println("translateThis = " + transValue);
				w.flush();
			} finally {
				w.close();
			}
		} catch (IOException ioe) {
			fail ("Unable to write to translation file " + prefTransFile.getPath());
		}
		Preferences prefs = getPreferences("primaryFeatureTranslations", "5");
		verifyPreferences ("primaryFeatureTranslations", prefs, "5",
			untransValue, transValue);
	} finally {
			cleanupTestFiles(primaryFeaturePluginId, null, null, "plugin_customization.ini");
			cleanupTestFiles(primaryFeaturePluginId, "primaryFeatureFragmentTranslations", null, "plugin_customization.properties");
	}
}

public void testPFTranslationsFragmentNL1 () {
	// Test translations in the primary feature preferences when
	// the translation file is in the most specific nl directory
	// below the primary feature fragment.
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	try {
		String subDirectory = buildNLSubdirectory(0);
		String untransValue = "Test string from " + primaryFeaturePluginId + " plugin " + subDirectory + " directory.";
		String transValue = "Translated string from " + primaryFeaturePluginId + " fragment " + subDirectory + " directory.";
		if (!buildFragmentTestFile(primaryFeaturePluginId,
			"nl/" + subDirectory, "plugin_customization", untransValue,
			transValue, "primaryFeatureTranslations"))
			// We don't expect this one to fail
			fail ("0.6 Could not build nl preference data for testPrimaryFeatureTranslationsNL1");
		else {
			Preferences prefs = getPreferences("primaryFeatureTranslations", "6");
			verifyPreferences("primaryFeatureTranslations", prefs, "6", untransValue, transValue);
		}
	} finally {
			cleanupTestFiles(primaryFeaturePluginId, "primaryFeatureFragmentTranslations", "nl", "plugin_customization.ini");
	}
}

public void testPFTranslationsFragmentNL2() {
	// Test translations in the primary feature preferences when
	// the translation file is in the nl/<first_segment> directory
	// below the primary feature fragment.  Note that if there is
	// only one segment to the locale string, the test 
	// testPFTranslationsFragmentNL1 will have already done
	// this.  In this case, this test will do nothing.
	
	String nl = BootLoader.getNL();
	// there is at least one segment
	int localeSegments = 1;
	int i = nl.indexOf('_');
	while (i != -1) {
		localeSegments++;
		nl = nl.substring(i+1);
		i = nl.indexOf('_');
	}
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	if (localeSegments > 1) {
		try {
			String subDirectory = buildNLSubdirectory(1);
			String untransValue = "Test string from " + primaryFeaturePluginId + " plugin " + subDirectory + " directory.";
			String transValue = "Translated string from " + primaryFeaturePluginId + " fragment " + subDirectory + " directory.";
			if (!buildFragmentTestFile(primaryFeaturePluginId,
				"nl/" + subDirectory, "plugin_customization", untransValue,
				transValue, "primaryFeatureTranslations"))
				// We don't expect this one to fail
				fail ("0.7 Could not build nl preference data for testPFTranslationsFragmentNL2");
			else {
				Preferences prefs = getPreferences("primaryFeatureTranslations", "7");
				verifyPreferences("primaryFeatureTranslations", prefs, "7", untransValue, transValue);
			}
		} finally {
			cleanupTestFiles(primaryFeaturePluginId, "primaryFeatureFragmentTranslations", "nl", "plugin_customization.ini");
		}
	}
}

public void testPFTranslationsFragmentNL3() {
	// Test translations in the primary feature preferences when
	// the translation file is in the nl/ directory
	// below the primary feature fragment.  Note that these
	// translations should not be found.
	String nl = BootLoader.getNL();
	// there is at least one segment
	int localeSegments = 1;
	int i = nl.indexOf('_');
	while (i != -1) {
		localeSegments++;
		nl = nl.substring(i+1);
		i = nl.indexOf('_');
	}
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	try {
		String subDirectory = buildNLSubdirectory(localeSegments);
		String untransValue = "Test string from " + primaryFeaturePluginId + " plugin " + subDirectory + " directory.";
		String transValue = "Translated string from " + primaryFeaturePluginId + " fragment " + subDirectory + " directory.";
		if (buildFragmentTestFile(primaryFeaturePluginId,
			"nl/" + subDirectory, "plugin_customization", untransValue,
			transValue, "primaryFeatureTranslations"))
		if (subDirectory == null)
			// We don't expect this one to fail
			fail ("0.8 Could not build nl preference data for testPFTranslationsFragmentNL3");
		else {
			Preferences prefs = getPreferences("primaryFeatureTranslations", "8");
				verifyPreferences("primaryFeatureTranslations", prefs, "8", untransValue, "%translateThis");
		}
	} finally {
		cleanupTestFiles(primaryFeaturePluginId, "primaryFeatureFragmentTranslations", "nl", "plugin_customization.ini");
	}
}

public void xtestPluginLocalPreferenceTranslations() {
}
}
