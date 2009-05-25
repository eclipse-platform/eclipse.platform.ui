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

import org.eclipse.update.tests.UpdateTestsPlugin;

public class TestRemoteFeatureVersionMirror extends MirrorManagerTestCase {
	private static boolean isMirrored;
	
	public TestRemoteFeatureVersionMirror(String arg0) {
		super(arg0);
		errOutput = new StringBuffer();
		isMirrored = false;
	}

	public void umSetUp() {
		String featureId = "update.feature1";
		String version = "1.0.0";
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteFeatureVersionMirror";
		String mirrorURL = "http://update.eclipse.org/my-mirror-url";
		String fromRemoteSiteUrl = "file:" + dataPath + "mirrors/update-site1";
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
				+ "/temp/testRemoteFeatureVersionMirror";
		assertTrue(doesCategoryDefinitionExist(toLocalSiteUrl));
	}

	// ensure feature exists per site.xml
	public void testFeatureInSiteXMLExists(){
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteFeatureVersionMirror";
		String featureId = "update.feature1";
		String version = "1.0.0";
		assertTrue(checkFeatureInSiteXMLExists(toLocalSiteUrl, featureId, version));
	}
	
	// ensure site.xml is generated
	public void testSiteXMLExists() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteFeatureVersionMirror";
		checkSiteXML(toLocalSiteUrl);
	}

	// ensure policy.xml is generated
	public void testPolicyXMLExists() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteFeatureVersionMirror";
		checkPolicyXML(toLocalSiteUrl);
	}
	
	// ensure policy.xml uses the correct mirrorURL
	public void testPolicyURL() throws Exception{
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteFeatureVersionMirror";
		String mirrorURL = "http://update.eclipse.org/my-mirror-url";
		assertTrue(checkPolicyURL(toLocalSiteUrl, mirrorURL));
	}

	// ensure all jars for features and plugins mirrored
	public void testAllJarsExist() throws Exception {
		String toLocalSiteUrl =
			UpdateTestsPlugin.getPlugin().getStateLocation()
				+ "/temp/testRemoteFeatureVersionMirror";
		String featureId = "update.feature1";
		String version = "1.0.0";
		String jarName = featureId + "_" + version + ".jar";
		File file = new File(toLocalSiteUrl + "/features/" + jarName);
		assertTrue(file.exists());
	}

	// ensure output string buffer ends with "Mirror command completed
	// successfully."
	// note: output may instead by "Command completed successfully."
	public void testMirrorSuccess() throws Exception {
		super.testMirrorSuccess();
	}

}
