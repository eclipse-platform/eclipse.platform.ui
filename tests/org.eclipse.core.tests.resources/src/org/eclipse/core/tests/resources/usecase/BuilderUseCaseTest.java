package org.eclipse.core.tests.resources.usecase;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.core.tests.internal.builders.SortBuilder;
import org.eclipse.core.tests.resources.Comparator;
import org.eclipse.core.tests.resources.ResourceDeltaVerifier;

public class BuilderUseCaseTest extends EclipseWorkspaceTest {
public BuilderUseCaseTest() {
	super();
}
public BuilderUseCaseTest(String name) {
	super(name);
}
/**
 * Sets the workspace autobuilding to the desired value.
 */
protected void setAutoBuilding(boolean value) throws CoreException {
	IWorkspace workspace = getWorkspace();
	if (workspace.isAutoBuilding() == value)
		return;
	IWorkspaceDescription desc = workspace.getDescription();
	desc.setAutoBuilding(value);
	workspace.setDescription(desc);
}
public void testSortBuilder() {
	IProgressMonitor monitor = null;

	// Create some resource handles
	IProject proj = getWorkspace().getRoot().getProject("Project");
	IFolder unsortedFolder = proj.getFolder(SortBuilder.DEFAULT_UNSORTED_FOLDER);
	IFolder sortedFolder = proj.getFolder(SortBuilder.DEFAULT_SORTED_FOLDER);
	IFile file1a = unsortedFolder.getFile("File" + 1);
	IFile file2a = unsortedFolder.getFile("File" + 2);
	IFile file1b = sortedFolder.getFile("File" + 1);
	IFile file2b = sortedFolder.getFile("File" + 2);

	// Create unsorted content for each file
	byte[] bytes1a = new byte[] {0, 4, 1, 3, 2};
	byte[] bytes2a = new byte[] {9, 5, 8, 6, 7};

	// Create sorted content for each file (ascending order)
	byte[] bytes1b = new byte[] {0, 1, 2, 3, 4};
	byte[] bytes2b = new byte[] {5, 6, 7, 8, 9};

	// Create sorted content for each file (descending order)
	byte[] bytes1c = new byte[] {4, 3, 2, 1, 0};
	byte[] bytes2c = new byte[] {9, 8, 7, 6, 5};
	ByteArrayInputStream bais1a = null;
	ByteArrayInputStream bais2a = null;

	try {
		// Turn auto-building off
		setAutoBuilding(false);
		
		// Create some resources
		bais1a = new ByteArrayInputStream(bytes1a);
		bais2a = new ByteArrayInputStream(bytes2a);
		proj.create(monitor);
		proj.open(monitor);
		unsortedFolder.create(false, true, monitor);
		file1a.create(bais1a, false, monitor);
		file2a.create(bais2a, false, monitor);
	} catch (CoreException e) {
		fail("1.0", e);
	}

	// Set up a resource delta verifier
	ResourceDeltaVerifier verifier = new ResourceDeltaVerifier();
	getWorkspace().addResourceChangeListener(verifier);

	// Create and set a build spec for the project
	try {
		IProjectDescription desc = proj.getDescription();
		ICommand command = desc.newCommand();
		command.setBuilderName(SortBuilder.BUILDER_NAME);
		Map arguments = command.getArguments();
		arguments.put(SortBuilder.SORT_ORDER, SortBuilder.ASCENDING);
		desc.setBuildSpec(new ICommand[] {command});
	} catch (CoreException e) {
		fail("2.0", e);
	}

	// Build the solution
	try {
		verifier.addExpectedChange(sortedFolder, IResourceDelta.ADDED, 0);
		verifier.addExpectedChange(file1b, IResourceDelta.ADDED, 0);
		verifier.addExpectedChange(file2b, IResourceDelta.ADDED, 0);
		proj.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
		assertTrue("3.0 " + verifier.getMessage(), verifier.isDeltaValid());
		assertTrue("3.1", Comparator.equals(bytes1b, readBytesInWorkspace(file1b)));
		assertTrue("3.2", Comparator.equals(bytes2b, readBytesInWorkspace(file2b)));
	} catch (CoreException e) {
		fail("3.3", e);
	}

	// Change an unsorted file and build the solution
	try {
		verifier.addExpectedChange(file2a, IResourceDelta.CHANGED, IResourceDelta.CONTENT);
		bais2a = new ByteArrayInputStream(bytes1a);
		file2a.setContents(bais2a, true, false, monitor);
		assertTrue("4.0 " + verifier.getMessage(), verifier.isDeltaValid());

		verifier.addExpectedChange(file2b, IResourceDelta.CHANGED, IResourceDelta.CONTENT);
		proj.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		assertTrue("4.0 " + verifier.getMessage(), verifier.isDeltaValid());
		assertTrue("4.1", Comparator.equals(bytes1b, readBytesInWorkspace(file2b)));
	} catch (CoreException e) {
		fail("4.2", e);
	}

	// Remove an unsorted file and build the solution
	try {
		verifier.addExpectedChange(file2a, IResourceDelta.REMOVED, 0);
		file2a.delete(false, monitor);
		assertTrue("5.0 " + verifier.getMessage(), verifier.isDeltaValid());
		
		verifier.addExpectedChange(file2b, IResourceDelta.REMOVED, 0);
		proj.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		assertTrue("5.1 " + verifier.getMessage(), verifier.isDeltaValid());
	} catch (CoreException e) {
		fail("5.9", e);
	}

	// Add an unsorted file and build the solution
	try {
		verifier.reset();
		verifier.addExpectedChange(file2a, IResourceDelta.ADDED, 0);
		bais2a = new ByteArrayInputStream(bytes2a);
		file2a.create(bais2a, false, monitor);
		assertTrue("6.0 " + verifier.getMessage(), verifier.isDeltaValid());
		
		verifier.addExpectedChange(file2b, IResourceDelta.ADDED, 0);
		proj.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		assertTrue("6.1 " + verifier.getMessage(), verifier.isDeltaValid());
		assertTrue("6.2", Comparator.equals(bytes2b, readBytesInWorkspace(file2b)));
	} catch (CoreException e) {
		fail("6.9", e);
	}

	// Change the builder's sort order and build the solution
	try {
		IProjectDescription desc = proj.getDescription();
		ICommand command = desc.getBuildSpec()[0];
		Map arguments = command.getArguments();
		arguments.put(SortBuilder.SORT_ORDER, SortBuilder.DESCENDING);

		verifier.addExpectedChange(file1b, IResourceDelta.CHANGED, IResourceDelta.CONTENT | IResourceDelta.REPLACED);
		verifier.addExpectedChange(file2b, IResourceDelta.CHANGED, IResourceDelta.CONTENT | IResourceDelta.REPLACED);

		proj.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
		assertTrue("7.0 " + verifier.getMessage(), verifier.isDeltaValid());
		assertTrue("7.1", Comparator.equals(bytes1c, readBytesInWorkspace(file1b)));
		assertTrue("7.2", Comparator.equals(bytes2c, readBytesInWorkspace(file2b)));
	} catch (CoreException e) {
		fail("7.3", e);
	}
}
}
