/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.update.tests.standalone;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.standalone.*;

public class TestFeatureUpdate extends StandaloneManagerTestCase {
	public static boolean isUpdated;
	
	public TestFeatureUpdate(String arg0) {
		super(arg0);
		isUpdated = false;
	}

	public void umSetUp() {
		super.umSetUp();
		System.out.println("looking at configured sites available....");
		checkConfiguredSites();
		
		String featureId = "my.alphabet";
		String version = "1.0.1";

		if (!isUpdated) {
			System.out.println(
				"==============" + this.getClass() + "=============");
			StandaloneUpdateApplication app = new StandaloneUpdateApplication();
			try {
				exitValue = (Integer)app.run(getCommand(
						"update",
						featureId,
						version,
						null,
						null,
						TARGET_FILE_SITE.getFile()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			isUpdated = true;
		}
	}


	// ensure exit without problems
	public void testExitValue() throws Exception {
		System.out.println("exitValue: " + exitValue);
		assertEquals(exitValue, IPlatformRunnable.EXIT_OK);
	}
	
	public void testPluginsExist() {

			ISite localSite =  getConfiguredSite(TARGET_FILE_SITE);

			IPluginEntry[] pluginEntries = localSite.getPluginEntries();
			ArrayList list = new ArrayList();
			if (pluginEntries == null || pluginEntries.length == 0)
				fail("No plugin entries on the target site");
			else
				for (int i = 0; i < pluginEntries.length; i++) {
					list.add(
						pluginEntries[i].getVersionedIdentifier().toString());
				}

			String[] pluginNames =
				{
					"my.alphabet.letter.a_1.0.0",
					"my.alphabet.letter.b_1.0.0",
					"my.alphabet.letter.c_1.0.0",
					"my.alphabet.letter.e_1.0.0",
					"my.alphabet.round.letters_1.0.0",
					"my.alphabet.straight.letters_1.0.0",
					"my.alphabet_1.0.0",
					"my.alphabet.letter.a_1.0.1",
					"my.alphabet.letter.b_1.0.1",
					"my.alphabet.letter.c_1.0.1",
					"my.alphabet.letter.e_1.0.1",
					"my.alphabet.round.letters_1.0.1",
					"my.alphabet.straight.letters_1.0.1",
					"my.alphabet_1.0.1" };
			assertTrue(checkFilesInList(pluginNames, list));
	}

	public void testFeaturesExist() {
		try {
			ISite localSite = getConfiguredSite(TARGET_FILE_SITE);

			// get feature references
			IFeatureReference[] localFeatures =
				localSite.getFeatureReferences();
			ArrayList list = new ArrayList();
			if (localFeatures == null || localFeatures.length == 0) {
				fail("No features on the target site");
			} else {
				for (int i = 0; i < localFeatures.length; i++) {
					list.add(
						localFeatures[i].getVersionedIdentifier().toString());
					System.out.println(
						localFeatures[i].getVersionedIdentifier().toString());
				}
			}
			String[] featureNames =
				{
					"my.alphabet.round.letters_1.0.0",
					"my.alphabet.straight.letters_1.0.0",
					"my.alphabet_1.0.0",
					"my.alphabet.round.letters_1.0.1",
					"my.alphabet.straight.letters_1.0.1",
					"my.alphabet_1.0.1" };
			assertTrue(checkFilesInList(featureNames, list));
		} catch (CoreException e) {
			System.err.println(e);
		}
	}

	// makes sure all files/directories in "names" are in the directory listing
	// "list"
	public boolean checkFilesInList(String[] names, ArrayList list) {

		for (int i = 0; i < names.length; i++) {
			if (!list.contains(names[i]))
				return false;
		}
		return true;
	}

}
