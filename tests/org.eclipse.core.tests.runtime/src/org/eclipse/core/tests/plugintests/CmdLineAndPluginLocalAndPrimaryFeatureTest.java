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
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.SafeFileInputStream;
import org.eclipse.core.internal.runtime.SafeFileOutputStream;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;

public class CmdLineAndPluginLocalAndPrimaryFeatureTest extends PrimaryFeaturePreferenceHelperTests {

public CmdLineAndPluginLocalAndPrimaryFeatureTest() {
	super(null);
}
public CmdLineAndPluginLocalAndPrimaryFeatureTest(String name) {
	super(name);
}

public void testCmdLineAndPluginLocalAndPrimaryFeature() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	
	// Preferences in the file specified by -pluginCustomization
	// command line parameter and in the plugin state area
	IPluginDescriptor resPlugin = registry.getPluginDescriptor("cmdLineAndPluginLocalAndPrimaryFeature");
	Preferences prefs = null;
	String sourceName = ((PluginDescriptor)resPlugin).getLocation() + "/originalplugin_customization.ini";
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
	assertTrue("1.0 Four default preferences", defaultNames.length == 4);
	assertTrue("1.1 No explicit preferences", prefNames.length == 0);
	// Do we have the right names for the default preferences?
	boolean foundCommandLinePreference = false;
	boolean foundPluginLocalPreference = false;
	boolean foundPrimaryFeaturePreference = false;
	boolean foundCommonPreference = false;
	for (int i = 0; i < defaultNames.length; i++) {
		if (defaultNames[i].equals("CommandLinePreference"))
			foundCommandLinePreference = true;
		else if (defaultNames[i].equals("commonCmdLineAndPluginLocalAndPrimaryFeaturePreference"))
			foundCommonPreference = true;
		else if (defaultNames[i].equals("PluginLocalPreference"))
			foundPluginLocalPreference = true;
		else if (defaultNames[i].equals("PrimaryFeaturePreference"))
			foundPrimaryFeaturePreference = true;
	}
	assertTrue("1.2 Got right default preference names",
		foundCommandLinePreference && foundCommonPreference && foundPluginLocalPreference && foundPrimaryFeaturePreference);
	// Check preference values
	assertTrue("1.3 CommandLinePreference value",
		prefs.getString("CommandLinePreference").equals("From the command line specified file via the plugin cmdLineAndPluginLocalAndPrimaryFeature"));
	assertTrue("1.4 commonCmdLineAndPluginLocalAndPrimaryFeaturePreference value",
		prefs.getString("commonCmdLineAndPluginLocalAndPrimaryFeaturePreference").equals("Common preference from the command line via the plugin cmdLineAndPluginLocalAndPrimaryFeature"));
	assertTrue("1.5 PluginLocalPreference value",
		prefs.getString("PluginLocalPreference").equals("From the local plugin directory of the plugin cmdLineAndPluginLocalAndPrimaryFeature"));
	assertTrue("1.6 PrimaryFeaturePreference value",
		prefs.getString("PrimaryFeaturePreference").equals("From the primary feature plugin directory via the plugin cmdLineAndPluginLocalAndPrimaryFeature"));
}
}
