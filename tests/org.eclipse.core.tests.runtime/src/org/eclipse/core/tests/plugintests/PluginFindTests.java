/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.plugintests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.SafeFileInputStream;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;

public class PluginFindTests extends WorkspaceSessionTest {

public PluginFindTests() {
	super(null);
}

public PluginFindTests(String name) {
	super(name);
}

public void testFindNothing () {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.runtime");
	assertNotNull("1.0 can't find runtime plugin descriptor", plugin);
	// Just make sure we handle this gracefully
	URL result = plugin.find(null, null);
	assertNull("1.1 non-null result", result);
	// And make sure we handle this one too
	result = plugin.find(null);
	assertNull("1.2 non-null result", result);
}

private void findHelper(String pluginId, String errorPrefix, String fileName, String fileData, String directory) {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor(pluginId);
	assertNotNull(errorPrefix + ".0 Can't find plugin " + pluginId);
	// Now make sure we can find the file
	IPath path = new Path(fileName);
	URL result = plugin.find(path);
	assertTrue(errorPrefix + ".1 Plugin should not be activated", !plugin.isPluginActivated());
	assertNotNull(errorPrefix + ".2 Can't find text file, " + fileName, result);
	// Make sure this is the right URL
	String stringResult = result.toString();
	// Strip off any leading directory stuff from the filename
	int idx = fileName.lastIndexOf("/");
	String bareFileName = null;
	if (idx == -1)
		bareFileName = fileName;
	else
		bareFileName = fileName.substring(idx+1);
	assertTrue(errorPrefix + ".3 Wrong url - " + stringResult,
		stringResult.endsWith("Plugintests_Testing/FindTests/plugins/" + directory + "/" + bareFileName));
	// And finally, read the file and make sure it is the right one.
	InputStream in = null;
	byte[] buffer = null;
	try {
		in = result.openStream();
		buffer = new byte[in.available()];
		in.read(buffer);
		in.close();
	} catch (IOException ioe) {
		fail(errorPrefix + ".4 IOException reading " + stringResult);
	}
	String dataString = (new String(buffer)).trim();

	assertTrue(errorPrefix + ".5 Data string incorrect", dataString.equals(fileData));
}

private void findFailsHelper(String pluginId, String errorPrefix, String fileName, String fileData, String directory) {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor(pluginId);
	assertNotNull(errorPrefix + ".0 Can't find plugin " + pluginId);
	// Now make sure we can't find the file
	IPath path = new Path(fileName);
	URL result = plugin.find(path);
	assertNull(errorPrefix + ".1 Found text file, " + fileName, result);
}

public void testRootFind () {
	// Do a find for something in the plugin root directory
	findHelper("rootPluginFindTest", "2", "rootPluginFind.txt", "Test string from rootPluginFind plugin root directory.", "rootPluginFind");
	// Do a find for something in the fragment root directory
	findHelper("rootPluginFindTest", "3", "rootFragmentFind.txt", "Test string from rootFragmentFind fragment root directory.", "rootFragmentFind");
}

public void testNLFind() {
	// Do a find for something in the plugin nl/en/CA directory
	// ASSUMPTION - your default locale is en_CA
	findHelper("nlPluginFindTest1", "1", "$nl$/nlPluginFind1.txt", "Test string from nlPluginFind1 plugin nl/en/CA directory.", "nlPluginFind1/nl/en/CA");
	// Do a find for something in the plugin nl/en directory
	findHelper("nlPluginFindTest2", "2", "$nl$/nlPluginFind2.txt", "Test string from nlPluginFind2 plugin nl/en directory.", "nlPluginFind2/nl/en");
	// Do a find for something in the plugin nl directory
	findFailsHelper("nlPluginFindTest3", "3", "$nl$/nlPluginFind3.txt", "Test string from nlPluginFind3 plugin nl directory.", "nlPluginFind3/nl");
	// Do a find for something in the plugin root directory
	findHelper("nlPluginFindTest4", "4", "$nl$/nlPluginFind4.txt", "Test string from nlPluginFind4 plugin root directory.", "nlPluginFind4");
	// Do a find for something in the plugin ja/JP directory when ja/JP is not our default locale
	findFailsHelper("nlPluginFindTest5", "5", "$nl$/nlPluginFind5.txt", "Test string from nlPluginFind5 plugin nl/ja/JP directory.", "nlPluginFind5/nl/ja/JP");

	// Do a find for something in the fragment nl/en/CA directory
	// ASSUMPTION - your default locale is en_CA
	findHelper("nlPluginFindTest1", "6", "$nl$/nlFragmentFind1.txt", "Test string from nlFragmentFind1 fragment nl/en/CA directory.", "nlFragmentFind1/nl/en/CA");
	// Do a find for something in the fragment nl/en directory
	findHelper("nlPluginFindTest2", "7", "$nl$/nlFragmentFind2.txt", "Test string from nlFragmentFind2 fragment nl/en directory.", "nlFragmentFind2/nl/en");
	// Do a find for something in the fragment nl directory
	findFailsHelper("nlPluginFindTest3", "8", "$nl$/nlFragmentFind3.txt", "Test string from nlFragmentFind3 fragment nl directory.", "nlFragmentFind3/nl");
	// Do a find for something in the fragment root directory
	findHelper("nlPluginFindTest4", "9", "$nl$/nlFragmentFind4.txt", "Test string from nlFragmentFind4 fragment root directory.", "nlFragmentFind4");
	// Do a find for something in the fragment ja/JP directory when ja/JP is not our default locale
	findFailsHelper("nlPluginFindTest5", "10", "$nl$/nlFragmentFind5.txt", "Test string from nlFragmentFind5 fragment nl/ja/JP directory.", "nlFragmentFind5/nl/ja/JP");
}

public void testOSFind() {
	// Do a find for something in the plugin os/win32/x86 directory
	// ASSUMPTION - your default os is win32 on an x86 machine
	findFailsHelper("osPluginFindTest1", "1", "$os$/osPluginFind1.txt", "Test string from osPluginFind1 plugin os/win32/x86 directory.", "osPluginFind1/os/win32/x86");
	// Do a find for something in the plugin os/win32 directory
	findHelper("osPluginFindTest2", "2", "$os$/osPluginFind2.txt", "Test string from osPluginFind2 plugin os/win32 directory.", "osPluginFind2/os/win32");
	// Do a find for something in the plugin os directory
	findFailsHelper("osPluginFindTest3", "3", "$os$/osPluginFind3.txt", "Test string from osPluginFind3 plugin os directory.", "osPluginFind3/os");
	// Do a find for something in the plugin root directory
	findHelper("osPluginFindTest4", "4", "$os$/osPluginFind4.txt", "Test string from osPluginFind4 plugin root directory.", "osPluginFind4");
	// Do a find for something in the plugin os/linux/x86 directory when this is not our default os
	findFailsHelper("osPluginFindTest5", "5", "$os$/osPluginFind5.txt", "Test string from osPluginFind5 plugin os/linux/x86 directory.", "osPluginFind5/os/linux/x86");

	// Do a find for something in the fragment os/win32/x86 directory
	// ASSUMPTION - your default os is win32 on an x86 machine
	findFailsHelper("osPluginFindTest1", "6", "$os$/osFragmentFind1.txt", "Test string from osFragmentFind1 fragment os/win32/x86 directory.", "osFragmentFind1/os/win32/x86");
	// Do a find for something in the fragment os/win32 directory
	findHelper("osPluginFindTest2", "7", "$os$/osFragmentFind2.txt", "Test string from osFragmentFind2 fragment os/win32 directory.", "osFragmentFind2/os/win32");
	// Do a find for something in the fragment os directory
	findFailsHelper("osPluginFindTest3", "8", "$os$/osFragmentFind3.txt", "Test string from osFragmentFind3 fragment os directory.", "osFragmentFind3/os");
	// Do a find for something in the fragment root directory
	findHelper("osPluginFindTest4", "9", "$os$/osFragmentFind4.txt", "Test string from osFragmentFind4 fragment root directory.", "osFragmentFind4");
	// Do a find for something in the fragment os/linux/x86 directory when this is not our default os
	findFailsHelper("osPluginFindTest5", "10", "$os$/osFragmentFind5.txt", "Test string from osFragmentFind5 fragment os/linux/x86 directory.", "osFragmentFind5/os/linux/x86");
}

public void testWSFind() {
	// Do a find for something in the plugin ws/win32 directory
	// ASSUMPTION - your default ws is win32
	findHelper("wsPluginFindTest1", "1", "$ws$/wsPluginFind1.txt", "Test string from wsPluginFind1 plugin ws/win32 directory.", "wsPluginFind1/ws/win32");
	// Do a find for something in the plugin ws directory
	findFailsHelper("wsPluginFindTest2", "2", "$ws$/wsPluginFind2.txt", "Test string from wsPluginFind2 plugin ws directory.", "wsPluginFind2/ws");
	// Do a find for something in the plugin root directory
	findHelper("wsPluginFindTest3", "3", "$ws$/wsPluginFind3.txt", "Test string from wsPluginFind3 plugin root directory.", "wsPluginFind3");
	// Do a find for something in the plugin ws/motif directory when this is not our default ws
	findFailsHelper("wsPluginFindTest4", "4", "$ws$/wsPluginFind4.txt", "Test string from wsPluginFind4 plugin ws/motif directory.", "wsPluginFind4/ws/motif");

	// Do a find for something in the fragment ws/win32 directory
	// ASSUMPTION - your default ws is win32
	findHelper("wsPluginFindTest1", "5", "$ws$/wsFragmentFind1.txt", "Test string from wsFragmentFind1 fragment ws/win32 directory.", "wsFragmentFind1/ws/win32");
	// Do a find for something in the fragment ws directory
	findFailsHelper("wsPluginFindTest2", "6", "$ws$/wsFragmentFind2.txt", "Test string from wsFragmentFind2 fragment ws directory.", "wsFragmentFind2/ws");
	// Do a find for something in the fragment root directory
	findHelper("wsPluginFindTest3", "7", "$ws$/wsFragmentFind3.txt", "Test string from wsFragmentFind3 fragment root directory.", "wsFragmentFind3");
	// Do a find for something in the fragment ws/motif directory when this is not our default os
	findFailsHelper("wsPluginFindTest4", "8", "$ws$/osFragmentFind4.txt", "Test string from wsFragmentFind4 fragment ws/motif directory.", "wsFragmentFind4/ws/motif");
}
}
