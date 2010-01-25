/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;

import org.eclipse.core.runtime.IPath;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.ProjectPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Test the ProjectPathVariableManager public methods
 */
public class ProjectPathVariableManagerTest extends ResourceTest {

	public static Test suite() {
		return new TestSuite(ProjectPathVariableManagerTest.class);
	}

	public ProjectPathVariableManagerTest() {
		super("");
	}

	public ProjectPathVariableManagerTest(String name) {
		super(name);
	}

	private static IProject getProject(String name) {
		return getWorkspace().getRoot().getProject(name);
	}

	public void testConvertToUserEditableFormat() {
		String result = ProjectPathVariableManager.convertToUserEditableFormat("C:\\foo\\bar");
		assertEquals("1.0", "C:\\foo\\bar", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("C:/foo/bar");
		assertEquals("1.1", "C:/foo/bar", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("VAR/foo/bar");
		assertEquals("1.2", "VAR/foo/bar", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${VAR}/foo/bar");
		assertEquals("1.3", "${VAR}/foo/bar", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${VAR}/../foo/bar");
		assertEquals("1.4", "${VAR}/../foo/bar", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${PARENT-1-VAR}/foo/bar");
		assertEquals("1.5", "${VAR}/../foo/bar", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${PARENT-0-VAR}/foo/bar");
		assertEquals("1.6", "${VAR}/foo/bar", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${PARENT-VAR}/foo/bar");
		assertEquals("1.7", "${PARENT-VAR}/foo/bar", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${PARENT-2}/foo/bar");
		assertEquals("1.8", "${PARENT-2}/foo/bar", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${PARENT}/foo/bar");
		assertEquals("1.9", "${PARENT}/foo/bar", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${PARENT-2-VAR}/foo/bar");
		assertEquals("2.0", "${VAR}/../../foo/bar", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${PARENT-2-VAR}/foo/${PARENT-4-BAR}");
		assertEquals("2.1", "${VAR}/../../foo/${BAR}/../../../..", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${PARENT-2-VAR}/foo${PARENT-4-BAR}");
		assertEquals("2.2", "${VAR}/../../foo${BAR}/../../../..", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${PARENT-2-VAR}/${PARENT-4-BAR}/foo");
		assertEquals("2.3", "${VAR}/../../${BAR}/../../../../foo", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("${PARENT-2-VAR}/f${PARENT-4-BAR}/oo");
		assertEquals("2.4", "${VAR}/../../f${BAR}/../../../../oo", result);

		result = ProjectPathVariableManager.convertToUserEditableFormat("/foo/bar");
		assertEquals("2.5", "/foo/bar", result);
	}

	public void testConvertFromUserEditableFormat() {
		IProject project = getProject("foo");
		ensureExistsInWorkspace(project, true);

		ProjectPathVariableManager manager = (ProjectPathVariableManager) project.getPathVariableManager();
		String result = manager.convertFromUserEditableFormat("C:\\foo\\bar");
		assertEquals("1.0", "C:/foo/bar", result);

		result = manager.convertFromUserEditableFormat("C:/foo/bar");
		assertEquals("1.1", "C:/foo/bar", result);

		result = manager.convertFromUserEditableFormat("VAR/foo/bar");
		assertEquals("1.2", "VAR/foo/bar", result);

		result = manager.convertFromUserEditableFormat("${VAR}/foo/bar");
		assertEquals("1.3", "${VAR}/foo/bar", result);

		result = manager.convertFromUserEditableFormat("${VAR}/../foo/bar");
		assertEquals("1.4", "${PARENT-1-VAR}/foo/bar", result);

		result = manager.convertFromUserEditableFormat("${PARENT-1-VAR}/foo/bar");
		assertEquals("1.5", "${PARENT-1-VAR}/foo/bar", result);

		result = manager.convertFromUserEditableFormat("${PARENT-VAR}/foo/bar");
		assertEquals("1.6", "${PARENT-VAR}/foo/bar", result);

		result = manager.convertFromUserEditableFormat("${PARENT-2}/foo/bar");
		assertEquals("1.7", "${PARENT-2}/foo/bar", result);

		result = manager.convertFromUserEditableFormat("${PARENT}/foo/bar");
		assertEquals("1.8", "${PARENT}/foo/bar", result);

		result = manager.convertFromUserEditableFormat("${VAR}/../../foo/bar");
		assertEquals("1.9", "${PARENT-2-VAR}/foo/bar", result);

		result = manager.convertFromUserEditableFormat("${VAR}/../../foo/${BAR}/../../../../");
		assertEquals("2.0", "${PARENT-2-VAR}/foo/${PARENT-4-BAR}", result);

		result = manager.convertFromUserEditableFormat("${VAR}/../../foo${BAR}/../../../../");
		assertEquals("2.1", "${PARENT-2-VAR}/foo${PARENT-4-BAR}", result);

		result = manager.convertFromUserEditableFormat("${VAR}/../../${BAR}foo/../../../../");
		assertEquals("2.2", "${PARENT-2-VAR}/${PARENT-4-BARfoo}", result);
		
		IPath intermeiateValue = manager.getValue("BARfoo");
		assertEquals("2.3", "${BAR}foo", intermeiateValue.toPortableString());

		result = manager.convertFromUserEditableFormat("${VAR}/../../f${BAR}oo/../../../../");
		assertEquals("2.4", "${PARENT-2-VAR}/${PARENT-4-BARoo}", result);

		intermeiateValue = manager.getValue("BARoo");
		assertEquals("2.5", "f${BAR}oo", intermeiateValue.toPortableString());

		result = manager.convertFromUserEditableFormat("/foo/bar");
		assertEquals("2.6", "/foo/bar", result);
	}
}
