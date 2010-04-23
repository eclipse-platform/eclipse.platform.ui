/*******************************************************************************
 *  Copyright (c) 2010 Broadcom Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     James Blackburn (Broadcom Corp.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.harness.TestBarrier;
import org.eclipse.core.tests.internal.builders.TestBuilder.BuilderRuleCallback;

/**
 * This class tests extended functionality (since 3.6) which allows
 * builders to be run with reduced (non-Workspace) scheduling rules.
 * 
 * When one of these builders runs, other threads may modify the workspace
 * depending on the builder's scheduling rule
 */
public class RelaxedSchedRuleBuilderTest extends AbstractBuilderTest {
	public static Test suite() {
		return new TestSuite(RelaxedSchedRuleBuilderTest.class);
	}

	public RelaxedSchedRuleBuilderTest() {
		super(null);
	}

	public RelaxedSchedRuleBuilderTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		getWorkspace().getRoot().delete(true, null);
		TestBuilder builder = DeltaVerifierBuilder.getInstance();
		if (builder != null)
			builder.reset();
		builder = EmptyDeltaBuilder.getInstance();
		if (builder != null)
			builder.reset();
		builder = EmptyDeltaBuilder2.getInstance();
		if (builder != null)
			builder.reset();
	}

	/**
	 * Test a simple builder with a relaxed scheduling rule
	 * @throws Exception
	 */
	public void testBasicRelaxedSchedulingRules() throws Exception {
		String name = "TestRelaxed";
		setAutoBuilding(false);
		final IProject project = getWorkspace().getRoot().getProject(name);
		create(project, false);
		addBuilder(project, EmptyDeltaBuilder.BUILDER_NAME);

		// Ensure the builder is instantiated
		project.build(IncrementalProjectBuilder.CLEAN_BUILD, getMonitor());

		final TestBarrier tb = new TestBarrier(TestBarrier.STATUS_WAIT_FOR_START);

		// Create a builder set a null scheduling rule
		EmptyDeltaBuilder builder = EmptyDeltaBuilder.getInstance();
		builder.setRuleCallback(new BuilderRuleCallback() {
			public ISchedulingRule getRule(String name, IncrementalProjectBuilder builder, int trigger, Map args) {
				return null;
			}

			public IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
				assertTrue(Job.getJobManager().currentRule() == null);
				tb.setStatus(TestBarrier.STATUS_START);
				while (!monitor.isCanceled())
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// Don't care
					}
				tb.setStatus(TestBarrier.STATUS_DONE);
				return super.build(kind, args, monitor);
			}
		});

		Job j = new Job("build job") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
				} catch (CoreException e) {
					fail();
				}
				return Status.OK_STATUS;
			}
		};
		j.schedule();

		// wait for build to be called
		tb.waitForStatus(TestBarrier.STATUS_START);

		// Should be able to write a file in the project
		create(project.getFile("foo.c"), false);
		assertTrue(project.getFile("foo.c").exists());

		// Cancel the builder
		j.cancel();
		tb.waitForStatus(TestBarrier.STATUS_DONE);
	}

	/**
	 * Test two builders, each with relaxed scheduling rules, run in the same build operation
	 * Each should be run with their own scheduling rule.
	 * Tests:
	 *     Bug 306824 - null scheduling rule and non-null scheduling rule don't work together
	 *     Builders should have separate scheduling rules
	 * @throws Exception
	 */
	public void testTwoBuildersRunInOneBuild() throws Exception {
		String name = "testTwoBuildersRunInOneBuild";
		setAutoBuilding(false);
		final IProject project = getWorkspace().getRoot().getProject(name);
		create(project, false);

		IProjectDescription desc = project.getDescription();
		desc.setBuildSpec(new ICommand[] {createCommand(desc, EmptyDeltaBuilder.BUILDER_NAME, "Project1Build1"), createCommand(desc, EmptyDeltaBuilder2.BUILDER_NAME, "Project1Build2")});
		project.setDescription(desc, getMonitor());

		// Ensure the builder is instantiated
		project.build(IncrementalProjectBuilder.CLEAN_BUILD, getMonitor());

		final TestBarrier tb1 = new TestBarrier(TestBarrier.STATUS_WAIT_FOR_START);
		final TestBarrier tb2 = new TestBarrier(TestBarrier.STATUS_WAIT_FOR_START);

		// Create a builder set a null scheduling rule
		EmptyDeltaBuilder builder = EmptyDeltaBuilder.getInstance();
		EmptyDeltaBuilder2 builder2 = EmptyDeltaBuilder2.getInstance();

		// Set the rule call-back
		builder.setRuleCallback(new BuilderRuleCallback() {
			public ISchedulingRule getRule(String name, IncrementalProjectBuilder builder, int trigger, Map args) {
				tb1.setStatus(TestBarrier.STATUS_START);
				return project;
			}

			public IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
				// shared scheduling rule
				assertTrue(Job.getJobManager().currentRule().contains(project));
				tb1.setStatus(TestBarrier.STATUS_RUNNING);
				tb1.waitForStatus(TestBarrier.STATUS_WAIT_FOR_DONE);
				tb1.setStatus(TestBarrier.STATUS_DONE);
				return super.build(kind, args, monitor);
			}
		});
		// Set the rule call-back
		builder2.setRuleCallback(new BuilderRuleCallback() {
			public ISchedulingRule getRule(String name, IncrementalProjectBuilder builder, int trigger, Map args) {
				// get rule is called before starting
				tb2.setStatus(TestBarrier.STATUS_START);
				return null;
			}

			public IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
				// shared scheduling rule
				assertTrue(Job.getJobManager().currentRule() == null || Job.getJobManager().currentRule().contains(getWorkspace().getRoot()));
				tb2.setStatus(TestBarrier.STATUS_RUNNING);
				tb2.waitForStatus(TestBarrier.STATUS_WAIT_FOR_DONE);
				tb2.setStatus(TestBarrier.STATUS_DONE);
				return super.build(kind, args, monitor);
			}
		});

		// Run the build
		Job j = new Job("build job1") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
				} catch (CoreException e) {
					fail();
				}
				return Status.OK_STATUS;
			}
		};
		j.schedule();

		// Wait for the build to transition

		tb1.waitForStatus(TestBarrier.STATUS_RUNNING);
		tb1.setStatus(TestBarrier.STATUS_WAIT_FOR_DONE);
		tb1.waitForStatus(TestBarrier.STATUS_DONE);

		tb2.waitForStatus(TestBarrier.STATUS_RUNNING);
		tb2.setStatus(TestBarrier.STATUS_WAIT_FOR_DONE);
		tb2.waitForStatus(TestBarrier.STATUS_DONE);
	}

}
