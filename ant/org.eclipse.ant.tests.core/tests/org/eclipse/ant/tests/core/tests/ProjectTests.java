/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
package org.eclipse.ant.tests.core.tests;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.tests.core.AbstractAntTest;
import org.eclipse.ant.tests.core.testplugin.AntTestChecker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class ProjectTests extends AbstractAntTest {

	public ProjectTests(String name) {
		super(name);
	}

	/**
	 * Tests that the three properties that should always be set are correct
	 */
	public void testBasePropertiesSet() throws CoreException {
		String buildFileName = "TestForEcho.xml"; //$NON-NLS-1$
		run(buildFileName);
		IFile buildFile = getBuildFile(buildFileName);
		String fullName = buildFile.getLocation().toFile().getAbsolutePath();
		assertEquals("eclipse.running should have been set as true", "true", AntTestChecker.getDefault().getUserProperty("eclipse.running")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("ant.file should have been set as the build file name", fullName, AntTestChecker.getDefault().getUserProperty("ant.file")); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull("ant.java.version should have been set", AntTestChecker.getDefault().getUserProperty("ant.java.version")); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull("ant.version should have been set", AntTestChecker.getDefault().getUserProperty("ant.version")); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull("eclipse.home should have been set", AntTestChecker.getDefault().getUserProperty("eclipse.home")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testValue() throws CoreException {
		String buildFileName = "TestForEcho.xml"; //$NON-NLS-1$
		run(buildFileName);
		assertEquals("property.testing should have been set as true", "true", AntTestChecker.getDefault().getUserProperty("property.testing")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testValueWithClass() throws CoreException {

		String buildFileName = "TestForEcho.xml"; //$NON-NLS-1$
		run(buildFileName);
		assertEquals("property.testing2 should have been set as hey", "hey", AntTestChecker.getDefault().getUserProperty("property.testing2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testClass() throws CoreException {
		String buildFileName = "TestForEcho.xml"; //$NON-NLS-1$
		run(buildFileName);
		assertEquals("property.testing3 should have been set as AntTestPropertyProvider", "AntTestPropertyValueProvider", AntTestChecker.getDefault().getUserProperty("property.testing3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testHeadless() throws CoreException {
		try {
			AntCorePlugin.getPlugin().setRunningHeadless(true);
			String buildFileName = "TestForEcho.xml"; //$NON-NLS-1$
			run(buildFileName);
			assertNull("property.headless should not have been set as AntTestPropertyProvider", AntTestChecker.getDefault().getUserProperty("property.headless")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		finally {
			AntCorePlugin.getPlugin().setRunningHeadless(false);
		}
	}

	public void testNotHeadless() throws CoreException {
		String buildFileName = "TestForEcho.xml"; //$NON-NLS-1$
		run(buildFileName);
		assertEquals("property.headless should have been set as AntTestPropertyProvider", "headless", AntTestChecker.getDefault().getUserProperty("property.headless")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}