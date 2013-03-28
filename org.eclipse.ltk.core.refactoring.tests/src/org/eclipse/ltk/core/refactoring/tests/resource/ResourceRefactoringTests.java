/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin <eclipse.sprigogin@gmail.com> - [refactoring] Provide a way to implement refactorings that depend on resources that have to be explicitly released - https://bugs.eclipse.org/347599
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.resource;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.filesystem.EFS;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContext;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourcesDescriptor;
import org.eclipse.ltk.core.refactoring.resource.MoveResourceChange;
import org.eclipse.ltk.core.refactoring.resource.MoveResourcesDescriptor;
import org.eclipse.ltk.core.refactoring.tests.util.SimpleTestProject;

public class ResourceRefactoringTests extends TestCase {

	public static Test suite() {
		TestSuite suite= new TestSuite(ResourceRefactoringTests.class.getName());
		suite.addTestSuite(ResourceRefactoringTests.class);
		suite.addTestSuite(ResourceRefactoringUndoTests.class);
		return suite;
	}

	private SimpleTestProject fProject;

	protected void setUp() throws Exception {
		fProject= new SimpleTestProject();
	}

	protected void tearDown() throws Exception {
		fProject.delete();
	}

	public void testMoveChange1() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		Change undoChange= perform(new MoveResourceChange(file, destination));

		IResource movedResource= assertMove(file, destination, content);

		perform(undoChange);

		assertMove(movedResource, file.getParent(), content);
	}

	public void testMoveChange2() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		Change undoChange= perform(new MoveResourceChange(testFolder, destination));


		IFolder movedResource= (IFolder) assertMove(testFolder, destination, null);
		assertTrue(movedResource.getFile("myFile.txt").exists());

		perform(undoChange);

		assertMove(movedResource, testFolder.getParent(), null);
		assertTrue(testFolder.getFile("myFile.txt").exists());
	}

	public void testMoveChange3() throws Exception {
		// move with overwrite

		String content1= "hello";
		String content2= "world";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile.txt", content2);

		Change undoChange= perform(new MoveResourceChange(file1, destination));

		IResource movedResource= assertMove(file1, destination, content1);

		perform(undoChange);

		assertMove(movedResource, file1.getParent(), content1);

		assertTrue(content2.equals(fProject.getContent(file2)));
	}

	public void testMoveRefactoring1() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveResourcesDescriptor.ID);
		MoveResourcesDescriptor descriptor= (MoveResourcesDescriptor) contribution.createDescriptor();

		descriptor.setResourcesToMove(new IResource[] { file });
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		IResource movedResource= assertMove(file, destination, content);

		perform(undoChange);

		assertMove(movedResource, file.getParent(), content);
	}

	public void testMoveRefactoring2() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveResourcesDescriptor.ID);
		MoveResourcesDescriptor descriptor= (MoveResourcesDescriptor) contribution.createDescriptor();

		descriptor.setResourcesToMove(new IResource[] { testFolder });
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		IFolder movedResource= (IFolder) assertMove(testFolder, destination, null);
		assertTrue(movedResource.getFile("myFile.txt").exists());

		perform(undoChange);

		assertMove(movedResource, testFolder.getParent(), null);
		assertTrue(testFolder.getFile("myFile.txt").exists());
	}

	public void testMoveRefactoring3() throws Exception {
		// move with overwrite

		String content1= "hello";
		String content2= "world";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile.txt", content2);

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveResourcesDescriptor.ID);
		MoveResourcesDescriptor descriptor= (MoveResourcesDescriptor) contribution.createDescriptor();

		descriptor.setResourcesToMove(new IResource[] { file1 });
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		IResource movedResource= assertMove(file1, destination, content1);

		perform(undoChange);

		assertMove(movedResource, file1.getParent(), content1);
		assertTrue(content2.equals(fProject.getContent(file2)));
	}

	public void testDeleteRefactoring1_bug343584() throws Exception {
		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", "hello");
		
		IProject testProject2= ResourcesPlugin.getWorkspace().getRoot().getProject(SimpleTestProject.TEST_PROJECT_NAME + "2");
		try {
			testProject2.create(null);
			testProject2.open(null);

			RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
			DeleteResourcesDescriptor descriptor= (DeleteResourcesDescriptor)contribution.createDescriptor();

			descriptor.setDeleteContents(true);
			descriptor.setResources(new IResource[] { fProject.getProject(), testProject2 });

			perform(descriptor);

			assertFalse(fProject.getProject().exists());
			assertFalse(testProject2.exists());
		} finally {
			testProject2.delete(true, true, null);
		}
	}

	public void testDeleteRefactoring2_bug343584() throws Exception {
		IPath location= fProject.getProject().getLocation();
		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", "hello");
		
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		String p2Name= "p2";
		IProjectDescription p2Description= workspace.newProjectDescription(p2Name);
		p2Description.setLocation(location.append(p2Name));
		IProject p2= workspace.getRoot().getProject(p2Name);
		p2.create(p2Description, null);
		p2.open(null);
		IPath p2Location= p2.getLocation();
		
		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
		DeleteResourcesDescriptor descriptor= (DeleteResourcesDescriptor) contribution.createDescriptor();
		
		descriptor.setDeleteContents(true);
		descriptor.setResources(new IResource[] { fProject.getProject(), p2 });
		
		perform(descriptor);
		
		assertFalse(fProject.getProject().exists());
		assertFalse(p2.exists());
		
		assertFalse(location.toFile().exists());
		assertFalse(p2Location.toFile().exists());
	}
	
	public void testDeleteRefactoring3_bug343584() throws Exception {
		IPath location= fProject.getProject().getLocation();
		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", "hello");
		IPath fileLocation= file.getLocation();
		
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		String p2Name= "p2";
		IProjectDescription p2Description= workspace.newProjectDescription(p2Name);
		p2Description.setLocation(location.append(p2Name));
		IProject p2= workspace.getRoot().getProject(p2Name);
		p2.create(p2Description, null);
		p2.open(null);
		IPath p2Location= p2.getLocation();
		
		try {
			RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
			DeleteResourcesDescriptor descriptor= (DeleteResourcesDescriptor) contribution.createDescriptor();
			
			descriptor.setDeleteContents(false);
			descriptor.setResources(new IResource[] { fProject.getProject(), p2 });
			
			perform(descriptor);
			
			assertFalse(fProject.getProject().exists());
			assertFalse(p2.exists());
			
			assertTrue(location.toFile().exists());
			assertTrue(fileLocation.toFile().exists());
			assertTrue(p2Location.toFile().exists());
			
		} finally {
			EFS.getLocalFileSystem().getStore(location).delete(EFS.NONE, null);
			EFS.getLocalFileSystem().getStore(p2Location).delete(EFS.NONE, null);
		}
	}
	
	private Change perform(Change change) throws CoreException {
		PerformChangeOperation op= new PerformChangeOperation(change);
		op.run(null);
		assertTrue(op.changeExecuted());
		return op.getUndoChange();
	}

	private Change perform(RefactoringDescriptor descriptor) throws CoreException {
		RefactoringStatus status= new RefactoringStatus();
		final RefactoringContext context= descriptor.createRefactoringContext(status);
		try {
			final Refactoring refactoring= context != null ? context.getRefactoring() : null;
			assertTrue(status.isOK());

			PerformRefactoringOperation op= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
			op.run(null);
			RefactoringStatus validationStatus= op.getValidationStatus();
			assertTrue(!validationStatus.hasFatalError() && !validationStatus.hasError());
			return op.getUndoChange();
		} finally {
			if (context != null)
				context.dispose();
		}
	}

	private IResource assertMove(IResource source, IContainer destination, String content) throws CoreException, IOException {
		IResource res= destination.findMember(source.getName());

		assertTrue(res != null);
		assertTrue(res.getType() == source.getType());

		if (res instanceof IFile) {
			assertTrue(content.equals(fProject.getContent((IFile) res)));
		}
		return res;
	}


}
