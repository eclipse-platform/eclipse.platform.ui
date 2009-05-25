/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

public class TestRemoteDoubleEmbeddedFeatureMirror extends MirrorManagerTestCase {
	private static boolean isMirrored;

	public TestRemoteDoubleEmbeddedFeatureMirror(String arg0) {
		super(arg0);
		errOutput = new StringBuffer();
		isMirrored = false;
	}

	public void umSetUp() {
		String featureId = "update.feature1c";
		String version = "3.0.0";
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteDoubleEmbeddedFeatureMirror";
		String mirrorURL = "http://update.eclipse.org/my-mirror-url";
		// first mirror
		String fromRemoteSiteUrl =
			"file:"
				+ dataPath
				+ "mirrors/update-site1";
		if (!isMirrored) {
			exitValue =
				performMirror(
					getCommand(fromRemoteSiteUrl,toLocalSiteUrl, null, null, mirrorURL));
		
		// end of first mirror
		fromRemoteSiteUrl =
			"file:"
			+ dataPath
			+ "mirrors/update-site5";
			exitValue =
				performMirror(
					getCommand(fromRemoteSiteUrl,toLocalSiteUrl, featureId, version, mirrorURL));
			isMirrored = true;
		}
	}

	// make sure each feature is under its correct category names
	public void testFeatureCategories() throws Exception{
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteDoubleEmbeddedFeatureMirror";
		FeatureCategory[] mirrorFeatures = getFeatureCategories(toLocalSiteUrl);
		FeatureCategory[] required = new FeatureCategory[1];
		required[0] = new FeatureCategory();
		required[0].setFeatureID("update.feature1c");
		required[0].addCategory("Site5-Category1");
		assertTrue(checkFeatureCategoriesContained(required, mirrorFeatures));
	}
	
	// ensure exit without problems
	public void testExitValue() throws Exception {
		super.testExitValue();
	}
	
	// ensure category definitions exist
	public void testCategoryDefinitionsExist() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteDoubleEmbeddedFeatureMirror";
		assertTrue(doesCategoryDefinitionExist(toLocalSiteUrl));
	}

	// ensure feature exists per site.xml
	public void testFeatureInSiteXMLExists() {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteDoubleEmbeddedFeatureMirror";
		String featureId = "update.feature1c";
		String version = "3.0.0";
		assertTrue(
			checkFeatureInSiteXMLExists(toLocalSiteUrl, featureId, version));
	}

	// ensure site.xml is generated
	public void testSiteXMLExists() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteDoubleEmbeddedFeatureMirror";
		checkSiteXML(toLocalSiteUrl);
	}

	// ensure policy.xml is generated
	public void testPolicyXMLExists() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteDoubleEmbeddedFeatureMirror";
		checkPolicyXML(toLocalSiteUrl);
	}

	// ensure policy.xml references the correct mirrorURL
	public void testPolicyURL() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteDoubleEmbeddedFeatureMirror";
		String mirrorURL = "http://update.eclipse.org/my-mirror-url";
		assertTrue(checkPolicyURL(toLocalSiteUrl, mirrorURL));
	}

	// ensure all jars in features directory mirrored
	public void testAllFeatureJarsExist() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteDoubleEmbeddedFeatureMirror";
		String[] featureJars = { "update.feature1b_2.0.0.jar" };
		assertTrue(checkAllFeatureJars(toLocalSiteUrl, featureJars));
	}

	// ensure all jars in plugins directory mirrored
	public void testAllPluginJarsExist() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteDoubleEmbeddedFeatureMirror";
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

		for (int j = 0; j < jarNames.length; j++) {
			assertTrue(ls.contains(jarNames[j]));
		}
		return true;
	}

	public boolean checkFeatureCategoriesContained(FeatureCategory[] required,FeatureCategory[] localDefs){
		boolean hasMatch;
		for (int i = 0 ; i<required.length; i++){
			hasMatch = false;
			for (int j = 0; j<localDefs.length; j++){
				if (localDefs[j].getFeatureID().equals(required[i].getFeatureID()) &&
					checkCategoriesMatch(localDefs[j].getCategories(), required[i].getCategories())){
						hasMatch = true;
						j=localDefs.length;
				}
			}
			if (!hasMatch)
				return false;
		}
		return true;
	}
	
	public boolean checkCategoriesMatch(String[] localCat, String[] remoteCat){
		boolean hasMatch;
		for (int i = 0; i<localCat.length; i++){
			hasMatch = false;
			for (int j = 0; j<remoteCat.length; i++){
				if (localCat[i].equals(remoteCat[j])){
					hasMatch = true;
					j=remoteCat.length;
				}
			}
			if(!hasMatch)
				return false;
		}
		return true;
	}

}
