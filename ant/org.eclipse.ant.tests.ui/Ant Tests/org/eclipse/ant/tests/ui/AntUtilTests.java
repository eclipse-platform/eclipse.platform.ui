/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.model.AntTargetNode;
import org.eclipse.ant.launching.IAntLaunchConstants;
import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public class AntUtilTests extends AbstractAntUITest {

	public AntUtilTests(String name) {
		super(name);
	}

	public void testGetTargetsLaunchConfiguration() throws CoreException {
		String buildFileName = "echoing"; //$NON-NLS-1$
		File buildFile = getBuildFile(buildFileName + ".xml"); //$NON-NLS-1$
		String arguments = null;
		Map<String, String> properties = null;
		String propertyFiles = null;
		AntTargetNode[] targets = AntUtil.getTargets(buildFile.getAbsolutePath(), getLaunchConfiguration(buildFileName, arguments, properties, propertyFiles));
		assertTrue(targets != null);
		assertTrue("Incorrect number of targets retrieved; should be 4 was: " + targets.length, targets.length == 4); //$NON-NLS-1$
		assertContains("echo3", targets); //$NON-NLS-1$
	}

	public void testGetTargetsLaunchConfigurationMinusD() throws CoreException {
		String buildFileName = "importRequiringUserProp"; //$NON-NLS-1$
		File buildFile = getBuildFile(buildFileName + ".xml"); //$NON-NLS-1$
		String arguments = "-DimportFileName=toBeImported.xml"; //$NON-NLS-1$
		Map<String, String> properties = null;
		String propertyFiles = null;
		AntTargetNode[] targets = AntUtil.getTargets(buildFile.getAbsolutePath(), getLaunchConfiguration(buildFileName, arguments, properties, propertyFiles));
		assertTrue(targets != null);
		assertTrue("Incorrect number of targets retrieved; should be 3 was: " + targets.length, targets.length == 3); //$NON-NLS-1$
		assertContains("import-default", targets); //$NON-NLS-1$
	}

	public void testGetTargetsLaunchConfigurationMinusDAndProperty() throws CoreException {
		String buildFileName = "importRequiringUserProp"; //$NON-NLS-1$
		File buildFile = getBuildFile(buildFileName + ".xml"); //$NON-NLS-1$
		String arguments = "-DimportFileName=toBeImported.xml"; //$NON-NLS-1$
		// arguments should win
		Map<String, String> properties = new HashMap<>();
		properties.put("importFileName", "notToBeImported.xml"); //$NON-NLS-1$ //$NON-NLS-2$
		String propertyFiles = null;
		AntTargetNode[] targets = AntUtil.getTargets(buildFile.getAbsolutePath(), getLaunchConfiguration(buildFileName, arguments, properties, propertyFiles));
		assertTrue(targets != null);
		assertTrue("Incorrect number of targets retrieved; should be 3 was: " + targets.length, targets.length == 3); //$NON-NLS-1$
		assertContains("import-default", targets); //$NON-NLS-1$
	}

	public void testGetTargetsLaunchConfigurationProperty() throws CoreException {
		String buildFileName = "importRequiringUserProp"; //$NON-NLS-1$
		File buildFile = getBuildFile(buildFileName + ".xml"); //$NON-NLS-1$
		String arguments = null;
		Map<String, String> properties = new HashMap<>();
		properties.put("importFileName", "toBeImported.xml"); //$NON-NLS-1$ //$NON-NLS-2$
		String propertyFiles = null;
		AntTargetNode[] targets = AntUtil.getTargets(buildFile.getAbsolutePath(), getLaunchConfiguration(buildFileName, arguments, properties, propertyFiles));
		assertTrue(targets != null);
		assertTrue("Incorrect number of targets retrieved; should be 3 was: " + targets.length, targets.length == 3); //$NON-NLS-1$
		assertContains("import-default", targets); //$NON-NLS-1$
	}

	public void testGetTargetsLaunchConfigurationPropertyFile() throws CoreException {
		String buildFileName = "importRequiringUserProp"; //$NON-NLS-1$
		File buildFile = getBuildFile(buildFileName + ".xml"); //$NON-NLS-1$
		String arguments = null;
		Map<String, String> properties = null;
		String propertyFiles = "buildtest1.properties"; //$NON-NLS-1$
		AntTargetNode[] targets = AntUtil.getTargets(buildFile.getAbsolutePath(), getLaunchConfiguration(buildFileName, arguments, properties, propertyFiles));
		assertTrue(targets != null);
		assertTrue("Incorrect number of targets retrieved; should be 3 was: " + targets.length, targets.length == 3); //$NON-NLS-1$
		assertContains("import-default", targets); //$NON-NLS-1$
	}

	protected ILaunchConfiguration getLaunchConfiguration(String buildFileName, String arguments, Map<String, String> properties, String propertyFiles) throws CoreException {
		ILaunchConfiguration config = getLaunchConfiguration(buildFileName);
		assertNotNull("Could not locate launch configuration for " + buildFileName, config); //$NON-NLS-1$
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		if (arguments != null) {
			copy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, arguments);
		}
		if (properties != null) {
			copy.setAttribute(IAntLaunchConstants.ATTR_ANT_PROPERTIES, properties);
		}
		if (propertyFiles != null) {
			copy.setAttribute(IAntLaunchConstants.ATTR_ANT_PROPERTY_FILES, propertyFiles);
		}
		return copy;
	}

	/**
	 * Asserts that <code>displayString</code> is in one of the completion proposals.
	 */
	private void assertContains(String targetName, AntTargetNode[] targets) {
		boolean found = false;
		for (int i = 0; i < targets.length; i++) {
			AntTargetNode target = targets[i];
			String foundName = target.getTargetName();
			if (targetName.equals(foundName)) {
				found = true;
				break;
			}
		}
		assertEquals("Did not find target: " + targetName, true, found); //$NON-NLS-1$
	}

	public void testIsKnownAntFileName() throws Exception {
		assertTrue("The file name 'foo.xml' is a valid name", AntUtil.isKnownAntFileName("a/b/c/d/foo.xml")); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("The file name 'foo.ant' is a valid name", AntUtil.isKnownAntFileName("a/b/c/d/foo.ant")); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("The file name 'foo.ent' is a valid name", AntUtil.isKnownAntFileName("a/b/c/d/foo.ent")); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("The file name 'foo.macrodef' is a valid name", AntUtil.isKnownAntFileName("a/b/c/d/foo.macrodef")); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("The file name 'foo.xsi' is not a valid name", AntUtil.isKnownAntFileName("a/b/c/d/foo.xsi")); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("The file name 'foo.txt' is a valid name", AntUtil.isKnownAntFileName("a/b/c/d/foo.txt")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
