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

public class TestFeatureInstall extends StandaloneManagerTestCase {
	private static boolean isInstalled;
	public URL TARGET_FILE_SITE;
	public TestFeatureInstall(String arg0) {
		super(arg0);
		isInstalled = false;
		try {
			TARGET_FILE_SITE = new URL("file", null, "D:/temp/standalone/mytarget/");
		} catch (MalformedURLException e) {
			System.err.println(e);
		}
	}

	public void umSetUp() {
		System.out.println("looking at configured sites available....");
		checkConfiguredSites();
		
		String featureId = "my.alphabet";
		String version = "1.0.0";
		String config = "file:D:/temp/standalone/config/";
		String fromRemoteSiteUrl =
			"http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-update-home/3.0/site/";
		if (!isInstalled) {
			System.out.println(
				"==============" + this.getClass() + "=============");
			StandaloneUpdateApplication app = new StandaloneUpdateApplication();
			try {
				exitValue = (Integer)app.run(getCommand(
					"install",
					featureId,
					version,
					config,
					fromRemoteSiteUrl,
					TARGET_FILE_SITE.getFile()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isInstalled = true;
			createConfiguredSite(TARGET_FILE_SITE);
		}
		
	}

	public void testPluginsExist() {
		try {
			ISite localSite = SiteManager.getSite(TARGET_FILE_SITE, null);
			System.out.println("TARGET_FILE_SITE: " + TARGET_FILE_SITE);
			if (localSite.getCurrentConfiguredSite() == null) {
				System.out.println("local site has null current config site");
				localSite = getConfiguredSite(TARGET_FILE_SITE.getFile());
			}
			System.out.println("localSite: " + localSite.getURL().getFile());
			IPluginEntry[] pluginEntries = localSite.getPluginEntries();
			ArrayList list = new ArrayList();
			if (pluginEntries == null || pluginEntries.length == 0){
				System.err.println("No plugin entries on the target site");
				fail("No plugin entries on the target site");
			} else{
				for (int i = 0; i < pluginEntries.length; i++){
					System.out.println("found plugin: " + pluginEntries[i].getVersionedIdentifier().toString());
					list.add(pluginEntries[i].getVersionedIdentifier().toString());
				}
			}

			String[] pluginNames =
				{	"my.alphabet.letter.a_1.0.0",
					"my.alphabet.letter.b_1.0.0",
					"my.alphabet.letter.c_1.0.0",
					"my.alphabet.letter.e_1.0.0",
					"my.alphabet.round.letters_1.0.0",
					"my.alphabet.straight.letters_1.0.0",
					"my.alphabet_1.0.0" };
			assertTrue(checkFilesInList(pluginNames, list));
		} catch (CoreException e) {
			System.err.println(e);
		}
	}

	public void testFeaturesExist() {
		try {
			ISite localSite = SiteManager.getSite(TARGET_FILE_SITE, null);
			if (localSite.getCurrentConfiguredSite() == null) {
				System.out.println("local site has null current config site");
				localSite = getConfiguredSite(TARGET_FILE_SITE.getFile());
			}
			System.out.println("localSite: " + localSite.getURL().getFile());
			// get feature references 
			IFeatureReference[] localFeatures =
				localSite.getFeatureReferences();
			System.out.println("local currentCOnfigSite: " + localSite.getCurrentConfiguredSite());
			ArrayList list = new ArrayList();
			if (localFeatures == null || localFeatures.length == 0){
				System.err.println("No features on the target site");
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
			assertTrue(checkFilesInList(featureNames, list));
		} catch (CoreException e) {
			System.err.println(e);
		}
	}

	// makes sure all files/directories in "names" are in the directory listing "list"
	public boolean checkFilesInList(
		String[] names,
		ArrayList list) {
		
		for (int i = 0; i < names.length; i++) {
			System.out.println(names[i]);
			if (!list.contains(names[i])){
				return false;
			}
		}
		return true;
	}

	// ensure exit without problems
	public void testExitValue() throws Exception {
		System.out.println("exitValue: " + exitValue);
		assertEquals(exitValue, IPlatformRunnable.EXIT_OK);
	}
}
