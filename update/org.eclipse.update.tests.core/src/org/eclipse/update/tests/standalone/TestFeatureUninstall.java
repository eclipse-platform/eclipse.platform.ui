/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.update.tests.standalone;

import java.net.*;
import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.standalone.*;


public class TestFeatureUninstall extends StandaloneManagerTestCase {
	public static boolean isUninstalled;
	public URL TARGET_FILE_SITE;
	
	public TestFeatureUninstall(String arg0){
		super(arg0);
		isUninstalled = false;
		try {
			TARGET_FILE_SITE = new URL("file", null, "D:/temp/standalone/mytarget/");
//			TARGET_FILE_SITE = new URL("file", null, dataPath + "standalone/mytarget/");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void umSetUp() {
		System.out.println("looking at configured sites available....");
		checkConfiguredSites();
		
		String featureId = "my.alphabet";
		String version = "1.0.0";
		String config = "file:D:/temp/standalone/config/";
		if(!isUninstalled){
			System.out.println(
				"==============" + this.getClass() + "=============");
			StandaloneUpdateApplication app = new StandaloneUpdateApplication();
			try {
				exitValue = (Integer)app.run(getCommand(
					"install",
					featureId,
					version,
					config,
					null,
					TARGET_FILE_SITE.getFile()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isUninstalled = true;
		}
	}
	
	// ensure exit without problems
	public void testExitValue() throws Exception {
		System.out.println("exitValue: " + exitValue);
		assertEquals(exitValue, IPlatformRunnable.EXIT_OK);
	}
	
	public void testFeaturesUninstalled() throws Exception {
		try {
			ISite localSite = SiteManager.getSite(TARGET_FILE_SITE, null);
			if (localSite.getCurrentConfiguredSite() == null) {
				System.out.println("local site has null current config site");
				localSite = getConfiguredSite(TARGET_FILE_SITE.getFile());
			}
			
			// get feature references 
			IFeatureReference[] localFeatures =
				localSite.getFeatureReferences();
			ArrayList list = new ArrayList();
			if (localFeatures == null || localFeatures.length == 0){
				fail("No features on the target site");
			} else {
				for (int i = 0; i < localFeatures.length; i++)
					list.add(localFeatures[i].getVersionedIdentifier().toString());
			}
			String[] featureNames =
				{
					"my.alphabet.round.letters_1.0.0",
					"my.alphabet.straight.letters_1.0.0",
					"my.alphabet_1.0.0" };
			assertTrue(checkFilesNotInList(featureNames, list));
		} catch (CoreException e) {
			System.err.println(e);
		}
	}
	
	public void testPluginsUninstalled() throws Exception {
		try {
			ISite localSite = SiteManager.getSite(TARGET_FILE_SITE, null);
			
			if (localSite.getCurrentConfiguredSite() == null) {
				System.out.println("local site has null current config site");
				localSite = getConfiguredSite(TARGET_FILE_SITE.getFile());
			}
			
			IPluginEntry[] pluginEntries = localSite.getPluginEntries();
			ArrayList list = new ArrayList();
			if (pluginEntries == null || pluginEntries.length == 0)
				fail("No plugin entries on the target site");
			else
				for (int i = 0; i < pluginEntries.length; i++){
					list.add(pluginEntries[i].getVersionedIdentifier().toString());
					System.out.println("plugin found: " + pluginEntries[i].getVersionedIdentifier().toString());
				}

			String[] pluginNames =
				{	"my.alphabet.letter.a_1.0.0",
					"my.alphabet.letter.b_1.0.0",
					"my.alphabet.letter.c_1.0.0",
					"my.alphabet.letter.e_1.0.0",
					"my.alphabet.round.letters_1.0.0",
					"my.alphabet.straight.letters_1.0.0",
					"my.alphabet_1.0.0" };
			assertTrue(checkFilesNotInList(pluginNames, list));
		} catch (CoreException e) {
			System.err.println(e);
		}		
	}
	
	// makes sure all files/directories in "names" are NOT in the directory listing "list"
	public boolean checkFilesNotInList(
		String[] names,
		ArrayList list) {
		
		for (int i = 0; i < names.length; i++) {
			System.out.println("searching for " + names[i] + "...");
			if (list.contains(names[i]))
				return false;
		}
		return true;
	}
}
