package org.eclipse.core.tests.resources.regression;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.resources.usecase.SignaledBuilder;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import junit.framework.*;
import junit.textui.TestRunner;

public class IProjectTest extends EclipseWorkspaceTest {
public IProjectTest() {
}
public IProjectTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(IProjectTest.class);
}
public void test_1G0XIMA() throws CoreException {
	/* common objects */
	IProject project = project = getWorkspace().getRoot().getProject("MyProject");
	project.create(null);;
	project.open(null);

	/* define a visitor that renames all resources it visits */
	IResourceVisitor renameVisitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			IPath path = resource.getFullPath();
			path = path.removeLastSegments(1);
			long timestamp = System.currentTimeMillis();
			path = path.append(resource.getName() + " renamed at " + timestamp);
			resource.move(path, false, null);
			return true;
		}
	};

	/* test */
	try {
		project.accept(renameVisitor);
	} catch (CoreException e) {
		fail("1.0", e);
	}

	// cleanup
	project.delete(true, getMonitor());
}
public void test_1G5I6PV() throws CoreException {
	/* common objects */
	IProject project = project = getWorkspace().getRoot().getProject("MyProject");
	project.create(getMonitor());
	project.open(getMonitor());

	/* test */
	try {
		project.setLocal(true, IResource.DEPTH_ZERO, getMonitor());
	} catch (Exception e) {
		fail("1.0", e);
	}

	// cleanup
	project.delete(true, getMonitor());
}
/**
 * 1GC2FKV: ITPCORE:BuildManager triggers incremental build when doing full builds
 */
public void testAutoBuild_1GC2FKV() {
	// set auto build ON
	try {
		IWorkspaceDescription description = getWorkspace().getDescription();
		description.setAutoBuilding(true);
		getWorkspace().setDescription(description);
	} catch (CoreException e) {
		fail("0.0", e);
	}

	// create project with a builder
	IProject projectONE = getWorkspace().getRoot().getProject("Project_ONE");
	try {
		IProjectDescription prjDescription = getWorkspace().newProjectDescription("ProjectONE");
		ICommand command = prjDescription.newCommand();
		command.setBuilderName(SignaledBuilder.BUILDER_ID);
		prjDescription.setBuildSpec(new ICommand[] { command });
		projectONE.create(prjDescription, getMonitor());
		projectONE.open(getMonitor());
	} catch (CoreException e) {
		fail("0.1", e);
	}

	// create project with a builder
	IProject projectTWO = getWorkspace().getRoot().getProject("Project_TWO");
	try {
		IProjectDescription prjDescription = getWorkspace().newProjectDescription("Project_TWO");
		ICommand command = prjDescription.newCommand();
		command.setBuilderName(SignaledBuilder.BUILDER_ID);
		prjDescription.setBuildSpec(new ICommand[] { command });
		projectTWO.create(prjDescription, getMonitor());
		projectTWO.open(getMonitor());
	} catch (CoreException e) {
		fail("0.2", e);
	}

	SignaledBuilder projectONEbuilder = SignaledBuilder.getInstance(projectONE);
	SignaledBuilder projectTWObuilder = SignaledBuilder.getInstance(projectTWO);
	projectONEbuilder.reset();
	projectTWObuilder.reset();
	try {
		projectONE.build(IncrementalProjectBuilder.FULL_BUILD, null);
	} catch (CoreException e) {
		fail("1.0", e);
	}
	assertTrue("1.1", projectONEbuilder.wasExecuted());
	assertTrue("1.2", !projectTWObuilder.wasExecuted());

	projectONEbuilder.reset();
	projectTWObuilder.reset();
	try {
		projectTWO.build(IncrementalProjectBuilder.FULL_BUILD, SignaledBuilder.BUILDER_ID, null, null);
	} catch (CoreException e) {
		fail("2.0", e);
	}
	assertTrue("2.1", !projectONEbuilder.wasExecuted());
	assertTrue("2.2", projectTWObuilder.wasExecuted());

	projectONEbuilder.reset();
	projectTWObuilder.reset();
	try {
		projectTWO.touch(null);
	} catch (CoreException e) {
		fail("3.0", e);
	}
	//project one won't be executed because project didn't change.
	assertTrue("3.1", !projectONEbuilder.wasExecuted());
	assertTrue("3.2", projectTWObuilder.wasExecuted());
}
/**
 * 1G5FYZM: ITPCORE:WIN - Project.deleteWithoutForce does not look for out of sync children
 */
public void testDelete_1G5FYZM() {
	//FIXME: invalid test now? if delete_contents is true then force flag is ignored.
	if (true)
		return;
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}

	try {
		project.close(getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}

	IFile file = project.getFile("MyFile");
	ensureExistsInFileSystem(file);

	try {
		project.delete(true, false, getMonitor());
		fail("3.0");
	} catch (CoreException e) {
		assertEquals("3.1", 1, e.getStatus().getChildren().length);
	}

	assertExistsInFileSystem("4.0", file);

	// clean up
	try {
		project.delete(true, true, getMonitor());
	} catch (CoreException e) {
		fail("5.0", e);
	}
}
/**
 * 1GDW1RX: ITPCORE:ALL - IResource.delete() without force not working correctly
 */
public void testDelete_1GDW1RX() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}

	String[] paths = new String[] {"/1/", "/1/1", "/1/2", "/1/3", "/2/", "/2/1"};
	IResource[] resources = buildResources(project, paths);
	ensureExistsInWorkspace(resources, true);

	IFolder folder = project.getFolder("folder");
	ensureExistsInFileSystem(folder);

	IFile file = folder.getFile("MyFile");
	ensureExistsInFileSystem(file);

	try {
		project.delete(false, getMonitor());
		fail("3.0");
	} catch (CoreException e) {
		// FIXME: remove this check?
//		assertEquals("3.1", 2, e.getStatus().getChildren().length);
	}

	// clean up
	try {
		project.delete(true, true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
}