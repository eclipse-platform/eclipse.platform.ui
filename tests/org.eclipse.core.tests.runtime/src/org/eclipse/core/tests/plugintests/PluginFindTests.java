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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.boot.InternalBootLoader;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.SafeFileInputStream;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.PluginFragmentModel;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;

public class PluginFindTests extends WorkspaceSessionTest {

public PluginFindTests() {
	super(null);
}

public PluginFindTests(String name) {
	super(name);
}

private void findHelper(String pluginId, String fragmentId, String errorPrefix, String filePrefix, String pluginSubDir) {
	// Look for a file called <filePrefix>/<pluginId>.txt
	// Make sure this doesn't cause the plugin to be activated.
	// Make sure the data in file looks like:
	// "Test string from " + pluginId + " plugin " + pluginSubDir + " directory."
	// Make sure the file was found in
	//     <workspaceRoot>/Plugintests_Testing/FindTests/plugins/<pluginId>/<pluginSubDir>/<pluginId>.txt
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor(pluginId);
	assertNotNull(errorPrefix + ".0 Can't find plugin " + pluginId);
	// Now make sure we can find the file
	IPath path = null;
	String whichId = pluginId;
	if (fragmentId != null)
		whichId = fragmentId;
	path = new Path(filePrefix + "/" + whichId + ".txt");
	URL result = plugin.find(path);
	assertTrue(errorPrefix + ".1 Plugin should not be activated", !plugin.isPluginActivated());
	assertNotNull(errorPrefix + ".2 Can't find text file, " + path.toString(), result);
	// Make sure this is the right URL
	String stringResult = result.getFile();
	if (pluginSubDir != null) {
		assertTrue(errorPrefix + ".3 Wrong url - " + stringResult,
			stringResult.endsWith("Plugintests_Testing/FindTests/plugins/" + whichId + "/" + pluginSubDir + "/" + whichId + ".txt"));
	} else {
		assertTrue(errorPrefix + ".3 Wrong url - " + stringResult,
			stringResult.endsWith("Plugintests_Testing/FindTests/plugins/" + whichId + "/" + whichId + ".txt"));
	}
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

	if (fragmentId == null)
		assertEquals(errorPrefix + ".5 Data string incorrect", "Test string from " + whichId + " plugin " + pluginSubDir + " directory.", dataString);
	else
		assertEquals(errorPrefix + ".5 Data string incorrect", "Test string from " + whichId + " fragment " + pluginSubDir + " directory.", dataString);
}

private void findFailsHelper(String pluginId, String fragmentId, String errorPrefix, String filePrefix) {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor(pluginId);
	assertNotNull(errorPrefix + ".0 Can't find plugin " + pluginId);
	String whichId = pluginId;
	if (fragmentId != null)
		whichId = fragmentId;
	// Now make sure we can't find the file
	IPath path = new Path(filePrefix + "/" + whichId + ".txt");
	URL result = plugin.find(path);
	assertNull(errorPrefix + ".1 Found text file, " + path.toString(), result);
}

private boolean buildPluginTestFile(String pluginId, String pluginSubDir) {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor(pluginId);
	assertNotNull("0.1 Can't find plugin " + pluginId, plugin);
	URL pluginRoot = plugin.find(new Path("./"));
	File file = null;
	if (pluginSubDir != null) {
		file = new File(pluginRoot.getFile() + pluginSubDir);
		file.mkdirs();
	} else 
		file = new File(pluginRoot.getFile());
	// Now build up the file
	File testFile = new File(file, pluginId + ".txt");
	try {
		FileOutputStream fs = new FileOutputStream(testFile);
		PrintWriter w = new PrintWriter(fs);
		try {
			w.println("Test string from " + pluginId + " plugin " + pluginSubDir + " directory.");
			w.flush();
		} finally {
			w.close();
		}
	} catch (IOException ioe) {
		System.out.println ("Unable to write to test file " + testFile.getPath());
		return false;
	}
	return true;
}

private boolean buildFragmentTestFile(String pluginId, String fragmentId, String pluginSubDir) {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor(pluginId);
	assertNotNull("0.1 Can't find plugin " + pluginId, plugin);
	PluginFragmentModel[] fragments = ((PluginDescriptor)plugin).getFragments();
	// Should only be one fragment
	assertEquals("0.2 Fragment mismatch", fragmentId, fragments[0].getId());
	URL fragmentRoot = null;
	try {
		fragmentRoot = new URL(fragments[0].getLocation());
	} catch (MalformedURLException badURL) {
		return false;
	}
	File file = null;
	if (pluginSubDir != null) {
		file = new File(fragmentRoot.getFile() + pluginSubDir);
		file.mkdirs();
	} else 
		file = new File(fragmentRoot.getFile());
	// Now build up the file
	File testFile = new File(file, fragmentId + ".txt");
	try {
		FileOutputStream fs = new FileOutputStream(testFile);
		PrintWriter w = new PrintWriter(fs);
		try {
			w.println("Test string from " + fragmentId + " fragment " + pluginSubDir + " directory.");
			w.flush();
		} finally {
			w.close();
		}
	} catch (IOException ioe) {
		System.out.println ("Unable to write to test file " + testFile.getPath());
		return false;
	}
	return true;
}

private String buildNLTestFile(String pluginId, String fragmentId, int chopSegment) {
	// Build up nl related directories and test files
	String nl = InternalBootLoader.getNL();
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
	if (fragmentId == null) {
		if (buildPluginTestFile(pluginId, "nl/" + nl))
			return nl;
	} else {
		if (buildFragmentTestFile(pluginId, fragmentId, "nl/" + nl))
			return nl;
	}
	return null;
}
/**
 * Build a test file in the OLD nl/en_US form instead of replacing
 * the under-scores with slashes.
 */
private String buildOldNLTestFile(String pluginId, String fragmentId, int chopSegment) {
	// Build up nl related directories and test files
	String nl = BootLoader.getNL();
	// Chop off the number of segments stated
	int i = chopSegment;
	while (nl.length() > 0 && i > 0) {
		i--;
		int idx = nl.lastIndexOf('_');
		if (idx != -1)
			nl = nl.substring(0, idx);
		else 
			nl = "";
	}
	if ((nl.length() == 0) && (i > 0))
		// We couldn't get rid of all the segments we wanted to
		return null;
	if (fragmentId == null) {
		if (buildPluginTestFile(pluginId, "nl/" + nl))
			return nl;
	} else {
		if (buildFragmentTestFile(pluginId, fragmentId, "nl/" + nl))
			return nl;
	}
	return null;
}

private String buildOSTestFile(String pluginId, String fragmentId, int chopSegment) {
	// Build up os related directories and test files
	String fullOS = InternalBootLoader.getOS() + "/" + InternalBootLoader.getOSArch();
	int i = chopSegment;
	while (fullOS.length() > 0 && i > 0) {
		i--;
		int idx = fullOS.lastIndexOf('/');
		if (idx != -1)
			fullOS = fullOS.substring(0, idx);
		else 
			fullOS = "";
	}
	if ((fullOS.length() == 0) && (i > 0))
		// We couldn't get rid of all the segments we wanted to
		return null;
	if (fragmentId == null) {
		if (buildPluginTestFile(pluginId, "os/" + fullOS))
			return fullOS;
	} else {
		if (buildFragmentTestFile(pluginId, fragmentId, "os/" + fullOS))
			return fullOS;
	}
	return null;
}

private String buildWSTestFile(String pluginId, String fragmentId, int chopSegments) {
	// Build up ws related directories and test files
	String ws = InternalBootLoader.getWS();
	if (chopSegments > 0)
		ws = "";
	if (fragmentId == null) {
		if (buildPluginTestFile(pluginId, "ws/" + ws))
			return ws;
	} else {
		if (buildFragmentTestFile(pluginId, fragmentId, "ws/" + ws))
			return ws;
	}
	return null;
}

private void deleteDirectory (File directory) {
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

private void cleanupTestDirectory (String pluginId, String fragmentId, String testDirectory) {
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
	deleteDirectory(new File(rootString + "/" + testDirectory));
}

public void testFindNothing () {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.runtime");
	assertNotNull("1.0 can't find runtime plugin descriptor", plugin);
	// Just make sure we handle this gracefully
	URL result = plugin.find(null, null);
	assertNull("1.1 non-null result from null path and override", result);
	// And make sure we handle this one too
	result = plugin.find(null);
	assertNull("1.2 non-null result from null path", result);
	// Bug 14224 indicates that for "" or "/" we gave a 
	// NullPointerException.  The following tests ensure we catch
	// this.
	// The result should be the root directory for this plugin
	String pluginRoot = ((PluginDescriptor)plugin).getInstallURLInternal().getFile();
	result = plugin.find(new Path(""));
	assertEquals("1.3 find(new Path(\"\")", pluginRoot, result.getFile());
	result = plugin.find(new Path("/"));
	assertEquals("1.4 find(new Path(\"/\")", pluginRoot, result.getFile());
	result = plugin.find(new Path("./"));
	assertEquals("1.5 find(new Path(\"./\")", pluginRoot, result.getFile());
	result = plugin.find(new Path("."));
	assertEquals("1.6 find(new Path(\".\")", pluginRoot, result.getFile());
	result = plugin.find(new Path(".."));
	assertNull("1.7 find(new Path(\"..\")", result);
}

public void testFindInteresting () {
	IPluginDescriptor plugin = InternalPlatform.getPluginRegistry().getPluginDescriptor("interestingPluginFindTest");
	assertNotNull("1.0 Can't find plugin, interestingPluginFindTest", plugin);
	URL pluginRoot = plugin.find(new Path("./"));
	PluginFragmentModel[] fragments = ((PluginDescriptor)plugin).getFragments();
	assertEquals("1.1 Only one fragment", 1, fragments.length);
	assertEquals("1.2 Fragment id is incorrect", "interestingFragmentFindTest", fragments[0].getId());
	URL fragmentRoot = null;
	try {
		fragmentRoot = new URL(fragments[0].getLocation());
	} catch (MalformedURLException badURL) {
		// ignore bad url
	}

	File testFile = null;
	File testDirs = null;
	try {
		// Check for a file in a directory above this plugin's root
		File pluginRootFile = new File(pluginRoot.getFile());
		testFile = new File(pluginRootFile, "../interestingPluginFindTest.txt");
		try {
			FileOutputStream fs = new FileOutputStream(testFile);
			PrintWriter w = new PrintWriter(fs);
			try {
				w.println("Test string from interestingPluginFindTest plugin in the .. directory.");
				w.flush();
			} finally {
				w.close();
			}
		} catch (IOException ioe) {
			System.out.println ("Unable to write to test file " + testFile.getPath());
		}
		findFailsHelper("interestingPluginFindTest", null, "1", "..");
		testFile.delete();
		testFile = null;

		// Check for a file in a directory above the fragment's root
		File fragmentRootFile = new File(fragmentRoot.getFile());
		testFile = new File(fragmentRootFile, "../interestingFragmentFindTest.txt");
		try {
			FileOutputStream fs = new FileOutputStream(testFile);
			PrintWriter w = new PrintWriter(fs);
			try {
				w.println("Test string from interestingPluginFindTest fragment in the .. directory.");
				w.flush();
			} finally {
				w.close();
			}
		} catch (IOException ioe) {
			System.out.println ("Unable to write to test file " + testFile.getPath());
		}
		findFailsHelper("interestingPluginFindTest", "interestingFragmentFindTest", "2", "..");
		testFile.delete();

		// Check for a file in a sibling directory to this plugin
		testDirs = new File(pluginRootFile, "../siblingTestDirectory/");
		testDirs.mkdirs();
		testFile = new File(pluginRootFile, "../siblingTestDirectory/interestingPluginFindTest.txt");
		try {
			FileOutputStream fs = new FileOutputStream(testFile);
			PrintWriter w = new PrintWriter(fs);
			try {
				w.println("Test string from interestingPluginFindTest plugin in the ../siblingTestDirectory directory.");
				w.flush();
			} finally {
				w.close();
			}
		} catch (IOException ioe) {
			System.out.println ("Unable to write to test file " + testFile.getPath());
		}
		findFailsHelper("interestingPluginFindTest", null, "3", "../siblingTestDirectory");
		deleteDirectory(testDirs);

		// Check for a file in a sibling directory to this fragment
		testDirs = new File(pluginRootFile, "../siblingFragmentTestDirectory/");
		testDirs.mkdirs();
		testFile = new File(pluginRootFile, "../siblingFragmentTestDirectory/interestingFragmentFindTest.txt");
		try {
			FileOutputStream fs = new FileOutputStream(testFile);
			PrintWriter w = new PrintWriter(fs);
			try {
				w.println("Test string from interestingPluginFindTest fragment in the ../siblingFragmentTestDirectory directory.");
				w.flush();
			} finally {
				w.close();
			}
		} catch (IOException ioe) {
			System.out.println ("Unable to write to test file " + testFile.getPath());
		}
		findFailsHelper("interestingPluginFindTest", "interestingFragmentFindTest", "4", "../siblingFragmentTestDirectory");
		deleteDirectory(testDirs);
	} finally {
		if (testFile != null)
			testFile.delete();
		if (testDirs != null)
			deleteDirectory(testDirs);
	}

	// Check for /namelessDirectory
//	findHelper("interestingPluginFindTest", "1", "/namelessDirectory/namelessPluginFind.txt", "Test string from interestingPluginFind plugin namelessDirectory directory.", "interestingPluginFind/namelessDirectory");
}

public void testRootFind () {
	// Do a find for something in the plugin root directory
	try {
		if (buildPluginTestFile("rootPluginFindTest", null))
			findHelper("rootPluginFindTest", null, "1", ".", null);
	} finally {
		cleanupTestDirectory("rootPluginFindTest", null, "rootPluginFindTest.txt");
	}
	// Do a find for something in the fragment root directory
	try {
		if (buildFragmentTestFile("rootPluginFindTest", "rootFragmentFindTest", null))
			findHelper("rootPluginFindTest", "rootFragmentFindTest", "2", ".", null);
	} finally {
		cleanupTestDirectory("rootPluginFindTest", "rootFragmentFindTest", "rootFragmentFindTest.txt");
	}
}

public void testNLFind() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	// How many segments in the default locale?
	String nl = InternalBootLoader.getNL();
	// there is at least one segment
	int localeSegments = 1;
	int i = nl.indexOf('_');
	while (i != -1) {
		localeSegments++;
		nl = nl.substring(i+1);
		i = nl.indexOf('_');
	}
	// Build up nl related directories and test files
	try {
		String subDirectory = buildNLTestFile("nlPluginFindTest", null, 0);
		if (subDirectory != null)
			findHelper("nlPluginFindTest", null, "1", "$nl$", "nl/" + subDirectory);
		else
			// We don't expect this one to fail
			fail ("0.1 Could not build first nl test data for nlPluginFindTest");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", null, "nl");
	}
	
	if (localeSegments > 1) {
		try {
			String subDirectory = buildNLTestFile("nlPluginFindTest", null, 1);
			if (subDirectory != null)
				findHelper("nlPluginFindTest", null, "2", "$nl$", "nl/" + subDirectory);
			else
				// We don't expect this one to fail
				fail ("0.2 Could not build first nl test data for nlPluginFindTest");
		} finally {
			cleanupTestDirectory("nlPluginFindTest", null, "nl");
		}
	}

	// Do a find for something in the plugin nl directory
	try {
		String subDirectory = buildNLTestFile("nlPluginFindTest", null, localeSegments);
		if (subDirectory != null)
			findFailsHelper("nlPluginFindTest", null, "3", "$nl$");
		else
			// We don't expect this one to fail
			fail ("0.3 Could not build first nl test data for nlPluginFindTest");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", null, "nl");
	}
	
	// Do a find for something in the plugin root directory
	try {
		if (buildPluginTestFile("nlPluginFindTest", null))
			findHelper("nlPluginFindTest", null, "4", "$nl$", null);
	} finally {
		cleanupTestDirectory("nlPluginFindTest", null, "nlPluginFindTest.txt");
	}
		
	// Do a find for something in the plugin ja/CA directory which is a nonsense locale
	try {
		if (buildPluginTestFile("nlPluginFindTest", "nl/ja/CA"))
			findFailsHelper("nlPluginFindTest", null, "5", "$nl$");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", null, "nl");
	}

	// Do a find for something in the fragment directory
	try {
		String subDirectory = buildNLTestFile("nlPluginFindTest", "nlFragmentFindTest", 0);
		if (subDirectory != null)
			findHelper("nlPluginFindTest", "nlFragmentFindTest", "6", "$nl$", "nl/" + subDirectory);
		else
			// We don't expect this one to fail
			fail ("0.4 Could not build first nl test data for nlPluginFindTest");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", "nlFragmentFindTest", "nl");
	}

	if (localeSegments > 1) {
		try {
			String subDirectory = buildNLTestFile("nlPluginFindTest", "nlFragmentFindTest", 1);
			if (subDirectory != null)
				findHelper("nlPluginFindTest", "nlFragmentFindTest", "7", "$nl$", "nl/" + subDirectory);
			else
				// We don't expect this one to fail
				fail ("0.5 Could not build first nl test data for nlPluginFindTest");
		} finally {
			cleanupTestDirectory("nlPluginFindTest", "nlFragmentFindTest", "nl");
		}
	}

	// Do a find for something in the fragment nl directory
	try {
		String subDirectory = buildNLTestFile("nlPluginFindTest", "nlFragmentFindTest", localeSegments);
		if (subDirectory != null)
			findFailsHelper("nlPluginFindTest", "nlFragmentFindTest", "8", "$nl$");
		else
			// We don't expect this one to fail
			fail ("0.6 Could not build first nl test data for nlPluginFindTest");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", "nlFragmentFindTest", "nl");
	}

	// Do a find for something in the fragment root directory
	try {
		if (buildFragmentTestFile("nlPluginFindTest", "nlFragmentFindTest", null))
			findHelper("nlPluginFindTest", "nlFragmentFindTest", "9", "$nl$", null);
	} finally {
		cleanupTestDirectory("nlPluginFindTest", "nlFragmentFindTest", "nlFragmentFindTest.txt");
	}

	// Do a find for something in the fragment ja/CA directory which is a nonsense locale
	try {
		if (buildFragmentTestFile("nlPluginFindTest", "nlFragmentFindTest", "nl/ja/CA"))
			findFailsHelper("nlPluginFindTest", "nlFragmentFindTest", "10", "$nl$");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", "nlFragmentFindTest", "nl");
	}
}
/**
 * Test the #find method with the old en_US directory structure. (the new one replaces
 * the under-scores wtih slashes)
 */
public void _testOldNLFind() {
	// How many segments in the default locale?
	String nl = BootLoader.getNL();
	// there is at least one segment
	int localeSegments = 1;
	int i = nl.indexOf('_');
	while (i != -1) {
		localeSegments++;
		nl = nl.substring(i+1);
		i = nl.indexOf('_');
	}
	// Build up nl related directories and test files
	try {
		String subDirectory = buildOldNLTestFile("nlPluginFindTest", null, 0);
		if (subDirectory != null)
			findHelper("nlPluginFindTest", null, "1", "$nl$", "nl/" + subDirectory);
		else
			// We don't expect this one to fail
			fail ("0.1 Could not build first nl test data for nlPluginFindTest");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", null, "nl");
	}
	
	if (localeSegments > 1) {
		try {
			String subDirectory = buildOldNLTestFile("nlPluginFindTest", null, 1);
			if (subDirectory != null)
				findHelper("nlPluginFindTest", null, "2", "$nl$", "nl/" + subDirectory);
			else
				// We don't expect this one to fail
				fail ("0.2 Could not build first nl test data for nlPluginFindTest");
		} finally {
			cleanupTestDirectory("nlPluginFindTest", null, "nl");
		}
	}

	// Do a find for something in the plugin nl directory
	try {
		String subDirectory = buildOldNLTestFile("nlPluginFindTest", null, localeSegments);
		if (subDirectory != null)
			findFailsHelper("nlPluginFindTest", null, "3", "$nl$");
		else
			// We don't expect this one to fail
			fail ("0.3 Could not build first nl test data for nlPluginFindTest");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", null, "nl");
	}
	
	// Do a find for something in the plugin root directory
	try {
		if (buildPluginTestFile("nlPluginFindTest", null))
			findHelper("nlPluginFindTest", null, "4", "$nl$", null);
	} finally {
		cleanupTestDirectory("nlPluginFindTest", null, "nlPluginFindTest.txt");
	}
		
	// Do a find for something in the plugin ja/CA directory which is a nonsense locale
	try {
		if (buildPluginTestFile("nlPluginFindTest", "nl/ja/CA"))
			findFailsHelper("nlPluginFindTest", null, "5", "$nl$");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", null, "nl");
	}

	// Do a find for something in the fragment directory
	try {
		String subDirectory = buildOldNLTestFile("nlPluginFindTest", "nlFragmentFindTest", 0);
		if (subDirectory != null)
			findHelper("nlPluginFindTest", "nlFragmentFindTest", "6", "$nl$", "nl/" + subDirectory);
		else
			// We don't expect this one to fail
			fail ("0.4 Could not build first nl test data for nlPluginFindTest");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", "nlFragmentFindTest", "nl");
	}

	if (localeSegments > 1) {
		try {
			String subDirectory = buildOldNLTestFile("nlPluginFindTest", "nlFragmentFindTest", 1);
			if (subDirectory != null)
				findHelper("nlPluginFindTest", "nlFragmentFindTest", "7", "$nl$", "nl/" + subDirectory);
			else
				// We don't expect this one to fail
				fail ("0.5 Could not build first nl test data for nlPluginFindTest");
		} finally {
			cleanupTestDirectory("nlPluginFindTest", "nlFragmentFindTest", "nl");
		}
	}

	// Do a find for something in the fragment nl directory
	try {
		String subDirectory = buildOldNLTestFile("nlPluginFindTest", "nlFragmentFindTest", localeSegments);
		if (subDirectory != null)
			findFailsHelper("nlPluginFindTest", "nlFragmentFindTest", "8", "$nl$");
		else
			// We don't expect this one to fail
			fail ("0.6 Could not build first nl test data for nlPluginFindTest");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", "nlFragmentFindTest", "nl");
	}

	// Do a find for something in the fragment root directory
	try {
		if (buildFragmentTestFile("nlPluginFindTest", "nlFragmentFindTest", null))
			findHelper("nlPluginFindTest", "nlFragmentFindTest", "9", "$nl$", null);
	} finally {
		cleanupTestDirectory("nlPluginFindTest", "nlFragmentFindTest", "nlFragmentFindTest.txt");
	}

	// Do a find for something in the fragment ja/CA directory which is a nonsense locale
	try {
		if (buildFragmentTestFile("nlPluginFindTest", "nlFragmentFindTest", "nl/ja/CA"))
			findFailsHelper("nlPluginFindTest", "nlFragmentFindTest", "10", "$nl$");
	} finally {
		cleanupTestDirectory("nlPluginFindTest", "nlFragmentFindTest", "nl");
	}
}

public void testOSFind() {
	String subDirectory = null;
	// Do a find for something in the plugin os/* directory
	try {
		subDirectory = buildOSTestFile("osPluginFindTest", null, 0);
		if (subDirectory != null)
			findHelper("osPluginFindTest", null, "1", "$os$", "os/" + subDirectory);
		else
			// We don't expect this one to fail
			fail ("0.1 Could not build os test data");
	} finally {
		cleanupTestDirectory("osPluginFindTest", null, "os");
	}

	// Now chop off a segment
	try {
		subDirectory = buildOSTestFile("osPluginFindTest", null, 1);
		if (subDirectory != null)
			findHelper("osPluginFindTest", null, "2", "$os$", "os/" + subDirectory);
		else
			// We don't expect this one to fail
			fail ("0.2 Could not build os test data");
	} finally {
		cleanupTestDirectory("osPluginFindTest", null, "os");
	}
	
	// Do a find for something in the plugin os directory
	try {
		subDirectory = buildOSTestFile("osPluginFindTest", null, 2);
		if (subDirectory != null)
			findFailsHelper("osPluginFindTest", null, "3", "$os$");
		else
			// We don't expect this one to fail
			fail ("0.3 Could not build os test data");
	} finally {
		cleanupTestDirectory("osPluginFindTest", null, "os");
	}

	// Do a find for something in the plugin root directory
	try {
		if (buildPluginTestFile("osPluginFindTest", null))
			findHelper("osPluginFindTest", null, "4", "$os$", null);
	} finally {
		cleanupTestDirectory("osPluginFindTest", null, "osPluginFindTest.txt");
	}

	// Do a find for something in the plugin os/badOS directory which is a 
	// nonsense os
	try {
		if (buildPluginTestFile("osPluginFindTest", "os/badOS"))
			findFailsHelper("osPluginFindTest", null, "5", "$os$");
	} finally {
		cleanupTestDirectory("osPluginFindTest", null, "os");
	}

	// Do a find for something in the fragment os/* directory
	try {
		subDirectory = buildOSTestFile("osPluginFindTest", "osFragmentFindTest", 0);
		if (subDirectory != null)
			findHelper("osPluginFindTest", "osFragmentFindTest", "6", "$os$", "os/" + subDirectory);
		else
			// We don't expect this one to fail
			fail ("0.4 Could not build os test data");
	} finally {
		cleanupTestDirectory("osPluginFindTest", "osFragmentFindTest", "os");
	}

	// Get rid of one segment (the osArch portion)
	try {
		subDirectory = buildOSTestFile("osPluginFindTest", "osFragmentFindTest", 1);
		if (subDirectory != null)
			findHelper("osPluginFindTest", "osFragmentFindTest", "7", "$os$", "os/" + subDirectory);
		else
			// We don't expect this one to fail
			fail ("0.5 Could not build os test data");
	} finally {
		cleanupTestDirectory("osPluginFindTest", "osFragmentFindTest", "os");
	}
	
	// Do a find for something in the fragment os directory
	try {
		subDirectory = buildOSTestFile("osPluginFindTest", "osFragmentFindTest", 2);
		if (subDirectory != null)
			findFailsHelper("osPluginFindTest", "osFragmentFindTest", "8", "$os$");
		else
			// We don't expect this one to fail
			fail ("0.6 Could not build os test data");
	} finally {
		cleanupTestDirectory("osPluginFindTest", "osFragmentFindTest", "os");
	}

	// Do a find for something in the fragment root directory
	try {
		if (buildFragmentTestFile("osPluginFindTest", "osFragmentFindTest", null))
			findHelper("osPluginFindTest", "osFragmentFindTest", "9", "$os$", null);
	} finally {
		cleanupTestDirectory("osPluginFindTest", "osFragmentFindTest", "osFragmentFindTest.txt");
	}

	// Do a find for something in the fragment os/badOS directory which is a 
	// nonsense os
	try {
		if (buildFragmentTestFile("osPluginFindTest", "osFragmentFindTest", "os/badOS"))
			findFailsHelper("osPluginFindTest", "osFragmentFindTest", "10", "$os$");
	} finally {
		cleanupTestDirectory("osPluginFindTest", "osFragmentFindTest", "os");
	}
}

public void testWSFind() {
	String subDirectory = null;
	// Do a find for something in the plugin ws/* directory
	try {
		subDirectory = buildWSTestFile("wsPluginFindTest", null, 0);
		if (subDirectory != null)
			findHelper("wsPluginFindTest", null, "1", "$ws$", "ws/" + subDirectory);
		else
			// We don't expect this one to fail
			fail ("0.1 Could not build ws test data");
	} finally {
		cleanupTestDirectory("wsPluginFindTest", null, "ws");
	}

	// Do a find for something in the plugin ws directory
	try {
		subDirectory = buildWSTestFile("wsPluginFindTest", null, 1);
		if (subDirectory != null)
			findFailsHelper("wsPluginFindTest", null, "2", "$ws$");
		else
			// We don't expect this one to fail
			fail ("0.2 Could not build ws test data");
	} finally {
		cleanupTestDirectory("wsPluginFindTest", null, "ws");
	}

	// Do a find for something in the plugin root directory
	try {
		if (buildPluginTestFile("wsPluginFindTest", null))
			findHelper("wsPluginFindTest", null, "3", "$ws$", null);
	} finally {
		cleanupTestDirectory("wsPluginFindTest", null, "wsPluginFindTest.txt");
	}

	// Do a find for something in the plugin ws/badWS directory which is a 
	// nonsense ws
	try {
		if (buildPluginTestFile("wsPluginFindTest", "ws/badWS"))
			findFailsHelper("wsPluginFindTest", null, "4", "$ws$");
	} finally {
		cleanupTestDirectory("wsPluginFindTest", null, "ws");
	}

	// Do a find for something in the fragment ws/* directory
	try {
		subDirectory = buildWSTestFile("wsPluginFindTest", "wsFragmentFindTest", 0);
		if (subDirectory != null)
			findHelper("wsPluginFindTest", "wsFragmentFindTest", "5", "$ws$", "ws/" + subDirectory);
		else
			// We don't expect this one to fail
			fail ("0.3 Could not build ws test data");
	} finally {
		cleanupTestDirectory("wsPluginFindTest", "wsFragmentFindTest", "ws");
	}

	// Do a find for something in the fragment ws directory
	try {
		subDirectory = buildWSTestFile("wsPluginFindTest", "wsFragmentFindTest", 1);
		if (subDirectory != null)
			findFailsHelper("wsPluginFindTest", "wsFragmentFindTest", "6", "$ws$");
		else
			// We don't expect this one to fail
			fail ("0.4 Could not build ws test data");
	} finally {
		cleanupTestDirectory("wsPluginFindTest", "wsFragmentFindTest", "ws");
	}

	// Do a find for something in the fragment root directory
	try {
		if (buildFragmentTestFile("wsPluginFindTest", "wsFragmentFindTest", null))
			findHelper("wsPluginFindTest", "wsFragmentFindTest", "7", "$ws$", null);
	} finally {
		cleanupTestDirectory("wsPluginFindTest", "wsFragmentFindTest", "wsFragmentFindTest.txt");
	}

	// Do a find for something in the fragment ws/badWS directory which is a 
	// nonsense ws
	try {
		if (buildFragmentTestFile("wsPluginFindTest", "wsFragmentFindTest", "ws/badWS"))
			findFailsHelper("wsPluginFindTest", "wsFragmentFindTest", "8", "$ws$");
	} finally {
		cleanupTestDirectory("wsPluginFindTest", "wsFragmentFindTest", "ws");
	}
}
}
