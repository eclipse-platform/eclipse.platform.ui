package org.eclipse.core.tests.plugintests;

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
import java.io.*;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.SafeFileInputStream;
import org.eclipse.core.internal.runtime.SafeFileOutputStream;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;

public class CmdLineAndStateAndPrimaryFeatureTest extends PrimaryFeaturePreferenceHelperTests {

public CmdLineAndStateAndPrimaryFeatureTest() {
	super(null);
}
public CmdLineAndStateAndPrimaryFeatureTest(String name) {
	super(name);
}

public void testCmdLineAndStateAndPrimaryFeature() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	
	IPluginDescriptor resPlugin = registry.getPluginDescriptor("cmdLineAndStateAndPrimaryFeature");
	Preferences prefs = null;
	Plugin runtimePlugin = null;
	try {
		runtimePlugin = resPlugin.getPlugin();
	} catch (CoreException ce) {
		fail("0.1 core exception from getPlugin");
	}
	String destString = runtimePlugin.getStateLocation().append("pref_store.ini").toOSString();
	// get rid of the protocol
	String sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalpref_store.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	PreferencesRuntimeTests.copyFile(sourceName, destString);
	sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalplugin_customization.ini";
	sourceName = sourceName.substring(sourceName.indexOf(':') + 1);
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	IPluginDescriptor primaryFeatureDescriptor = registry.getPluginDescriptor(primaryFeaturePluginId);
	String destName = ((PluginDescriptor)primaryFeatureDescriptor).getLocation() +
		"plugin_customization.ini";
	destName = destName.substring(destName.indexOf(':') + 1);
	PreferencesRuntimeTests.copyFile(sourceName, destName);
	try {
		prefs = resPlugin.getPlugin().getPluginPreferences();
	} catch (CoreException ce) {
		fail("0.1 core exception from getPlugin");
	}
	String[] defaultNames = prefs.defaultPropertyNames();
	String[] prefNames = prefs.propertyNames();
	assertTrue("1.0 Three default preferences", defaultNames.length == 3);
	assertTrue("1.1 Two explicit preferences", prefNames.length == 2);
	// Do we have the right names for the default preferences?
	boolean foundStatePreference = false;
	boolean foundCommandLinePreference = false;
	boolean foundPrimaryFeaturePreference = false;
	boolean foundCommonPreference = false;
	for (int i = 0; i < defaultNames.length; i++) {
		if (defaultNames[i].equals("CommandLinePreference"))
			foundCommandLinePreference = true;
		else if (defaultNames[i].equals("commonCmdLineAndStateAndPrimaryFeaturePreference"))
			foundCommonPreference = true;
		else if (defaultNames[i].equals("PrimaryFeaturePreference"))
			foundPrimaryFeaturePreference = true;
	}
	assertTrue("1.2 Got right default preference names",
		foundCommandLinePreference && foundCommonPreference && foundPrimaryFeaturePreference);
	// Do we have the right names for the explicit preferences?
	foundStatePreference = false;
	foundCommandLinePreference = false;
	foundPrimaryFeaturePreference = false;
	foundCommonPreference = false;
	for (int i = 0; i < prefNames.length; i++) {
		if (prefNames[i].equals("StateLocalPreference"))
			foundStatePreference = true;
		else if (prefNames[i].equals("commonCmdLineAndStateAndPrimaryFeaturePreference"))
			foundCommonPreference = true;
	}
	assertTrue("1.3 Got right explicit preference names",
		foundStatePreference && foundCommonPreference);
	// Check preference values
	assertTrue("1.4 CommandLinePreference value",
		prefs.getString("CommandLinePreference").equals("From the command line specified file via the plugin cmdLineAndStateAndPrimaryFeature"));
	assertTrue("1.5 commonCmdLineAndPrimaryFeaturePreference value",
		prefs.getString("commonCmdLineAndStateAndPrimaryFeaturePreference").equals("Common preference from the plugin state area of plugin cmdLineAndStateAndPrimaryFeature"));
	assertTrue("1.6 PrimaryFeaturePreference value",
		prefs.getString("PrimaryFeaturePreference").equals("From the primary feature plugin directory via the plugin cmdLineAndStateAndPrimaryFeature"));
	assertTrue("1.7 StatePreference value",
		prefs.getString("StateLocalPreference").equals("From the plugin state area of plugin cmdLineAndStateAndPrimaryFeature"));
}
}
