/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.perf;

import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.core.tests.internal.builders.SortBuilder;
import org.eclipse.core.tests.internal.builders.TestBuilder;

/**
 * Automated performance tests for builders.
 */
public class BuilderPerformanceTest extends WorkspacePerformanceTest {
	private static final int PROJECT_COUNT = 100;
	private static final int REPEAT = 20;

	public BuilderPerformanceTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new BuilderPerformanceTest("testManualBuildWithAutobuildOn"));
		return suite;
	}

	IProject[] otherProjects;

	/**
	 * Creates a project and fills it with contents
	 */
	void createAndPopulateProject(final IProject project, final IFolder folder, final int totalResources) {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
					desc.setBuildSpec(new ICommand[] {createCommand(desc, "Builder1"), createCommand(desc, "Builder2"), createCommand(desc, "Builder3"), createCommand(desc, "Builder4"), createCommand(desc, "Builder5")});
					project.create(desc, getMonitor());
					project.open(getMonitor());
					createFolder(folder, totalResources);
				}
			}, getMonitor());
		} catch (CoreException e) {
			fail("Failed to create project in performance test", e);
		}
	}

	/**
	 * Creates and returns a new command with the SortBuilder, and the TestBuilder.BUILD_ID 
	 * parameter set to the given value.
	 */
	protected ICommand createCommand(IProjectDescription description, String buildID) {
		return createCommand(description, SortBuilder.BUILDER_NAME, buildID);
	}

	/**
	 * Creates and returns a new command with the given builder name, and the TestBuilder.BUILD_ID 
	 * parameter set to the given value.
	 */
	protected ICommand createCommand(IProjectDescription description, String builderName, String buildID) {
		ICommand command = description.newCommand();
		Map args = command.getArguments();
		args.put(TestBuilder.BUILD_ID, buildID);
		command.setBuilderName(builderName);
		command.setArguments(args);
		return command;
	}

	protected void setUp() throws Exception {
		super.setUp();
		otherProjects = new IProject[PROJECT_COUNT];
		for (int i = 0; i < otherProjects.length; i++) {
			otherProjects[i] = getWorkspace().getRoot().getProject("Project " + i);
			IFolder folder = otherProjects[i].getFolder("Folder");
			createAndPopulateProject(otherProjects[i], folder, 100);
		}
	}

	/**
	 * Tests performing manual project-level increment builds when autobuild is on.
	 * See bug 261225 for details.
	 */
	public void testManualBuildWithAutobuildOn() {
		PerformanceTestRunner runner = new PerformanceTestRunner() {
			IProject[] projects;

			protected void setUp() {
				waitForBackgroundActivity();
				projects = getWorkspace().computeProjectOrder(getWorkspace().getRoot().getProjects()).projects;
			}

			protected void tearDown() {
			}

			protected void test() {
				try {
					for (int repeats = 0; repeats < REPEAT; repeats++) {
						for (int i = 0; i < projects.length; i++) {
							projects[i].build(IncrementalProjectBuilder.INCREMENTAL_BUILD, getMonitor());
						}
					}
				} catch (CoreException e) {
					fail("1.99", e);
				}
			}
		};
		//this test simulates a manual build before launch with autobuild enabled
		runner.setFingerprintName("Build workspace before launch");
		runner.run(this, REPEATS, 1);
	}
}
