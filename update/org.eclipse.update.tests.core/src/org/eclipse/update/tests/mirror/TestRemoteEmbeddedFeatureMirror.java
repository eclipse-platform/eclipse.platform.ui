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

package org.eclipse.update.tests.mirror;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.update.tests.UpdateTestsPlugin;

public class TestRemoteEmbeddedFeatureMirror extends MirrorManagerTestCase {
	private static boolean isMirrored;

	public TestRemoteEmbeddedFeatureMirror(String arg0) {
		super(arg0);
		errOutput = new StringBuffer();
		isMirrored = false;
	}

	public void umSetUp() {
		String featureId = "update.feature1c";
		String version = "3.0.0";
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteEmbeddedFeatureMirror";
		String mirrorURL = "http://update.eclipse.org/my-mirror-url";
		String fromRemoteSiteUrl =
				"file:"
				+ dataPath
				+ "mirrors/update-site4";
		if (!isMirrored) {
			exitValue =
				performMirror(
					getCommand(fromRemoteSiteUrl,toLocalSiteUrl, featureId, version, mirrorURL));
			isMirrored = true;
		}
	}

	// ensure exit without problems
	public void testExitValue() throws Exception {
		super.testExitValue();
	}

	// ensure all category definitions exist
	public void testCategoryDefinitionsExist() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteEmbeddedFeatureMirror";
		assertTrue(doesCategoryDefinitionExist(toLocalSiteUrl));
	}

	public void testFeatureInSiteXMLExists() {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteEmbeddedFeatureMirror";
		String featureId = "update.feature1c";
		String version = "3.0.0";
		assertTrue(
			checkFeatureInSiteXMLExists(toLocalSiteUrl, featureId, version));
	}

	// ensure site.xml is generated
	public void testSiteXMLExists() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteEmbeddedFeatureMirror";
		checkSiteXML(toLocalSiteUrl);
	}

	// ensure policy.xml is generated
	public void testPolicyXMLExists() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteEmbeddedFeatureMirror";
		checkPolicyXML(toLocalSiteUrl);
	}

	// ensure policy.xml uses the correct mirrorURL
	public void testPolicyURL() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteEmbeddedFeatureMirror";
		String mirrorURL = "http://update.eclipse.org/my-mirror-url";
		assertTrue(checkPolicyURL(toLocalSiteUrl, mirrorURL));
	}

	// ensure all jars in features directory mirrored
	public void testAllFeatureJarsExist() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteEmbeddedFeatureMirror";
		String[] featureJars = { "update.feature1_1.0.0.jar", "update.feature1_1.0.1.jar", "update.feature1b_2.0.0.jar", "update.feature1c_3.0.0.jar" };
		assertTrue(checkAllFeatureJars(toLocalSiteUrl, featureJars));
	}

	// ensure all jars in plugins directory mirrored
	public void testAllPluginJarsExist() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteEmbeddedFeatureMirror";
		String[] pluginJars =
			{ "com.plugin1_1.0.0.jar", "com.plugin1a_1.0.1.jar" };
		assertTrue(checkAllPluginJars(toLocalSiteUrl, pluginJars));
	}

	// ensure output string buffer ends with "Mirror command completed
	// successfully."
	// note: output may instead by "Command completed successfully."
	public void testMirrorSuccess() throws Exception {
		super.testMirrorSuccess();
	}

	public boolean checkAllFeatureJars(String url, String[] jarNames) {
		File featuresDir = new File(url + "/features");
		assertTrue(featuresDir.exists());
		assertTrue(featuresDir.isDirectory());
		String[] list = featuresDir.list();

		ArrayList ls = new ArrayList();
		for (int i = 0; i < list.length; i++) {
			ls.add(list[i]);
		}

		assertEquals(list.length, jarNames.length);
		for (int j = 0; j < jarNames.length; j++) {
			assertTrue(ls.contains(jarNames[j]));
		}
		return true;
	}

	public boolean checkAllPluginJars(String url, String[] jarNames) {
		File pluginsDir = new File(url + "/plugins");
		assertTrue(pluginsDir.exists());
		assertTrue(pluginsDir.isDirectory());
		String[] list = pluginsDir.list();

		ArrayList ls = new ArrayList();
		for (int i = 0; i < list.length; i++) {
			ls.add(list[i]);
		}

		assertEquals(list.length, jarNames.length);
		for (int j = 0; j < jarNames.length; j++) {
			assertTrue(ls.contains(jarNames[j]));
		}
		return true;
	}

	public boolean checkCategoryDefinitionsContained(CategoryDefinition[] localDefs, CategoryDefinition[] remoteDefs){
		boolean hasMatch;
		for (int i = 0 ; i<localDefs.length; i++){
			hasMatch = false;
			for (int j = 0; j<remoteDefs.length; j++){
				if (localDefs[i].getName().equals(remoteDefs[j].getName())
						&& localDefs[i].getDesc().equals(remoteDefs[j].getDesc()))
					hasMatch = true;
			}
			if (!hasMatch)
				return false;
		}
		return true;
	}
	


	// start testing category definitions
	public void testCategoryDefinitions() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteEmbeddedFeatureMirror";
		String fromRemoteSiteUrl =
		        dataPath
				+ "mirrors/update-site4";
		CategoryDefinition[] localDefs = getCategoryDefinitions(toLocalSiteUrl);
		CategoryDefinition[] remoteDefs = getCategoryDefinitions(fromRemoteSiteUrl);
		assertTrue(checkCategoryDefinitionsContained(localDefs, remoteDefs));
	}



}
