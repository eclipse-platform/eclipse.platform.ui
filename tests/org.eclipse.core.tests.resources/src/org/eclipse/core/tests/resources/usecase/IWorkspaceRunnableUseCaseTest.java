package org.eclipse.core.tests.resources.usecase;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class IWorkspaceRunnableUseCaseTest extends EclipseWorkspaceTest {
public IWorkspaceRunnableUseCaseTest() {
}
public IWorkspaceRunnableUseCaseTest(String name) {
	super(name);
}
protected IWorkspaceRunnable createRunnable(final IProject project, final IWorkspaceRunnable nestedOperation, final boolean triggerBuild, final boolean shouldCancel) {
	return new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			if (shouldCancel)
				throw new OperationCanceledException();
			if (triggerBuild)
				project.touch(getMonitor());
			if (nestedOperation != null)
				getWorkspace().run(nestedOperation, getMonitor());
		}
	};
}
public static Test suite() {
	return new TestSuite(IWorkspaceRunnableUseCaseTest.class);
}
public void testNestedOperationsAndBuilds() {
	IWorkspaceDescription original = getWorkspace().getDescription();
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		IWorkspaceDescription description = getWorkspace().getDescription();
		description.setAutoBuilding(true);
		getWorkspace().setDescription(description);
		IProjectDescription prjDescription = getWorkspace().newProjectDescription("MyProject");
		ICommand command = prjDescription.newCommand();
		command.setBuilderName(SignaledBuilder.BUILDER_ID);
		prjDescription.setBuildSpec(new ICommand[] { command });
		project.create(prjDescription, getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}
	SignaledBuilder builder = SignaledBuilder.getInstance(project);

	/* should trigger a build */
	IWorkspaceRunnable op1 = createRunnable(project, null, true, false);
	IWorkspaceRunnable op2 = createRunnable(project, op1, false, false);
	IWorkspaceRunnable op3 = createRunnable(project, op2, false, false);
	builder.reset();
	try {
		getWorkspace().run(op3, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}
	assertTrue("1.1", builder.wasExecuted());
	
	/* should not trigger a build */
	op1 = createRunnable(project, null, true, true);
	op2 = createRunnable(project, op1, true, false);
	op3 = createRunnable(project, op2, true, false);
	builder.reset();
	try {
		getWorkspace().run(op3, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	} catch (OperationCanceledException e) {
	}
	assertTrue("2.1", !builder.wasExecuted());

	/* should not trigger a build */
	op1 = createRunnable(project, null, false, false);
	op2 = createRunnable(project, op1, false, false);
	op3 = createRunnable(project, op2, false, false);
	builder.reset();
	try {
		getWorkspace().run(op3, getMonitor());
	} catch (CoreException e) {
		fail("3.0", e);
	} catch (OperationCanceledException e) {
	}
	assertTrue("3.1", !builder.wasExecuted());
		
	/* remove trash */
	try {
		project.delete(true, getMonitor());
		getWorkspace().setDescription(original);
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
}
