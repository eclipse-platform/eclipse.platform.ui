/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Broadcom Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.BuildConfiguration;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Tests configuration project build configuration on the project description
 */
public class ProjectBuildConfigsTest extends ResourceTest {
	public static Test suite() {
		return new TestSuite(ProjectBuildConfigsTest.class);
	}

	public ProjectBuildConfigsTest(String name) {
		super(name);
	}

	private IProject project;
	private String variantId0 = "Variant0";
	private String variantId1 = "Variant1";
	private String variantId2 = "Variant2";
	private IBuildConfiguration variant0;
	private IBuildConfiguration variant1;
	private IBuildConfiguration variant2;
	private IBuildConfiguration defaultVariant;

	public void setUp() throws Exception {
		project = getWorkspace().getRoot().getProject("ProjectBuildConfigsTest_Project");
		ensureExistsInWorkspace(new IProject[] {project}, true);
		variant0 = new BuildConfiguration(project, variantId0, null);
		variant1 = new BuildConfiguration(project, variantId1, "name1");
		variant2 = new BuildConfiguration(project, variantId2, "name2");
		defaultVariant = new BuildConfiguration(project, IBuildConfiguration.DEFAULT_CONFIG_ID);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		project.delete(true, null);
	}

	public void testBasics() throws CoreException {
		IProjectDescription desc = project.getDescription();
		IBuildConfiguration[] configs = new IBuildConfiguration[] {getWorkspace().newBuildConfiguration(project.getName(), variantId0), getWorkspace().newBuildConfiguration(project.getName(), variantId1)};
		desc.setBuildConfigurations(configs);
		project.setDescription(desc, getMonitor());

		assertEquals("1.0", new IBuildConfiguration[] {variant0, variant1}, project.getBuildConfigurations());
		assertEquals("1.1", variant0, project.getBuildConfiguration(variantId0));
		assertEquals("1.2", variant1, project.getBuildConfiguration(variantId1));

		// Build configuration names don't contribute to equality
		assertTrue("2.0", project.hasBuildConfiguration(variant0));
		assertTrue("2.1", project.hasBuildConfiguration(variant1));
		assertFalse("2.2", project.hasBuildConfiguration(variant2));

		assertEquals("3.0", variant0, project.getActiveBuildConfiguration());
		desc = project.getDescription();
		desc.setActiveBuildConfiguration(variantId1);
		project.setDescription(desc, getMonitor());
		assertEquals("3.1", variant1, project.getActiveBuildConfiguration());
		// test that setting the variant to an invalid id has no effect
		desc.setActiveBuildConfiguration(variantId2);
		assertEquals("3.2", variant1, project.getActiveBuildConfiguration());

		IBuildConfiguration variant = project.getBuildConfigurations()[0];
		assertEquals("4.0", project, variant.getProject());
		assertEquals("4.1", variantId0, variant.getId());
	}

	public void testDuplicates() throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setBuildConfigurations(new IBuildConfiguration[] {getWorkspace().newBuildConfiguration(project.getName(), variantId0), getWorkspace().newBuildConfiguration(project.getName(), variantId1), getWorkspace().newBuildConfiguration(project.getName(), variantId0)});
		project.setDescription(desc, getMonitor());
		assertEquals("1.0", new IBuildConfiguration[] {variant0, variant1}, project.getBuildConfigurations());
	}

	public void testDefaultVariant() throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setBuildConfigurations(new IBuildConfiguration[] {});
		project.setDescription(desc, getMonitor());

		assertEquals("1.0", new IBuildConfiguration[] {defaultVariant}, project.getBuildConfigurations());
		assertTrue("1.1", project.hasBuildConfiguration(defaultVariant));

		assertEquals("2.0", defaultVariant, project.getActiveBuildConfiguration());
		desc = project.getDescription();
		desc.setActiveBuildConfiguration(IBuildConfiguration.DEFAULT_CONFIG_ID);
		project.setDescription(desc, getMonitor());
		assertEquals("2.1", defaultVariant, project.getActiveBuildConfiguration());
	}

	public void testRemoveActiveVariant() throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setBuildConfigurations(new IBuildConfiguration[0]);
		desc.setBuildConfigurations(new IBuildConfiguration[] {variant0, variant1});
		project.setDescription(desc, getMonitor());
		assertEquals("1.0", variant0, project.getActiveBuildConfiguration());
		desc.setBuildConfigurations(new IBuildConfiguration[] {variant0, variant2});
		project.setDescription(desc, getMonitor());
		assertEquals("2.0", variant0, project.getActiveBuildConfiguration());
		desc = project.getDescription();
		desc.setActiveBuildConfiguration(variantId2);
		project.setDescription(desc, getMonitor());
		desc.setBuildConfigurations(new IBuildConfiguration[] {variant0, variant1});
		project.setDescription(desc, getMonitor());
		assertEquals("3.0", variant0, project.getActiveBuildConfiguration());
	}

	/**
	 * Tests that build configuration references are correct after moving a project
	 * @throws CoreException
	 */
	public void testProjectMove() throws CoreException {
		IProjectDescription desc = project.getDescription();
		IBuildConfiguration[] configs = new IBuildConfiguration[] {variant0, variant1};
		desc.setBuildConfigurations(configs);
		project.setDescription(desc, getMonitor());

		// Move the project. The build configurations should point at the new project
		String newProjectName = "projectMoved";
		desc = project.getDescription();
		desc.setName(newProjectName);
		project.move(desc, false, getMonitor());

		IProject newProject = getWorkspace().getRoot().getProject(newProjectName);
		assertTrue("1.0", newProject.exists());

		IBuildConfiguration[] newConfigs = newProject.getBuildConfigurations();
		for (int i = 0; i < configs.length; i++) {
			assertEquals("2." + i * 3, newProject, newConfigs[i].getProject());
			assertEquals("2." + i * 3 + 1, configs[i].getId(), newConfigs[i].getId());
		}
	}
}
