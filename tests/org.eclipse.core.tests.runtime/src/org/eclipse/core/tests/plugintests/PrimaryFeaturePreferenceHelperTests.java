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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;

public class PrimaryFeaturePreferenceHelperTests extends WorkspaceSessionTest {

public PrimaryFeaturePreferenceHelperTests() {
	super(null);
}
public PrimaryFeaturePreferenceHelperTests(String name) {
	super(name);
}

public static void deleteDirectory (File directory) {
	String[] files = directory.list();
	if (files == null) {
		directory.delete();
		return;
	}
	for (int i = 0; i < files.length; i++) {
		File newFile = new File(directory, files[i]);
		if (newFile.isFile())
			newFile.delete();
		else if (newFile.isDirectory())
			deleteDirectory(newFile);
	}
	directory.delete();
}

protected void setUp () {
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	IPluginDescriptor primaryFeatureDescriptor = Platform.getPluginRegistry().getPluginDescriptor(primaryFeaturePluginId);
	assertNotNull("Can't find primary feature plugin " + primaryFeatureDescriptor);
	String rootDirectory = null;
	try {
		rootDirectory = new URL(((PluginDescriptor)primaryFeatureDescriptor).getLocation()).getFile();
	} catch (MalformedURLException badURL) {
		fail("Bad URL created for " + ((PluginDescriptor)primaryFeatureDescriptor).getLocation());
	}
	IPath sourcePath = new Path(rootDirectory).append("nl");
	File sourceFile = sourcePath.toFile();
	if (sourceFile.exists()) {
		if (!sourceFile.renameTo(new File(new Path(rootDirectory).append("xxsavenl").toString())))
			fail("Unable to move nl directory for primary feature " + primaryFeaturePluginId);
	}
	sourcePath = new Path(rootDirectory).append("plugin_customization.ini");
	sourceFile = new File(sourcePath.toString());
	if (sourceFile.exists()) {
		if (!sourceFile.renameTo(new File(new Path(rootDirectory).append("xxsaveplugin_customization.ini").toString())))
			fail("Unable to move existing plugin_customization.ini file.");
	}
	sourcePath = new Path(rootDirectory).append("plugin_customization.properties");
	sourceFile = new File(sourcePath.toString());
	if (sourceFile.exists()) {
		if (!sourceFile.renameTo(new File(new Path(rootDirectory).append("xxsaveplugin_customization.properties").toString())))
			fail("Unable to move existing plugin_customization.properties file.");
	}
}

protected void tearDown () {
	String primaryFeaturePluginId = BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	IPluginDescriptor primaryFeatureDescriptor = Platform.getPluginRegistry().getPluginDescriptor(primaryFeaturePluginId);
	assertNotNull("Can't find primary feature plugin " + primaryFeatureDescriptor);
	String rootDirectory = null;
	try {
		rootDirectory = new URL(((PluginDescriptor)primaryFeatureDescriptor).getLocation()).getFile();
	} catch (MalformedURLException badURL) {
		fail("Bad URL created for " + ((PluginDescriptor)primaryFeatureDescriptor).getLocation());
	}
	IPath sourcePath = new Path(rootDirectory).append("xxsavenl");
	File sourceFile = new File(sourcePath.toString());
	if (sourceFile.exists()) {
		IPath destPath = new Path(rootDirectory).append("nl");
		File destFile = new File(destPath.toString());
		if (destFile.exists()) {
			deleteDirectory(destFile);
		}
		if (!sourceFile.renameTo(new File(new Path(rootDirectory).append("nl").toString())))
			fail("Unable to restore nl directory for primary feature " + primaryFeaturePluginId);
	}
	sourcePath = new Path(rootDirectory).append("xxsaveplugin_customization.ini");
	sourceFile = new File(sourcePath.toString());
	if (sourceFile.exists()) {
		IPath destPath = new Path(rootDirectory).append("plugin_customization.ini");
		File destFile = new File(destPath.toString());
		if (destFile.exists()) {
			if (!destFile.delete()) 
				fail("Unable to remove test plugin_customization.ini file for " + primaryFeaturePluginId);
		}
		if (!sourceFile.renameTo(new File(new Path(rootDirectory).append("plugin_customization.ini").toString())))
			fail("Unable to restore plugin_customization.ini file.");
	}
	sourcePath = new Path(rootDirectory).append("xxsaveplugin_customization.properties");
	sourceFile = new File(sourcePath.toString());
	if (sourceFile.exists()) {
		IPath destPath = new Path(rootDirectory).append("plugin_customization.properties");
		File destFile = new File(destPath.toString());
		if (destFile.exists()) {
			if (!destFile.delete()) 
				fail("Unable to remove test plugin_customization.properties file for " + primaryFeaturePluginId);
		}
		if (!sourceFile.renameTo(new File(new Path(rootDirectory).append("plugin_customization.properties").toString())))
			fail("Unable to restore plugin_customization.properties file.");
	}
}

}
