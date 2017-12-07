/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

import java.util.*;
import junit.framework.TestSuite;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobGroup;
import org.eclipse.core.tests.internal.builders.TimerBuilder.RuleType;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ParallelBuildChainTest extends AbstractBuilderTest {
	public static junit.framework.Test suite() {
		return new TestSuite(ParallelBuildChainTest.class);
	}

	public ParallelBuildChainTest() {
		super(null);
	}

	public ParallelBuildChainTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setAutoBuilding(false);
		TimerBuilder.resetCount();
		IWorkspaceRoot root = getWorkspace().getRoot();
		IProject projectInstantaneousBuild1 = root.getProject("projectInstantaneousBuild1");
		IProject projectLongBuild1 = root.getProject("projectLongBuild1");
		IProject projectInstantaneousBuild2 = root.getProject("projectInstantaneousBuild2");
		IProject projectLongBuild2 = root.getProject("projectLongBuild2");
		IProject projectInstantaneousBuild3 = root.getProject("projectInstantaneousBuild3");
		IProject projectLongBuild3 = root.getProject("projectLongBuild3");
		ensureExistsInWorkspace(new IResource[] {projectInstantaneousBuild1, projectInstantaneousBuild2, projectInstantaneousBuild3, projectLongBuild1, projectLongBuild2, projectLongBuild3}, true);
		configureTimerBuilder(projectInstantaneousBuild1, 0);
		configureTimerBuilder(projectInstantaneousBuild2, 0);
		configureTimerBuilder(projectInstantaneousBuild3, 0);
		configureTimerBuilder(projectLongBuild1, 1000);
		configureTimerBuilder(projectLongBuild2, 1000);
		configureTimerBuilder(projectLongBuild3, 1000);
	}

	private void configureTimerBuilder(IProject project, int duration) throws CoreException {
		BuildCommand buildCommand = new BuildCommand();
		buildCommand.setBuilderName(TimerBuilder.BUILDER_NAME);
		Map<String, String> arguments = new HashMap<>(2, (float) 1.);
		arguments.put(TimerBuilder.DURATION_ARG, Integer.toString(duration));
		arguments.put(TimerBuilder.RULE_TYPE_ARG, TimerBuilder.RuleType.NO_CONFLICT.toString());
		buildCommand.setArguments(arguments);
		IProjectDescription projectDescription = project.getDescription();
		projectDescription.setBuildSpec(new ICommand[] {buildCommand});
		project.setDescription(projectDescription, getMonitor());
	}

	private void setTimerBuilderSchedulingRuleForAllProjects(TimerBuilder.RuleType type, IProgressMonitor monitor) throws CoreException {
		for (IProject project : getWorkspace().getRoot().getProjects()) {
			IProjectDescription projectDescription = project.getDescription();
			BuildCommand command = (BuildCommand) projectDescription.getBuildSpec()[0];
			Map<String, String> args = command.getArguments();
			if (args == null) {
				args = Collections.singletonMap(TimerBuilder.RULE_TYPE_ARG, type.toString());
			} else {
				args.put(TimerBuilder.RULE_TYPE_ARG, type.toString());
			}
			command.setArguments(args);
			projectDescription.setBuildSpec(new ICommand[] {command});
			project.setDescription(projectDescription, getMonitor());
		}
	}

	@Test
	public void testIndividualProjectBuildsInParallelNoConflict() throws CoreException, OperationCanceledException, InterruptedException {
		int projectsCount = getWorkspace().getRoot().getProjects().length;
		JobGroup group = new JobGroup("Build Group", 5, projectsCount);
		for (IProject project : getWorkspace().getRoot().getProjects()) {
			Job job = new Job("Building " + project) {
				@Override
				public IStatus run(IProgressMonitor monitor) {
					try {
						project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
						return Status.OK_STATUS;
					} catch (CoreException e) {
						return new Status(IStatus.ERROR, "org.eclipse.core.tests.resources", e.getMessage(), e);
					}
				}
			};
			job.setJobGroup(group);
			job.schedule();
		}
		Assert.assertTrue("Timeout, most likely a deadlock", group.join(5000, getMonitor()));
		assertEquals(getWorkspace().getRoot().getProjects().length, TimerBuilder.getTotalBuilds());
	}

	@Test
	public void testIndividualProjectBuildsInParallelProjectScheduling() throws CoreException, OperationCanceledException, InterruptedException {
		setTimerBuilderSchedulingRuleForAllProjects(RuleType.CURRENT_PROJECT, getMonitor());
		testIndividualProjectBuildsInParallelNoConflict();
	}

}
