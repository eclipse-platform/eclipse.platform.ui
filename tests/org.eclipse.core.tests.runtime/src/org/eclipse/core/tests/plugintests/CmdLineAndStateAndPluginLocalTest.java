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

public class CmdLineAndStateAndPluginLocalTest extends WorkspaceSessionTest {

public CmdLineAndStateAndPluginLocalTest() {
	super(null);
}
public CmdLineAndStateAndPluginLocalTest(String name) {
	super(name);
}

public void testCmdLineAndStateAndPluginLocal() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	
	// Preferences in the file specified by -pluginCustomization
	// command line parameter and in the plugin state area
	IPluginDescriptor resPlugin = registry.getPluginDescriptor("cmdLineAndStateAndPluginLocal");
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
	Preferences prefs = runtimePlugin.getPluginPreferences();
	String[] defaultNames = prefs.defaultPropertyNames();
	String[] prefNames = prefs.propertyNames();
	assertTrue("1.0 Three default preferences", defaultNames.length == 3);
	assertTrue("1.1 Two explicit preferences", prefNames.length == 2);
	// Do we have the right names for the explicit preferences?
	boolean foundStateLocalPreference = false;
	boolean foundCommonPreference = false;
	for (int i = 0; i < prefNames.length; i++) {
		if (prefNames[i].equals("StateLocalPreference"))
			foundStateLocalPreference = true;
		else if (prefNames[i].equals("commonCommandLineAndStateAndPluginLocalPreference"))
			foundCommonPreference = true;
	}
	assertTrue("1.2 Got right explicit preference names",
		foundStateLocalPreference && foundCommonPreference);
	// Do we have the right names for the default preferences?
	boolean foundCommandLinePreference = false;
	boolean foundPluginLocalPreference = false;
	foundCommonPreference = false;
	for (int i = 0; i < defaultNames.length; i++) {
		if (defaultNames[i].equals("CommandLinePreference"))
			foundCommandLinePreference = true;
		else if (defaultNames[i].equals("commonCommandLineAndStateAndPluginLocalPreference"))
			foundCommonPreference = true;
		else if (defaultNames[i].equals("PluginLocalPreference"))
			foundPluginLocalPreference = true;
	}
	assertTrue("1.3 Got right default preference names",
		foundCommandLinePreference && foundCommonPreference && foundPluginLocalPreference);
	// Check preference values
	assertTrue("1.4 StateLocalPreference value",
		prefs.getString("StateLocalPreference").equals("From the plugin state area of plugin cmdLineAndStateAndPluginLocal"));
	assertTrue("1.5 CommandLinePreference value",
		prefs.getString("CommandLinePreference").equals("From the command line specified file via the plugin cmdLineAndStateAndPluginLocal"));
	assertTrue("1.6 PluginLocalPreference value",
		prefs.getString("PluginLocalPreference").equals("From the local plugin directory of the plugin cmdLineAndStateAndPluginLocal"));
	assertTrue("1.7 commonCommandLineAndStateAndPluginLocalPreference value",
		prefs.getString("commonCommandLineAndStateAndPluginLocalPreference").equals("Common preference from the plugin state area of plugin cmdLineAndStateAndPluginLocal"));
}
}
